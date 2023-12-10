package com.azurelight.capstone_2.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.azurelight.capstone_2.db.User;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, String>{
    List<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.profile_image_path = :newprofile where u.email = :email")
    int updateUserprofile(@Param(value="newprofile") String newprofile, @Param(value="email") String email);
} 
