package com.ridnaxata.carsten.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

// TODO explore the queies (TrxRepository) and create the indexes.
// previosly: (!!!)byWalletAndByCoinAndByDate, byWallet, byWalletAndByCoin
@Entity
@Table(name="trxs")
public class Trx {

    @Id()
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="trx_id")
    private Integer id;

    @Column(name="trx_wallet")
    private String wallet;

    @Column(name="trx_block_number")
    private Long blockNumber;

    @Column(name="trx_trx_hash")
    private String trxHash;

    @Column(name="trx_block_time")
    private LocalDateTime blockTime;

    @Column(name="trx_trx_type")
    private String trxType;

    @Column(name="trx_amount")
    private Double amount;

    public Trx() {
    }

    public Integer getId() {
        return id;
    }

    public Trx setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getWallet() {
        return wallet;
    }

    public Trx setWallet(String wallet) {
        this.wallet = wallet;
        return this;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public Trx setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
        return this;
    }

    public LocalDateTime getBlockTime() {
        return blockTime;
    }

    public Trx setBlockTime(LocalDateTime blockTime) {
        this.blockTime = blockTime;
        return this;
    }

    public String getTrxType() {
        return trxType;
    }

    public Trx setTrxType(String trxType) {
        this.trxType = trxType;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public Trx setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public String getTrxHash() {
        return trxHash;
    }

    public Trx setTrxHash(String trxHash) {
        this.trxHash = trxHash;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trx trx = (Trx) o;
        return Objects.equals(wallet, trx.wallet) &&
                Objects.equals(blockNumber, trx.blockNumber) &&
                Objects.equals(trxHash, trx.trxHash) &&
                Objects.equals(blockTime, trx.blockTime) &&
                Objects.equals(trxType, trx.trxType) &&
                Objects.equals(amount, trx.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wallet, blockNumber, trxHash, blockTime, trxType, amount);
    }

    @Override
    public String toString() {
        return "Trx{" +
                "id=" + id +
                ", wallet='" + wallet + '\'' +
                ", blockNumber=" + blockNumber +
                ", trxHash='" + trxHash + '\'' +
                ", blockTime=" + blockTime +
                ", trxType='" + trxType + '\'' +
                ", amount=" + amount +
                '}';
    }
}
