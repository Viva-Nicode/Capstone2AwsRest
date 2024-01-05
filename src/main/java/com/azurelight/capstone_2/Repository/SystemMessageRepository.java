package com.azurelight.capstone_2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.azurelight.capstone_2.db.SystemMessage;

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

@Repository
public interface SystemMessageRepository extends JpaRepository<SystemMessage, String> {

    @Query(value = "select * from systemmessage sm where sm.identifier = :identifier and sm.type = \"ENTER\"", nativeQuery = true)
    List<SystemMessage> findByIdentifierOnlyEnter(@Param("identifier") String identifier);

    @Query(value = "select * from systemmessage sm where sm.roomid = :roomid", nativeQuery = true)
    List<SystemMessage> findByRoomid(@Param("roomid") String roomid);
}
