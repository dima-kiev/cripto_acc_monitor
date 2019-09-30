package com.ridnaxata.carsten.service.scrappers.snow;

import com.ridnaxata.carsten.model.Trx;
import com.ridnaxata.carsten.service.scrappers.AbstractPageObject;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service("snowPageObject")
public class SnowPageObject extends AbstractPageObject {

    private static final String URL             = "https://explorer.snowblossom.org/?search=";
    private static final String CHECK_XPATH     = "/html/body/form/p[1]";
    private static final String EXPECTED        = "Address: snow:";
    private static final String TRX_CONTAINERS  = "/html/body/form/li/a[1]";
    private static final Map<String, String> fieldSelectors = new HashMap<String, String>(){{
        put("block", "/html/body/form/pre");
    }};

    public List<Trx> openWalletAndReadBlocks(String walletHash, Long latestStoredBlockNumber) {
        openWallet(walletHash);
        return readBlocks(walletHash, latestStoredBlockNumber);
    }

    private void openWallet(String walletHash) {
        openPage(URL + walletHash);
        checkPage(CHECK_XPATH, EXPECTED + walletHash);
    }

    public List<Trx> readBlocks(String walletHash, Long lastStoredBlock) {
        Predicate<String> onlyNewBlocksPredicate = blockNumber -> Long.parseLong(blockNumber) > lastStoredBlock;

        // todo make scrapping of numbers throw executeJSscript to improve the scrapping speed
        List<String> blockNumbers = scrapTransactionContainers(TRX_CONTAINERS)
                .stream()
                .map(WebElement::getText)
                .filter(onlyNewBlocksPredicate)
                .collect(Collectors.toList());

        return blockNumbers
                .stream()
                .map(blockNumber -> readBlock(URL + blockNumber, fieldSelectors))
                .map(m -> m.get("block"))
                //.map(blockText -> mapBlocksToTrxObject(walletHash, blockText))
                //.flatMap(Collection::stream)
                .flatMap(blockText -> mapBlocksToTrxObject(walletHash, blockText).stream())
                .collect(Collectors.toList());
    }

    private List<Trx> mapBlocksToTrxObject(String walletHash, String block) {
        List<Trx> blocks = new ArrayList<>();

        // TODO SET LOCALE TO AVOID TIME SHIFT (14hrs->17) WHEN CREATING DATE FROM TIMESTAMP
        int timestampLastPos = block.indexOf("timestamp: ") + "timestamp: ".length();
        long blockTimeStamp = Long.parseLong(block.substring(timestampLastPos, timestampLastPos + 13));
        LocalDateTime blockTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(blockTimeStamp),TimeZone.getDefault().toZoneId());

        int blockNumberPos = block.indexOf("height: ") + "height: ".length();
        int blockNumberEnds = block.indexOf("\n", blockNumberPos);
        Long blockNumber = Long.parseLong(block.substring(blockNumberPos, blockNumberEnds));

        int walletHashPos = block.indexOf(walletHash);
        while (walletHashPos != -1) {
            String trxType = block.substring(walletHashPos - 13, walletHashPos - 7);
            if ("Output".equals(trxType)) {
                int walletHashLastPos = walletHashPos + walletHash.length();
                int spaceAfterAmount = block.indexOf(" ", walletHashLastPos + 5);
                Double amount = Double.parseDouble(block.substring(walletHashLastPos, spaceAfterAmount));

                String snowTrx = findNearestTrxBackFromPosition(block, walletHashLastPos);

                blocks.add(new Trx().setWallet(walletHash)
                                        .setBlockNumber(blockNumber)
                                        .setBlockTime(blockTime)
                                        .setTrxHash(snowTrx)
                                        .setTrxType(trxType)
                                        .setAmount(amount));
            }
            walletHashPos = block.indexOf(walletHash, walletHashPos + 1);
        }

        return blocks;
    }

    private String findNearestTrxBackFromPosition(String block, int pos) {
        String marker = "Transaction: ";
        String scopeBlockParrt = block.substring(0, pos);
        int lastTrxPos = scopeBlockParrt.indexOf(marker);
        int prevTrxPos = 0;
        while (lastTrxPos != -1) {
            prevTrxPos = lastTrxPos;
            lastTrxPos = scopeBlockParrt.indexOf(marker, lastTrxPos + 1);
        }
        int trxHashPos = prevTrxPos + marker.length();
        return block.substring(trxHashPos, block.indexOf(" size:", trxHashPos + 1));
    }

}
