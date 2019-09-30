package com.ridnaxata.carsten.service.scrappers.snow;

import com.ridnaxata.carsten.service.Scrapper;
import com.ridnaxata.carsten.service.scrappers.AbstractTrxService;
import com.ridnaxata.carsten.service.storage.repositories.TrxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("snow")
public class TrxSnowService extends AbstractTrxService {

    private static final String COIN_NAME = "snow";
    private static final String BASE_URL = "https://explorer.snowblossom.org/?search=";

    @Autowired
    private TrxRepository repo;

    @Autowired
    @Qualifier("snowScrapper")
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
