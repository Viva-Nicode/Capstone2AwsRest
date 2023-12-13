package com.azurelight.capstone_2.db;

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

// desc chatmessage
// +-------------+--------------+------+-----+---------+-------+
// | Field       | Type         | Null | Key | Default | Extra |
// +-------------+--------------+------+-----+---------+-------+
// | chat_id     | char(36)     | NO   | PRI | NULL    |       |
// | from_id     | varchar(255) | NO   | MUL | NULL    |       |
// | to_id       | varchar(255) | NO   | MUL | NULL    |       |
// | chat_detail | varchar(512) | NO   |     | NULL    |       |
// | timestamp   | datetime     | NO   |     | NULL    |       |
// +-------------+--------------+------+-----+---------+-------+

@Getter
@Setter
@Entity
@Table(name = "chatmessage")
@AllArgsConstructor
@DynamicInsert
@NoArgsConstructor
public class ChatMessage implements Comparable<ChatMessage> {
    @Id
    @Column(name = "chat_id")
    private String id;

    @Column(name = "from_id")
    private String fromId;

    @Column(name = "to_id")
    private String toId;

    @Column(name = "chat_detail")
    private String chatDetail;

    @Column(name = "timestamp")
    private Date timestamp;

    @Override
    public int compareTo(ChatMessage o) {
        return o.getTimestamp().compareTo(timestamp);
    }
}
