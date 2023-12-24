package com.azurelight.capstone_2.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.azurelight.capstone_2.db.Chatroom;

import jakarta.transaction.Transactional;

// mysql> desc chatroom;
// +------------------+--------------+------+-----+---------+-------------------+
// | Field            | Type         | Null | Key | Default | Extra             |
// +------------------+--------------+------+-----+---------+-------------------+
// | roomid           | char(36)     | NO   | PRI | NULL    |                   |
// | usercount        | int          | NO   |     | NULL    |                   |
// | recent_detail    | varchar(512) | NO   |     | NULL    |                   |
// | recent_timestamp | datetime     | NO   |     | now()   | DEFAULT_GENERATED |
// +------------------+--------------+------+-----+---------+-------------------+

@Repository
public interface ChatroomRepository extends JpaRepository<Chatroom, String>{
    @Modifying
    @Transactional
    @Query(value = "update chatroom cr set cr.recent_detail = :detail, cr.recent_timestamp = now() where cr.roomid = :roomid", nativeQuery = true)
    int updateRecentInfo(@Param("roomid") String roomid, @Param("detail") String detail);
}
