package com.ridnaxata.carsten.service.scrappers.veo;

import com.ridnaxata.carsten.service.Scrapper;
import com.ridnaxata.carsten.service.scrappers.AbstractTrxService;
import com.ridnaxata.carsten.service.storage.repositories.TrxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("veo")
public class VeoService extends AbstractTrxService {

    private static final String COIN_NAME = "veo";
    private static final String BASE_URL = "https://veoscan.io/tx/";

    @Autowired
    private TrxRepository repo;

    @Autowired
    @Qualifier("veoScrapper")
    private Scrapper scr;

    @Override
    public String createLinkTo(String trxHash) {
        return BASE_URL + trxHash + "/1";
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
