package com.azurelight.capstone_2.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.azurelight.capstone_2.db.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    @Query(value = "SELECT * FROM chatmessage cm where cm.from_id = :fromid and cm.to_id = :toid", nativeQuery = true)
    List<ChatMessage> findByfromIdAndtoId(@Param("fromid") String from_id, @Param("toid")String to_id);
}