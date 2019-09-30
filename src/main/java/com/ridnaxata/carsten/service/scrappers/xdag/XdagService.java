package com.ridnaxata.carsten.service.scrappers.xdag;

import com.ridnaxata.carsten.service.Scrapper;
import com.ridnaxata.carsten.service.scrappers.AbstractTrxService;
import com.ridnaxata.carsten.service.storage.repositories.TrxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("xdag")
public class XdagService extends AbstractTrxService {

    private static final String COIN_NAME = "xdag";
    private static final String BASE_URL = "https://explorer.xdag.io/block/";

    @Autowired
    private TrxRepository repo;

    @Autowired
    @Qualifier("xdagScrapper")
    private Scrapper scr;

    @Override
    public String createLinkTo(String trxHash) {
        return BASE_URL + trxHash;
    }

    @Override
    protected Scrapper getScrapper() {
        return scr;
    }

    @Override
    protected TrxRepository getRepo() {
        return repo;
    }

    @Override
    protected String getCoinName() {
        return COIN_NAME;
    }
}