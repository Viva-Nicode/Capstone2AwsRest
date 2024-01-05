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
import java.util.Date;
import lombok.Setter;

// mysql> desc systemmessage;
// +------------+--------------+------+-----+---------+-------------------+
// | Field      | Type         | Null | Key | Default | Extra             |
// +------------+--------------+------+-----+---------+-------------------+
// | sysid      | char(36)     | NO   | PRI | NULL    |                   |
// | roomid     | char(36)     | YES  | MUL | NULL    |                   |
// | timestamp  | datetime     | YES  |     | now()   | DEFAULT_GENERATED |
// | detail     | varchar(512) | NO   |     | NULL    |                   |
// | identifier | char(36)     | YES  | MUL | NULL    |                   |
// | type       | char(16)     | NO   |     | NULL    |                   |
// +------------+--------------+------+-----+---------+-------------------+

@Getter
@Setter
@Entity
@Table(name = "systemmessage")
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class SystemMessage implements Comparable<SystemMessage> {

    @Id
    @Column(name = "sysid")
    private String sysid;

    @Column(name = "roomid")
    private String roomid;

    @Column(name = "timestamp")
    private Date timestamp;

    @Column(name = "detail")
    private String detail;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "type")
    private String type;

    @Override
    public int compareTo(SystemMessage o) {
        return o.getTimestamp().compareTo(timestamp);
    }

    public String getTimestamAsString() {
        String s = this.timestamp + "";
        return s.substring(0, s.length() - 2);
    }

    public SystemMessage(String sysid, String roomid, String detail, String identifier, String type) {
        this.sysid = sysid;
        this.roomid = roomid;
        this.detail = detail;
        this.identifier = identifier;
        this.type = type;
    }

}