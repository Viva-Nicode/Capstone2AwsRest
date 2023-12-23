package com.azurelight.capstone_2.db;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// mysql> desc user;
// +---------------+--------------+------+-----+---------+-------------------+
// | Field         | Type         | Null | Key | Default | Extra             |
// +---------------+--------------+------+-----+---------+-------------------+
// | email         | varchar(128) | NO   | PRI | NULL    |                   |
// | password      | varchar(128) | NO   |     | NULL    |                   |
// | joindate      | datetime     | NO   |     | now()   | DEFAULT_GENERATED |
// | profile_image | varchar(64)  | YES  |     | NULL    |                   |
// | fcmtoken      | varchar(256) | NO   |     | NULL    |                   |
// +---------------+--------------+------+-----+---------+-------------------+

@Getter
@Setter
@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "joindate")
    @CreationTimestamp
    private Date joindate;

    @Column(name = "profile_image")
    private String profile_image;

    @Column(name = "fcmtoken")
    private String fcmtoken;
}
