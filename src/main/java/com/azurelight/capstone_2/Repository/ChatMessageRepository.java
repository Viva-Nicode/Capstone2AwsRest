package com.azurelight.capstone_2.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import com.azurelight.capstone_2.db.ChatMessage;

import jakarta.transaction.Transactional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    @Query(value = "SELECT * FROM chatmessage cm where cm.from_id = :fromid and cm.to_id = :toid", nativeQuery = true)
    List<ChatMessage> findByfromIdAndtoId(@Param("fromid") String from_id, @Param("toid") String to_id);

    @Query(value = "SELECT * FROM chatmessage cm WHERE cm.from_id = :id or cm.to_id = :id", nativeQuery = true)
    List<ChatMessage> findAllLogs(@Param("id") String id);

    Optional<ChatMessage> findById(String id);

    @Modifying
    @Transactional
    @Query("UPDATE chatmessage cm SET cm.isreadmsg = true where cm.chat_id = :chat_id")
    int updateIsreadMsg(@Param(value = "chat_id") String chatid);
}
