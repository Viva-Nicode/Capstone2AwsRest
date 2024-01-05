package com.azurelight.capstone_2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.azurelight.capstone_2.db.Chatroomuser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;

// mysql> desc chatroomuser;
// +------------+--------------+------+-----+---------+-------------------+
// | Field      | Type         | Null | Key | Default | Extra             |
// +------------+--------------+------+-----+---------+-------------------+
// | identifier | char(36)     | NO   | PRI | NULL    |                   |
// | roomid     | char(36)     | NO   | MUL | NULL    |                   |
// | email      | varchar(128) | NO   | MUL | NULL    |                   |
// | nickname   | varchar(32)  | YES  |     | NULL    |                   |
// | state      | tinyint(1)   | NO   |     | true    | DEFAULT_GENERATED |
// +------------+--------------+------+-----+---------+-------------------+

@Repository
public interface ChatroomuserRepository extends JpaRepository<Chatroomuser, String> {

    @Query(value = "select roomid from chatroomuser cru where cru.email = :email and cru.state = true", nativeQuery = true)
    List<String> findRoomidByEmail(@Param("email") String email);

    @Query(value = "select * from chatroomuser cru where cru.email = :email and cru.state = true", nativeQuery = true)
    List<Chatroomuser> findByEmailOnlyTrue(@Param("email") String email);

    @Query(value = "select * from chatroomuser cru where cru.roomid = :roomid", nativeQuery = true)
    List<Chatroomuser> findByRoomid(@Param("roomid") String roomid);

    @Query(value = "select * from chatroomuser cru where cru.roomid = :roomid and cru.state = true", nativeQuery = true)
    List<Chatroomuser> findByRoomidOnlyTrue(@Param("roomid") String roomid);

    @Query(value = "select identifier from chatroomuser cru where cru.roomid = :roomid and cru.email = :email", nativeQuery = true)
    List<String> findIdentifierByRoomidAndEmail(@Param("roomid") String roomid, @Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "update chatroomuser cru set cru.state = true where cru.roomid = :roomid and cru.email = :email", nativeQuery = true)
    int updateChatroomuserState(@Param("roomid") String roomid, @Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "update chatroomuser cru set cru.state = false where cru.roomid = :roomid and cru.email = :email", nativeQuery = true)
    int updateChatroomuserStateToFalse(@Param("roomid") String roomid, @Param("email") String email);
}
