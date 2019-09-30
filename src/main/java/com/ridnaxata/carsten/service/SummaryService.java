package com.ridnaxata.carsten.service;

import com.ridnaxata.carsten.controller.forms.WalletsToCheckForm;
import com.ridnaxata.carsten.model.ByDaySummaryDTO;
import com.ridnaxata.carsten.model.Trx;
import com.ridnaxata.carsten.model.WalletSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    @Autowired
    private List<TrxService> coinServicesRegistry;

    public List<WalletSummary> summarize(WalletsToCheckForm wallets) {

        List<String> hashes = wallets.getWallets()
                                     .entrySet()
                                     .stream()
                                     .filter(forWallet -> forWallet.getValue() != null)
                                     .map(Map.Entry::getKey)
                                     .collect(Collectors.toList());

        return hashes
                .stream()
                .map(walletHash -> WalletSummary.getBuilder()
                                          .setWalletDataAndTimePeriod(walletHash, wallets)
                                          .setTotalForPeriod(calcTotalForPeriodFromDB(wallets.getCoins().get(walletHash), walletHash, wallets.getFromDate(), wallets.getToDate()))
                                          .build())
                .collect(Collectors.toList());
    }

    private Double calcTotalForPeriodFromDB(String coinName, String walletHash, LocalDateTime from, LocalDateTime to) {
        return coinServicesRegistry
                    .stream()
                    .filter(coinService -> coinService.isApplicableFor(coinName))
                    .findFirst()
                    .map(coinService -> coinService.calcTotalForWalletInPeriod(walletHash, from, to))
                    .orElse(0.0);
    }

    private List<Trx> readFromDB(String coinName, String walletHash, LocalDateTime from, LocalDateTime to) {
        return coinServicesRegistry
                    .stream()
                    .filter(coinService -> coinService.isApplicableFor(coinName)) //groovy sugar: coinName.@it.isApplicableFor
                    .flatMap(coinService -> coinService.readForWalletInPeriod(walletHash, from, to).stream())
                    .collect(Collectors.toList());
    }

    public Page<Trx> readForWalletInPeriod(Pageable pageable, String coinName, String walletHash, LocalDateTime from, LocalDateTime to) {
        return coinServicesRegistry
                    .stream()
                    .filter(coinService -> coinService.isApplicableFor(coinName))
                    .findFirst()
                    .map(coinService -> coinService.readForWalletInPeriod(walletHash, from, to, pageable))
                    .get();
    }

    public Map<String, String> createLinksForTrx(String coinName, Page<Trx> page) {
        TrxService service = coinServicesRegistry.stream()
                                                 .filter(coinService -> coinService.isApplicableFor(coinName))
                                                 .findFirst()
                                                 .get(); // todo ? explore is needed here or not orElseThrow
        return page.getContent()
                    .stream()
                    .map(Trx::getTrxHash)
                    .collect(Collectors.toMap(trxHash -> trxHash, service::createLinkTo, (k1, k2) -> k1));
    }

    public List<ByDaySummaryDTO> readForWalletInPeriodByDays(String coinName, String walletHash, LocalDateTime from, LocalDateTime to) {
        return coinServicesRegistry
                .stream()
                .filter(coinService -> coinService.isApplicableFor(coinName))
                .findFirst()
                .map(coinService -> coinService.readForWalletInPeriodByDays(walletHash, from, to))
                .get();
    }
}
