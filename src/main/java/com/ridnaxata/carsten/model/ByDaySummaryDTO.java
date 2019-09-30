package com.ridnaxata.carsten.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ByDaySummaryDTO {

    private String wallet;
    private LocalDate date;
    private Double amount;

    public ByDaySummaryDTO(Map.Entry<LocalDate, List<Trx>> trxsByDay) {
        this.date = trxsByDay.getKey();
        this.wallet = trxsByDay.getValue().get(0).getWallet();

        Double dayBlDeb = extract("block", trxsByDay.getValue());
        Double dayDbt = extract("output", trxsByDay.getValue());
        Double dayCred = extract("input", trxsByDay.getValue());
        this.amount = dayBlDeb + dayDbt - dayCred;
    }

    private Double extract(String trxType, List<Trx> trxs) {
        return trxs
                .stream()
                .filter(trx -> trx.getTrxType().equals(trxType))
                .mapToDouble(Trx::getAmount)
                .sum();
    }

    public String getWallet() {
        return wallet;
    }

    public LocalDate getDate() {
        return date;
    }

    public Double getAmount() {
        return amount;
    }

    public String toCsvRecord() {
        return wallet + ", " + date + ", " + String.format("%.10f", amount) + ",\n";
    }

}
