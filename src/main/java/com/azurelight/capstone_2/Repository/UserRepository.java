package com.azurelight.capstone_2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.azurelight.capstone_2.db.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>{

} 
