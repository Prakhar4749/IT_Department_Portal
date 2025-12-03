package com.authService.repositories;


import com.authService.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserInfo,String> {
    Optional<UserInfo> findByEmail(String email); // Use 'email' if that is the correct field for login

}
