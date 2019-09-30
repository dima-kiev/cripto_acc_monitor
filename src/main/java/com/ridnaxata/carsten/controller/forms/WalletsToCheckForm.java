package com.ridnaxata.carsten.controller.forms;

import com.ridnaxata.carsten.model.Account;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletsToCheckForm {
    private Map<String, Boolean> wallets = new HashMap<>(); // NOTE: Boolean -> true or null !!! (if checkbox don`t checked)
    private Map<String, String> coins = new HashMap<>();
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fromDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime  toDate;

    public WalletsToCheckForm(List<Account> accounts) {
        accounts.forEach(account -> {
            wallets.put(account.getWalletHash(), true);
            coins.put(account.getWalletHash(), account.getCoinName());
        });
        initDates();
    }

    public WalletsToCheckForm() {
        initDates();
    }

    private void initDates() {
        String from = "2010-01-01 00:01";
        String to   = "2029-12-31 11:59";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        fromDate = LocalDateTime.parse(from, formatter);
        toDate = LocalDateTime.parse(to, formatter);
    }

    public Map<String, Boolean> getWallets() {
        return wallets;
    }

    public WalletsToCheckForm setWallets(Map<String, Boolean> wallets) {
        this.wallets = wallets;
        return this;
    }

    public Map<String, String> getCoins() {
        return coins;
    }

    public WalletsToCheckForm setCoins(Map<String, String> coins) {
        this.coins = coins;
        return this;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public WalletsToCheckForm setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public WalletsToCheckForm setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
        return this;
    }
}
