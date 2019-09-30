package com.ridnaxata.carsten.model;

import javax.persistence.*;

@Entity
@Table(name="accounts", uniqueConstraints = @UniqueConstraint(columnNames={"acc_wallet_hash"}))
public class Account {

    @Id()
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="acc_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "acc_usr_id")
    private User user;

    @Column(name = "acc_coin_name")
    private String coinName;

    @Column(name = "acc_wallet_hash")
    private String walletHash;

    public Integer getId() {
        return id;
    }

    public Account setId(Integer id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Account setUser(User user) {
        this.user = user;
        return this;
    }

    public String getCoinName() {
        return coinName;
    }

    public Account setCoinName(String coinName) {
        this.coinName = coinName;
        return this;
    }

    public String getWalletHash() {
        return walletHash;
    }

    public Account setWalletHash(String walletHash) {
        this.walletHash = walletHash;
        return this;
    }
}
