package com.ridnaxata.carsten.service.storage.repositories;

import com.ridnaxata.carsten.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

}
