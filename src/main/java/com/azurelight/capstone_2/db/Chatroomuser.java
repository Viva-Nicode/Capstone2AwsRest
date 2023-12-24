package com.azurelight.capstone_2.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// mysql> desc chatroomuser;
// +------------+--------------+------+-----+---------+-------------------+
// | Field      | Type         | Null | Key | Default | Extra             |
// +------------+--------------+------+-----+---------+-------------------+
// | identifier | char(36)     | NO   | PRI | NULL    |                   |
// | roomid     | char(36)     | NO   | MUL | NULL    |                   |
// | email      | varchar(128) | NO   | MUL | NULL    |                   |
// | nickname   | varchar(32)  | YES  |     | NULL    |                   |
// | state      | tinyint(1)   | NO   |     | true    | DEFAULT_GENERATED |
// +------------+--------------+------+-----+---------+-------------------+


@Getter
@Setter
@Entity
@Table(name = "chatroomuser")
@AllArgsConstructor
@NoArgsConstructor
public class Chatroomuser {

    @Id
    @Column(name = "identifier")
    private String identifier;

    @Column(name = "roomid")
    private String roomid;

    @Column(name = "email")
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "state")
    private boolean state;

}
