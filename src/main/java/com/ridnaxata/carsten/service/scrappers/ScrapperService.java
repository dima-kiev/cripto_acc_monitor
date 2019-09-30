package com.ridnaxata.carsten.service.scrappers;

import com.ridnaxata.carsten.controller.forms.WalletsToCheckForm;
import com.ridnaxata.carsten.service.TrxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScrapperService {

    @Autowired
    private List<TrxService> trxsRegistry;

    public void scrapNewTrxForWallets(WalletsToCheckForm form) {
        trxsRegistry
                .parallelStream()
                .forEach(scrapper -> form.getWallets()
                                         .entrySet()
                                         .stream()
                                         .filter(forWallet -> forWallet.getValue() != null) // getValue -> true or null(checkbox unchecked)
                                         .map(Map.Entry::getKey)
                                         .filter(walletHash -> scrapper.isApplicableFor(form.getCoins().get(walletHash)))
                                         .forEach(scrapper::getNewTrxForWalletUpToLastSavedBlockAndSave));
    }


}
