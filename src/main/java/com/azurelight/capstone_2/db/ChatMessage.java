package com.azurelight.capstone_2.db;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

// mysql> desc chatmessage;
// +-------------+--------------+------+-----+---------+-------------------+
// | Field       | Type         | Null | Key | Default | Extra             |
// +-------------+--------------+------+-----+---------+-------------------+
// | chatid      | char(36)     | NO   | PRI | NULL    |                   |
// | identifier  | char(36)     | NO   | MUL | NULL    |                   |
// | detail      | varchar(512) | NO   |     | NULL    |                   |
// | timestamp   | datetime     | NO   |     | now()   | DEFAULT_GENERATED |
// | unreadcount | int          | NO   |     | NULL    |                   |
// +-------------+--------------+------+-----+---------+-------------------+

@Getter
@Setter
@Entity
@Table(name = "chatmessage")
@AllArgsConstructor
@DynamicInsert
@NoArgsConstructor
public class ChatMessage implements Comparable<ChatMessage> {
    @Id
    @Column(name = "chatid")
    private String chatid;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "detail")
    private String detail;

    @Column(name = "timestamp")
    private Date timestamp;

    @Column(name = "unreadcount")
    private int unreadcount;

    @Override
    public int compareTo(ChatMessage o) {
        return o.getTimestamp().compareTo(timestamp);
    }

    public String getRecentTimestamAsString() {
        String s = this.timestamp + "";
        return s.substring(0, s.length() - 2);
    }
}
