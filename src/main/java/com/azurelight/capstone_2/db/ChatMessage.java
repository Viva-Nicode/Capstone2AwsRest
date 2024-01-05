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

// mysql> desc chatmessage;
// +------------+--------------+------+-----+---------+-------------------+
// | Field      | Type         | Null | Key | Default | Extra             |
// +------------+--------------+------+-----+---------+-------------------+
// | chatid     | char(36)     | NO   | PRI | NULL    |                   |
// | identifier | char(36)     | NO   | MUL | NULL    |                   |
// | detail     | varchar(512) | NO   |     | NULL    |                   |
// | timestamp  | datetime     | YES  |     | now()   | DEFAULT_GENERATED |
// | readusers  | varchar(512) | NO   |     | NULL    |                   |
// +------------+--------------+------+-----+---------+-------------------+

@Getter
@Setter
@Entity
@Table(name = "chatmessage")
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
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

    @Column(name = "readusers")
    private String readusers;

    @Override
    public int compareTo(ChatMessage o) {
        return o.getTimestamp().compareTo(timestamp);
    }

    public String getRecentTimestamAsString() {
        String s = this.timestamp + "";
        return s.substring(0, s.length() - 2);
    }

    public ChatMessage(String chatid, String identifier, String detail, String readusers) {
        this.chatid = chatid;
        this.identifier = identifier;
        this.detail = detail;
        this.readusers = readusers;
    }

}
