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

// mysql> desc imagemessage;
// +------------+--------------+------+-----+---------+-------------------+
// | Field      | Type         | Null | Key | Default | Extra             |
// +------------+--------------+------+-----+---------+-------------------+
// | imageid    | char(36)     | NO   | PRI | NULL    |                   |
// | identifier | char(36)     | YES  | MUL | NULL    |                   |
// | timestamp  | datetime     | YES  |     | now()   | DEFAULT_GENERATED |
// | readusers  | varchar(512) | YES  |     | NULL    |                   |
// +------------+--------------+------+-----+---------+-------------------+

@Getter
@Setter
@Entity
@Table(name = "imagemessage")
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@NoArgsConstructor
public class ImageMessage implements Comparable<ImageMessage> {

    @Id
    @Column(name = "imageid")
    private String imageid;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "timestamp")
    private Date timestamp;

    @Column(name = "readusers")
    private String readusers;

    @Override
    public int compareTo(ImageMessage o) {
        return o.getTimestamp().compareTo(timestamp);
    }

    public String getRecentTimestamAsString() {
        String s = this.timestamp + "";
        return s.substring(0, s.length() - 2);
    }

    public ImageMessage(String imageid, String identifier, String readusers) {
        this.imageid = imageid;
        this.identifier = identifier;
        this.readusers = readusers;
    }
}
