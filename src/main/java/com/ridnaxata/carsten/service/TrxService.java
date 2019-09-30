package com.ridnaxata.carsten.service;

import com.ridnaxata.carsten.model.ByDaySummaryDTO;
import com.ridnaxata.carsten.model.Trx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public interface TrxService {

    boolean isApplicableFor(@NotNull String coinName);

    String createLinkTo(String trxHash);

    void getNewTrxForWalletUpToLastSavedBlockAndSave(String walletHash);
    Long latestSavedBlockNumberForWallet(String walletHash);

    Double calcTotalForWalletInPeriod(String walletHash, LocalDateTime from, LocalDateTime to);

    Page<Trx> findAll(Pageable pageable);

    List<Trx> readForWalletInPeriod(String walletHash, LocalDateTime from, LocalDateTime to);
    Page<Trx> readForWalletInPeriod(String walletHash, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<ByDaySummaryDTO> readForWalletInPeriodByDays(String walletHash, LocalDateTime from, LocalDateTime to, Pageable pageable);
    List<ByDaySummaryDTO> readForWalletInPeriodByDays(String walletHash, LocalDateTime from, LocalDateTime to);

}
