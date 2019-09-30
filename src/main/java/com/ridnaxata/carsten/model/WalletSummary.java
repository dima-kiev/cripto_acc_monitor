package com.ridnaxata.carsten.model;

import com.ridnaxata.carsten.controller.forms.WalletsToCheckForm;

import java.time.format.DateTimeFormatter;

public class WalletSummary {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    private String coinName;
    private String walletHash;
    //private LocalDateTime from;
    private String from;
    //private LocalDateTime to;
    private String to;
    private Double totalForPeriod;

    public String getCoinName() {
        return coinName;
    }

    public String getWalletHash() {
        return walletHash;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Double getTotalForPeriod() {
        return totalForPeriod;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private WalletSummary walletSummary = new WalletSummary();

        private Builder() {
        }

        public Builder setWalletDataAndTimePeriod(String walletHash, WalletsToCheckForm form) {
            walletSummary.walletHash = walletHash;
            //walletSummary.from = form.getFromDate();
            walletSummary.from = form.getFromDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
            //walletSummary.to = form.getToDate();
            walletSummary.to = form.getToDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
            walletSummary.coinName = form.getCoins().getOrDefault(walletHash, "WRONG DATA");
            return this;
        }

        public Builder setTotalForPeriod(Double total) {
            walletSummary.totalForPeriod = total;
            return this;
        }

        public WalletSummary build() {
            assert !"WRONG DATA".equals(walletSummary.coinName);
            assert walletSummary.walletHash != null;
            assert walletSummary.totalForPeriod != null;
            return walletSummary;
        }

    }

}

