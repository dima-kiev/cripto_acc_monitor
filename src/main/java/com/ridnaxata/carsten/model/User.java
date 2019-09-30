package com.ridnaxata.carsten.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users", uniqueConstraints = @UniqueConstraint(columnNames={"usr_name"}))
public class User {

    @Id()
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="usr_id")
    private Integer id;

    @Column(name = "usr_name")
    @NotNull
    private String name;

    @Column(name = "usr_accounts")
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Account> accounts = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public User setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public User setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        return this;
    }
}