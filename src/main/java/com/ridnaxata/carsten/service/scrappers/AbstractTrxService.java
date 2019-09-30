package com.ridnaxata.carsten.service.scrappers;

import com.ridnaxata.carsten.model.ByDaySummaryDTO;
import com.ridnaxata.carsten.model.Trx;
import com.ridnaxata.carsten.service.Scrapper;
import com.ridnaxata.carsten.service.TrxService;
import com.ridnaxata.carsten.service.storage.repositories.TrxRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AbstractTrxService implements TrxService {

    protected abstract Scrapper getScrapper();
    protected abstract TrxRepository getRepo();
    protected abstract String getCoinName();

    @Override
    public boolean isApplicableFor(@NotNull String coinName) {
        return getCoinName().equals(coinName.toLowerCase());
    }

    @Override
    public void getNewTrxForWalletUpToLastSavedBlockAndSave(String walletHash) {
        Long lastBlockNumber = latestSavedBlockNumberForWallet(walletHash);
        List<Trx> scrapedBlocks = getScrapper().scrap(walletHash, lastBlockNumber);
        sortAndSave(scrapedBlocks);
    }

    private void sortAndSave(List<Trx> trxs) {
        List<Trx> fromSmallToBigBlockNumbers = trxs.stream()
                .sorted(Comparator.comparingLong(Trx::getBlockNumber))
                .collect(Collectors.toList());
        getRepo().saveAll(fromSmallToBigBlockNumbers);
    }

    @Override
    public Long latestSavedBlockNumberForWallet(String walletHash) {
        Long lastSavedBlockNumber = getRepo().findLatestSavedBlockNumberForWallet(walletHash);
        return lastSavedBlockNumber == null ? 0L : lastSavedBlockNumber;
    }

    @Override
    public List<Trx> readForWalletInPeriod(String walletHash, LocalDateTime from, LocalDateTime to) {
        return getRepo().findTrxsForWalletInPeriod(walletHash, from, to);
    }

    @Override
    public Page<Trx> readForWalletInPeriod(String walletHash, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return getRepo().findByWalletAndBlockTimeBetween(walletHash, from, to, pageable);
    }

    @Override
    public Page<ByDaySummaryDTO> readForWalletInPeriodByDays(String walletHash, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented yet");
        //List<ByDaySummaryDTO> dtos = readForWalletInPeriodByDays(walletHash, from, to);
    }

    @Override
    public List<ByDaySummaryDTO> readForWalletInPeriodByDays(String walletHash, LocalDateTime from, LocalDateTime to) {
        return getRepo()
                .findByWalletAndBlockTimeBetween(walletHash, from, to)
                .stream()
                .collect(TrxByDayCollector::new, TrxByDayCollector::accept, TrxByDayCollector::combine)
                    .getGroupedByDay()
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparing(Map.Entry::getKey))
                        .map(ByDaySummaryDTO::new)
                        .collect(Collectors.toList());
    }

    private class TrxByDayCollector {

        private Map<LocalDate, List<Trx>> groupedByDay = new ConcurrentHashMap<>();

        Map<LocalDate, List<Trx>> getGroupedByDay() {
            return groupedByDay;
        }

        void accept(Trx trx) {
            groupedByDay
                    .computeIfAbsent(trx.getBlockTime().toLocalDate(), d -> new ArrayList<>())
                    .add(trx);
        }

        void combine(TrxByDayCollector another) {
            another.getGroupedByDay()
                        .entrySet()
                        .forEach(entry -> { groupedByDay
                                                .merge(entry.getKey(),
                                                       entry.getValue(),
                                                       (v1, v2) -> { Set<Trx> set = new TreeSet<>(v1);
                                                                     set.addAll(v2);
                                                                     return new ArrayList<>(set);
                                                        });
                        });
        }
    }

    @Override
    public Page<Trx> findAll(Pageable pageable) {
        return getRepo().findAll(pageable);
    }

    @Override
    public Double calcTotalForWalletInPeriod(String walletHash, LocalDateTime from, LocalDateTime to) {
        Double blockDebet  = getRepo().calcTotalForPeriod(walletHash, from, to, "block");
        if (blockDebet == null) blockDebet = 0.0;

        Double debet  = getRepo().calcTotalForPeriod(walletHash, from, to, "output");
        if (debet == null) debet = 0.0;

        Double credit = getRepo().calcTotalForPeriod(walletHash, from, to, "input");
        if (credit == null) credit = 0.0;

        return blockDebet + debet - credit;
    }


}
