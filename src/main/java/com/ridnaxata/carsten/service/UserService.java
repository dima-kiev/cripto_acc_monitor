package com.ridnaxata.carsten.service;

import com.ridnaxata.carsten.model.User;
import com.ridnaxata.carsten.service.storage.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    public User getUserById(Integer userId) throws Exception {
        Optional<User> found = repo.findById(userId);
        if (found.isPresent()) {
            return found.get();
        }
        // todo create special exception (UserNotFoundException) to handle in controller for err pages
        throw new Exception("Such user not found, id: " + userId); // todo create special exception (UserNotFoundException) to handle in controller for err pages
    }


}
