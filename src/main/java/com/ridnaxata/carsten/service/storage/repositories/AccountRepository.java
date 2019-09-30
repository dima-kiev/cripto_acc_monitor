package com.ridnaxata.carsten.service.storage.repositories;

import com.ridnaxata.carsten.model.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account, Integer> {

}

