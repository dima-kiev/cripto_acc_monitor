package com.ridnaxata.carsten.service.scrappers.veo;

import com.ridnaxata.carsten.model.Trx;
import com.ridnaxata.carsten.service.scrappers.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service("veoPageObject")
public class VeoPageObject extends AbstractPageObject {

    private static final String DATE_PATTERN = "MMM-dd-u HH:mm:ss +z";

    private static final String URL = "https://veoscan.io/account/";
    private static final String CHECK_XPATH = "/html/body/div[2]/div/div/h1";
    private static final String EXPECTED = "Amoveo Account";
    private static final String TRX_CONTAINERS = "/html/body/div[2]/div/div/table[2]/tbody/tr";
    //private static final String MORE_BTN = "/html/body/div[2]/div/div/nav[1]/ul/li[10]/a";
    private static final String MORE_BTN = "/html/body/div[2]/div/div/nav[1]/ul/li[last()]/a";
    private static final Map<String, String> fieldSelectors = new HashMap<String, String>(){{
        put("trxType", "td[1]/a");
        put("trxTime", "td[3]");
        put("blockNumber", "td[2]");
        put("amount", "td[6]");
        put("amountFee", "td[7]");
        put("from", "td[4]/a");     // need to be extracted from attr
        put("to", "td[5]/a");       // need to be extracted from attr
        put("trxHash", "td[1]/a");  // need to be extracted from attr
    }};

    @Override
    public List<Trx> openWalletAndReadBlocks(String walletHash, Long latestStoredBlockNumber) {
        openWallet(walletHash);
        return readBlocks(walletHash, latestStoredBlockNumber);
    }

    private void openWallet(String walletHash) {
        openPage(URL + walletHash);
        checkPage(CHECK_XPATH, EXPECTED);
    }

    public List<Trx> readBlocks(String walletHash, Long lastStoredBlock) {
        Predicate<Trx> onlyNewBlocksByDB = trx -> trx.getBlockNumber() > lastStoredBlock;

        List<Trx> allReadedBlocks = new ArrayList<>();

        boolean isNewPage = true;
        do {
            List<Trx> newBlocks = scrapTransactionContainers(TRX_CONTAINERS)
                    .stream()
                    .map(blockWebElement -> mapBlockToTrx(walletHash, blockWebElement))
                    .filter(onlyNewBlocksByDB)
                    .collect(Collectors.toList());
            allReadedBlocks.addAll(newBlocks);
            isNewPage = tryToClick(MORE_BTN);
        } while (isNewPage);

        return allReadedBlocks;
    }

    private Trx mapBlockToTrx(String walletHash, WebElement block) {
        WebElement rowType = block.findElement(By.xpath(fieldSelectors.get("trxType")));
        String veoType = rowType.getText();
        String href = rowType.getAttribute("href");
        String trxHash = href.substring(href.indexOf("/tx/") + 4, href.length() - 2);
        Long blockNumber = Long.parseLong(block.findElement(By.xpath(fieldSelectors.get("blockNumber"))).getText());
        LocalDateTime blockTime = parseBlockTime(block.findElement(By.xpath(fieldSelectors.get("trxTime"))).getText());
        Double amount = parseAmount(block.findElement(By.xpath(fieldSelectors.get("amount"))).getText());
        Double amountFee = parseAmount(block.findElement(By.xpath(fieldSelectors.get("amountFee"))).getText());
        Boolean income = checkReciever(walletHash, block.findElement(By.xpath(fieldSelectors.get("to"))).getAttribute("href"));
        Double amnt; //todo how calculate the fee. Ask Carsten
        String trxType = "";
        if (income) {
            amnt = amount;
            if (veoType.equals("spend")) {
                trxType = "output";
            } else if (veoType.equals("create_acc")) {
                trxType = "output"; // todo ?????
            }
        } else {
            amnt = amount + amountFee;
            if (veoType.equals("spend")) {
                trxType = "input";
            } else if (veoType.equals("create_acc")) {
                trxType = "input"; // todo ?????
            }
        }

        return new Trx().setWallet(walletHash)
                        .setTrxHash(trxHash)
                        .setBlockNumber(blockNumber)
                        .setBlockTime(blockTime)
                        .setTrxType(trxType)
                        .setAmount(amnt);
    }

    private boolean checkReciever(String walletHash, String link) {
        //return link.contains(encodeWalletHash(walletHash));
        try {
            return link.contains(java.net.URLEncoder.encode(walletHash, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Double parseAmount(String rowAmount) {
        Double veoAmount;
        if (rowAmount.contains("mVEO")) {
            veoAmount = Double.parseDouble(rowAmount.replace(" mVEO", "")) * 1000;
        } else {
            veoAmount = Double.parseDouble(rowAmount.replace(" VEO", ""));
        }
        return veoAmount;
    }

    private LocalDateTime parseBlockTime(String source) {
        return LocalDateTime.parse(source, DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

/*    public static String encodeWalletHash(String walletHash) {
        return walletHash
                .replace("/", "%252F")
                .replace("=", "%253D")
                .replace("+", "%252B");
    }

    public static String decodeWalletHash(String encodedWalletHash) {
        return encodedWalletHash
                .replace("%2F", "/")
                .replace("%3D", "=")
                .replace("%2B", "+");
    }*/

}
