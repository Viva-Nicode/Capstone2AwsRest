package com.azurelight.capstone_2.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.azurelight.capstone_2.db.ChatMessage;


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

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    @Query(value = "select * from chatmessage cm where cm.identifier = :identifier", nativeQuery = true)
    List<ChatMessage> findByIdentifier(@Param("identifier") String identifier);

}
