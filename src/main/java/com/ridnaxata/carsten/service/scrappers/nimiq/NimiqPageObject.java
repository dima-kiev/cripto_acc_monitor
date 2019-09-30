package com.ridnaxata.carsten.service.scrappers.nimiq;

import com.ridnaxata.carsten.model.Trx;
import com.ridnaxata.carsten.service.scrappers.AbstractPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("nimiqPageObject")
public class NimiqPageObject extends AbstractPageObject {

    private static final String DATE_PATTERN_0 = "d/M/u, H:mm:ss";
    private static final String DATE_PATTERN_1 = "M/d/u, h:mm:ss a";
    private static final String DATE_PATTERN_2 = "d/M/u, h:mm:ss";

    private static final String URL                   = "https://nimiq.watch/#";
    private static final String CHECK_XPATH           = "//*[@id=\"hash-format\"]";
    private static final String EXPECTED              = "Account Address";
    private static final String MORE_BTN              = "//*[@id=\"infobox\"]/div/div[4]/button";
    private static final String MORE_BLOCKS_BTN       = "//*[@id=\"infobox\"]/div/div[5]/button";
    private static final String BLOCKS_BTN            = "//*[@id=\"infobox\"]/div/div[3]/label[2]";
    private static final String TRX_CONTAINERS        = "//*[@id=\"infobox\"]/div/div[4]/div";
    private static final String TRX_BLOCKS_CONTAINERS = "//*[@id=\"infobox\"]/div/div[5]/div";
    // regular trx block
    private static final Map<String, String> fieldSelectorsRegBlock = new HashMap<String, String>(){{
        put("trxType", "span[2]");
        put("trxTime", "span[4]");
        put("blockNumber", "span[5]/a");
        put("amount", "span[1]");
        put("trxHash", "span[3]/a");
    }};
    // block sign fee trx block
    private static final Map<String, String> fieldSelectorsSignBlock = new HashMap<String, String>(){{
        put("trxType", "span[2]");
        put("trxTime", "span[3]");
        put("blockNumber", "span[4]/a");
        put("amount", "span[1]");
        put("trxHash", "span[4]/a");
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
        Predicate<WebElement> notLastBlock = webElement -> !webElement.getAttribute("class").contains("no-more");

        List<Trx> allReadedBlocks = new ArrayList<>();

        boolean isNewPage = true;
        do {
            List<Trx> newBlocks = scrapTransactionContainers(TRX_CONTAINERS)
                    .stream()
                    .skip(allReadedBlocks.size())
                    .filter(notLastBlock)
                    .map(blockWebElement -> mapBlockToTrx(walletHash, blockWebElement, fieldSelectorsRegBlock))
                    .filter(onlyNewBlocksByDB)
                    .collect(Collectors.toList());
            allReadedBlocks.addAll(newBlocks);
            isNewPage = tryToClick(MORE_BTN);
        } while (isNewPage);

        //tryToClick(MORE_BTN);
        int blocksProceedEarly = allReadedBlocks.size();
        sleep(500);
        executeScript("switchAccountHistory('blocks')");
        sleep(500);
        do { // todo more blocks btn not clicked. need to be tested more
            sleep(500);
            List<Trx> newBlocks = scrapTransactionContainers(TRX_BLOCKS_CONTAINERS)
                    .stream()
                    .skip(allReadedBlocks.size() - blocksProceedEarly)
                    .filter(notLastBlock)
                    .map(blockWebElement -> mapBlockToTrx(walletHash, blockWebElement, fieldSelectorsSignBlock))
                    .filter(onlyNewBlocksByDB)
                    .collect(Collectors.toList());
            allReadedBlocks.addAll(newBlocks);
            isNewPage = tryToClick(MORE_BLOCKS_BTN);
        } while (isNewPage);

        return allReadedBlocks;
    }

    private Trx mapBlockToTrx(String walletHash, WebElement block, Map<String, String> selectors) {
        Long blockNumber = Long.parseLong(block.findElement(By.xpath(selectors.get("blockNumber"))).getText().replace("#", ""));
        String trxHash;

        String trxTypeRaw = block.findElement(By.xpath(selectors.get("trxType"))).getText();
        String trxType = "";
        if (trxTypeRaw.equals("Mined block")) {
            trxType = "block";
            trxHash = blockNumber.toString();
        } else {
            if (trxTypeRaw.equals("Received transaction")) {
                trxType = "output";
            } else if (trxTypeRaw.equals("Sent transaction")) {
                trxType = "input";
            }
            trxHash = block.findElement(By.xpath(selectors.get("trxHash"))).getAttribute("href").replace("https://nimiq.watch/#", "");
        }

        LocalDateTime blockTime = parse(block.findElement(By.xpath(selectors.get("trxTime"))).getText());

        String amountString = block.findElement(By.xpath(selectors.get("amount"))).getText();
        Double amount = Double.parseDouble(amountString.substring(1,amountString.indexOf("N")-1).replace(" ", ""));

        return new Trx().setWallet(walletHash)
                .setBlockNumber(blockNumber)
                .setTrxHash(trxHash)
                .setBlockTime(blockTime)
                .setTrxType(trxType)
                .setAmount(amount);
    }

    private LocalDateTime parse(String source) {
        return Stream.of(DATE_PATTERN_0, DATE_PATTERN_1, DATE_PATTERN_2)
              .map(ptrn -> {
                  LocalDateTime parsed = null;
                  try {
                      parsed = LocalDateTime.parse(source, DateTimeFormatter.ofPattern(ptrn));
                  } catch (Exception e) {
                      // ignore
                  }
                  return parsed;
              })
              .filter(Objects::nonNull)
              .findFirst()
              .get();
    }

}