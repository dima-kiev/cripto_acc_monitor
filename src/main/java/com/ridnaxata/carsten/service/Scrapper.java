package com.ridnaxata.carsten.service;

import com.ridnaxata.carsten.model.Trx;

import java.util.List;

public interface Scrapper {

    List<Trx> scrap(String walletHash, Long latestStoredBlockNumber);

}
