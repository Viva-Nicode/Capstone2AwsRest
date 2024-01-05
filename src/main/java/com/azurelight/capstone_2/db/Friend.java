package com.azurelight.capstone_2.db;

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
import java.util.Date;

// mysql> desc friend;
// +-------------+--------------+------+-----+---------+-------------------+
// | Field       | Type         | Null | Key | Default | Extra             |
// +-------------+--------------+------+-----+---------+-------------------+
// | relationid  | char(36)     | NO   | PRI | NULL    |                   |
// | useremail   | varchar(128) | YES  | MUL | NULL    |                   |
// | friendemail | varchar(128) | YES  | MUL | NULL    |                   |
// | friendgroup | varchar(64)  | YES  |     | NULL    |                   |
// | timestamp   | datetime     | YES  |     | now()   | DEFAULT_GENERATED |
// +-------------+--------------+------+-----+---------+-------------------+


@Getter
@Setter
@Entity
@Table(name = "friend")
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class Friend {

    @Id
    @Column(name = "relationid")
    private String relationid;

    @Column(name = "useremail")
    private String useremail;

    @Column(name = "friendemail")
    private String friendemail;

    @Column(name = "friendgroup")
    private String friendgroup;

    @Column(name = "timestamp")
    private Date timestamp;

    public String getRecentTimestamAsString() {
        String s = this.timestamp + "";
        return s.substring(0, s.length() - 2);
    }

    public Friend(String relationid, String useremail, String friendemail, String friendgroup) {
        this.relationid = relationid;
        this.useremail = useremail;
        this.friendemail = friendemail;
        this.friendgroup = friendgroup;
    }
}
