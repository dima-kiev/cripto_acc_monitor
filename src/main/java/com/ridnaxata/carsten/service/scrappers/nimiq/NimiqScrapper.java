package com.ridnaxata.carsten.service.scrappers.nimiq;

import com.ridnaxata.carsten.model.Trx;
import com.ridnaxata.carsten.service.Scrapper;
import com.ridnaxata.carsten.service.scrappers.PageObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("nimiqScrapper")
public class NimiqScrapper implements Scrapper {

    @Autowired
    @Qualifier("nimiqPageObject")
    private PageObject page;

    public synchronized List<Trx> scrap(String walletHash, Long latestStoredBlockNumber) {
        return page.openWalletAndReadBlocks(walletHash, latestStoredBlockNumber);
    }

}
