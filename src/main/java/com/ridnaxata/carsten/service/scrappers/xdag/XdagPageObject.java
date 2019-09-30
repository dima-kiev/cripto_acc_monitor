package com.ridnaxata.carsten.service.scrappers.xdag;

import com.ridnaxata.carsten.model.Trx;
import com.ridnaxata.carsten.service.scrappers.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service("xdagPageObject")
public class XdagPageObject extends AbstractPageObject {

    private static final String DATE_PATTERN = "u-MM-dd HH:mm:ss.SSS";

    private static final String URL             = "https://explorer.xdag.io/block/";
    private static final String CHECK_XPATH     = "//*[@id=\"app\"]/div[2]/div/div[1]/div[1]/div/div[1]/div[1]/h2";
    private static final String EXPECTED        = "Block Information";
    private static final String TRX_CONTAINERS  = "//*[@id=\"block-as-address\"]/div[2]/table/tbody/tr";
    private static final String MORE_BTN        = "//*[@id=\"block-as-address\"]/div[3]/div/div[3]/a";
    private static final Map<String, String> fieldSelectors = new HashMap<String, String>(){{
        put("trxType", "td[1]/span");
        put("trxTime", "td[4]");
        // put("blockNumber", ""); no for xdag -> will use timestamp as blocknumber for intermal logic
        put("amount", "td[3]");
        put("trxHash", "td[2]/a");
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
        Predicate<WebElement> subtotalRows = rawBlockWebElemant -> !rawBlockWebElemant.findElement(By.xpath("td[1]/span")).getText().equals("TOTALS");

        List<Trx> allReadedBlocks = new ArrayList<>();
        boolean isNewPage = true;
        do {
            List<Trx> newBlocks = scrapTransactionContainers(TRX_CONTAINERS)
                    .stream()
                    .filter(subtotalRows)
                    .map(blockWebElement -> mapBlockToTrx(walletHash, blockWebElement))
                    .filter(onlyNewBlocksByDB)
                    .collect(Collectors.toList());
            allReadedBlocks.addAll(newBlocks);
            isNewPage = tryToClick(MORE_BTN);
        } while (isNewPage);

        return allReadedBlocks;
    }

    private Trx mapBlockToTrx(String walletHash, WebElement block) {
        String trxHash = block.findElement(By.xpath(fieldSelectors.get("trxHash"))).getText();
        String rawType = block.findElement(By.xpath(fieldSelectors.get("trxType"))).getText().toUpperCase();
        String trxType = "";
        if (rawType.equals("INPUT")) { // TO UNIFY. Snow was first and it set a kind of standart here. Wired but there.
            trxType = "output";
        } else if (rawType.equals("OUTPUT")) {
            trxType = "input";
        } // todo else if FEE and EARNING. Don`t know how it works yet
        Double amount = Double.parseDouble(block.findElement(By.xpath(fieldSelectors.get("amount"))).getText());
        LocalDateTime blockTime = parseBlockTime(block.findElement(By.xpath(fieldSelectors.get("trxTime"))).getText());
        Long blockNumber = blockTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();

        return new Trx().setWallet(walletHash)
                        .setTrxHash(trxHash)
                        .setBlockNumber(blockNumber)
                        .setBlockTime(blockTime)
                        .setTrxType(trxType)
                        .setAmount(amount);
    }


    private LocalDateTime parseBlockTime(String source) {
        return LocalDateTime.parse(source, DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

}