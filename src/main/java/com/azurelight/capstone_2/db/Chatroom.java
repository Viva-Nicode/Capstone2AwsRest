package com.azurelight.capstone_2.db;

import java.util.Date;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// mysql> desc chatroom;
// +------------------+--------------+------+-----+---------+-------------------+
// | Field            | Type         | Null | Key | Default | Extra             |
// +------------------+--------------+------+-----+---------+-------------------+
// | roomid           | char(36)     | NO   | PRI | NULL    |                   |
// | usercount        | int          | NO   |     | NULL    |                   |
// | recent_detail    | varchar(512) | NO   |     | NULL    |                   |
// | recent_timestamp | datetime     | NO   |     | now()   | DEFAULT_GENERATED |
// +------------------+--------------+------+-----+---------+-------------------+

@Getter
@Setter
@Entity
@Table(name = "chatroom")
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class Chatroom {

    @Id
    @Column(name = "roomid")
    private String roomid;

    @Column(name = "usercount")
    private int usercount;

    @Column(name = "recent_detail")
    private String recent_detail;

    @Column(name = "recent_timestamp")
    private Date recent_timestamp;

    public String getRecentTimestamAsString() {
        String s = this.recent_timestamp + "";
        return s.substring(0, s.length() - 2);
    }

    public Chatroom(String roomid, int usercount, String recent_detail) {
        this.roomid = roomid;
        this.usercount = usercount;
        this.recent_detail = recent_detail;
    }
}
