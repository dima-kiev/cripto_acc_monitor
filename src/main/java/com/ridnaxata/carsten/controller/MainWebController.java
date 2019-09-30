package com.ridnaxata.carsten.controller;

import com.ridnaxata.carsten.controller.forms.WalletsToCheckForm;
import com.ridnaxata.carsten.model.ByDaySummaryDTO;
import com.ridnaxata.carsten.model.Trx;
import com.ridnaxata.carsten.model.User;
import com.ridnaxata.carsten.model.WalletSummary;
import com.ridnaxata.carsten.service.SummaryService;
import com.ridnaxata.carsten.service.UserService;
import com.ridnaxata.carsten.service.scrappers.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EnableSpringDataWebSupport
@Controller
public class MainWebController {

    @Autowired
    private ScrapperService scrappers;

    @Autowired
    private UserService userService;

    @Autowired
    private SummaryService summaryService;

    @RequestMapping("/")
    public String index(Model model) {

        model.addAttribute("userId", 1603);
        model.addAttribute("userName", "Korben Dallas");

        model.addAttribute("userId2", 1604);
        model.addAttribute("userName2", "Lilu");

        return "index";
    }

    @RequestMapping("/user/{userId}")
    public String user(Model model, @PathVariable("userId") Integer userId) throws Exception {

        User user = userService.getUserById(userId); // todo to be replaced for principal after security adding

        model.addAttribute("userName", user.getName());
        model.addAttribute("form", new WalletsToCheckForm(user.getAccounts()));
        model.addAttribute("userId", userId);

        return "user";
    }

    @RequestMapping("/scrap-and-check/{userId}")
    public String scrapAndCheck(@ModelAttribute("form") WalletsToCheckForm form,
                        @PathVariable("userId") Integer userId,
                        Model model) throws Exception {

        scrappers.scrapNewTrxForWallets(form); // todo make it non blocking method, in new Thread, add progress interface to show on FE

        List<WalletSummary> summaries = summaryService.summarize(form);

        User user = userService.getUserById(userId); // todo to be replaced for principal after security addition

        model.addAttribute("userName", user.getName());
        model.addAttribute("userId", userId);
        model.addAttribute("walletsSummaries", summaries);

        return "check";
    }

    @RequestMapping("/check/{userId}")
    public String check(@ModelAttribute("form") WalletsToCheckForm form,
                        @PathVariable("userId") Integer userId,
                        Model model) throws Exception {

        List<WalletSummary> summaries = summaryService.summarize(form);

        User user = userService.getUserById(userId); // todo to be replaced for principal after security addition

        model.addAttribute("userName", user.getName());
        model.addAttribute("userId", userId);
        model.addAttribute("walletsSummaries", summaries);

        return "check";
    }

    @RequestMapping("/details/wallet/{walletHash1}/{walletHash2}/coin/{coinName}/from/{from}/to/{to}")
    public String getDetails2(@PageableDefault(size = 100) Pageable pageable,
                             @PathVariable("walletHash1") String walletHash1,
                             @PathVariable("walletHash2") String walletHash2,
                             @PathVariable("coinName") String coinName,
                             @PathVariable("from") String from, @PathVariable("to") String to,
                             Model model) {
        String walletHash = walletHash1 + "/" + walletHash2;

        Page<Trx> page = summaryService.readForWalletInPeriod(pageable, coinName, walletHash, parseLdt(from), parseLdt(to));
        Map<String, String> links = summaryService.createLinksForTrx(coinName, page);

        model.addAttribute("page", page);
        model.addAttribute("FromTime", from);
        model.addAttribute("ToTime", to);
        model.addAttribute("links", links);
        model.addAttribute("thisPageUrl", "/details/wallet/" + walletHash + "/coin/" + coinName);

        return "details";
    }

    @RequestMapping("/details/wallet/{walletHash}/coin/{coinName}/from/{from}/to/{to}")
    public String getDetails(@PageableDefault(size = 100) Pageable pageable,
                             @PathVariable("walletHash") String walletHash,
                             @PathVariable("coinName") String coinName,
                             @PathVariable("from") String from, @PathVariable("to") String to,
                             Model model) {

        Page<Trx> page = summaryService.readForWalletInPeriod(pageable, coinName, walletHash, parseLdt(from), parseLdt(to));
        Map<String, String> links = summaryService.createLinksForTrx(coinName, page);

        model.addAttribute("page", page);
        model.addAttribute("FromTime", from);
        model.addAttribute("ToTime", to);
        model.addAttribute("links", links);
        model.addAttribute("thisPageUrl", "/details/wallet/" + walletHash + "/coin/" + coinName);

        return "details";
    }

    @RequestMapping("/bydays/wallet/{walletHash}/coin/{coinName}/from/{from}/to/{to}")
    public String getDetailsByDays(@PageableDefault(size = 100) Pageable pageable,
                                   @PathVariable("walletHash") String walletHash,
                                   @PathVariable("coinName") String coinName,
                                   @PathVariable("from") String from, @PathVariable("to") String to,
                                   Model model) {

        List<ByDaySummaryDTO> smry = summaryService.readForWalletInPeriodByDays(coinName, walletHash, parseLdt(from), parseLdt(to));
        List<ByDaySummaryDTO> smryPage;
        int startFrom = pageable.getPageNumber() * pageable.getPageSize();
        int itemsLeft = smry.size() - startFrom;
        if (itemsLeft < pageable.getPageSize()) {
            smryPage = smry.subList(startFrom, smry.size());
        } else {
            smryPage = smry.subList(startFrom, startFrom + pageable.getPageSize());
        }

        Page<ByDaySummaryDTO> page = new PageImpl(smryPage, pageable, smry.size());

        model.addAttribute("page", page);
        model.addAttribute("FromTime", from);
        model.addAttribute("ToTime", to);
        model.addAttribute("thisPageUrl", "/bydays/wallet/" + walletHash + "/coin/" + coinName);

        return "bydays";
    }

    @RequestMapping("/bydays/wallet/{walletHash1}/{walletHash2}/coin/{coinName}/from/{from}/to/{to}")
    public String getDetailsByDays2(@PageableDefault(size = 100) Pageable pageable,
                                   @PathVariable("walletHash1") String walletHash1,
                                   @PathVariable("walletHash2") String walletHash2,
                                   @PathVariable("coinName") String coinName,
                                   @PathVariable("from") String from, @PathVariable("to") String to,
                                   Model model) {
        String walletHash = walletHash1 + "/" + walletHash2;

        List<ByDaySummaryDTO> smry = summaryService.readForWalletInPeriodByDays(coinName, walletHash, parseLdt(from), parseLdt(to));
        List<ByDaySummaryDTO> smryPage;
        int startFrom = pageable.getPageNumber() * pageable.getPageSize();
        int itemsLeft = smry.size() - startFrom;
        if (itemsLeft < pageable.getPageSize()) {
            smryPage = smry.subList(startFrom, smry.size());
        } else {
            smryPage = smry.subList(startFrom, startFrom + pageable.getPageSize());
        }

        Page<ByDaySummaryDTO> page = new PageImpl(smryPage, pageable, smry.size());

        model.addAttribute("page", page);
        model.addAttribute("FromTime", from);
        model.addAttribute("ToTime", to);
        model.addAttribute("thisPageUrl", "/bydays/wallet/" + walletHash + "/coin/" + coinName);

        return "bydays";
    }

    @RequestMapping("/csvbydays/wallet/{walletHash}/coin/{coinName}/from/{from}/to/{to}")
    public ResponseEntity<InputStreamResource> getCsvByDays(@PathVariable("walletHash") String walletHash,
                                                            @PathVariable("coinName") String coinName,
                                                            @PathVariable("from") String from, @PathVariable("to") String to,
                                                            Model model) {

        List<ByDaySummaryDTO> smry = summaryService.readForWalletInPeriodByDays(coinName, walletHash, parseLdt(from), parseLdt(to));

        String csvFileContent = smry.stream()
                                        .map(ByDaySummaryDTO::toCsvRecord)
                                        .collect(Collectors.joining());

        byte[] csvAsBytes = csvFileContent.getBytes();

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.TEXT_MARKDOWN);
        respHeaders.setContentLength(csvAsBytes.length);
        respHeaders.setContentDispositionFormData("attachment", "csvByDays.csv");

        InputStreamResource isr = new InputStreamResource(new ByteArrayInputStream(csvAsBytes));
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }

    @RequestMapping("/csvbydays/wallet/{walletHash1}/{walletHash2}/coin/{coinName}/from/{from}/to/{to}")
    public ResponseEntity<InputStreamResource> getCsvByDays2(@PathVariable("walletHash1") String walletHash1,
                                                             @PathVariable("walletHash2") String walletHash2,
                                                             @PathVariable("coinName") String coinName,
                                                             @PathVariable("from") String from, @PathVariable("to") String to,
                                                             Model model) {
        String walletHash = walletHash1 + "/" + walletHash2;

        List<ByDaySummaryDTO> smry = summaryService.readForWalletInPeriodByDays(coinName, walletHash, parseLdt(from), parseLdt(to));

        String csvFileContent = smry.stream()
                                        .map(ByDaySummaryDTO::toCsvRecord)
                                        .collect(Collectors.joining());

        byte[] csvAsBytes = csvFileContent.getBytes();

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.TEXT_MARKDOWN);
        respHeaders.setContentLength(csvAsBytes.length);
        respHeaders.setContentDispositionFormData("attachment", "csvByDays.csv");

        InputStreamResource isr = new InputStreamResource(new ByteArrayInputStream(csvAsBytes));
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private LocalDateTime parseLdt(String source) {
        return LocalDateTime.parse(source, DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

}

