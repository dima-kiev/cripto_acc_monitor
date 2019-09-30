package com.ridnaxata.carsten.service.storage.repositories;

import com.ridnaxata.carsten.model.Trx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrxRepository extends PagingAndSortingRepository<Trx, Integer> {

    @Query("select max(blockNumber) from Trx where wallet=:walletHash")
    Long findLatestSavedBlockNumberForWallet(@Param("walletHash") String walletHash);

    @Query("select t from Trx t where t.wallet=:walletHash and t.blockTime between :fromDate and :toDate")
    List<Trx> findTrxsForWalletInPeriod(@Param("walletHash") String walletHash, @Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to);

    @Query("select sum(t.amount) from Trx t where t.wallet=:walletHash and t.trxType=:trxType and t.blockTime between :fromDate and :toDate")
    Double calcTotalForPeriod(@Param("walletHash") String walletHash, @Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to, @Param("trxType") String trxType);

    @Query(
        value = "select * from trxs where trx_wallet=:walletHash and trx_block_time between :fromDate and :toDate --#pageable\n",
        countQuery = "select count(*) from trxs where trx_wallet=:walletHash and trx_block_time between :fromDate and :toDate group by trx_id",
        nativeQuery = true)
    Page<Trx> findTrxsForWalletInPeriod(@Param("walletHash") String walletHash, @Param("fromDate") LocalDateTime from, @Param("toDate") LocalDateTime to, Pageable pageable);

    Page<Trx> findByWalletAndBlockTimeBetween(String walletHash, LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<Trx> findByWalletAndBlockTimeBetween(String walletHash, LocalDateTime from, LocalDateTime to);
}
