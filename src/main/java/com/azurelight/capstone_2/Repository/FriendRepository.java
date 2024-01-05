package com.azurelight.capstone_2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.azurelight.capstone_2.db.Friend;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;

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

@Repository
public interface FriendRepository extends JpaRepository<Friend, String> {

    @Query(value = "select * from friend f where f.useremail = :me", nativeQuery = true)
    List<Friend> findByUserEmail(@Param("me") String me);

    @Query(value = "select * from friend f where f.useremail = :me and f.friendemail = :friend", nativeQuery = true)
    List<Friend> findByTwoUser(@Param("me") String me, @Param("friend") String friend);

    @Query(value = "select * from friend f where f.friendemail = :me", nativeQuery = true)
    List<Friend> findByFriendemail(@Param("me") String me);

    @Modifying
    @Transactional
    @Query(value = "delete from friend f where f.useremail = :me and f.friendemail = :friend", nativeQuery = true)
    int deleteByTwoUser(@Param("me") String me, @Param("friend") String friend);
}
