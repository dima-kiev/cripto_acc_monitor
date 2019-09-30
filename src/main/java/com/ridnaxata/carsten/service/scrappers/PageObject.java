package com.ridnaxata.carsten.service.scrappers;

import com.ridnaxata.carsten.model.Trx;

import java.util.List;

public interface PageObject {

    List<Trx> openWalletAndReadBlocks(String walletHash, Long lastSavedBlockNumber);

}
