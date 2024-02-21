package com.azurelight.capstone_2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.azurelight.capstone_2.db.ChatMessage;
import com.azurelight.capstone_2.db.ImageMessage;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import jakarta.transaction.Transactional;

@Repository
public interface ImageMessageRepository extends JpaRepository<ImageMessage, String> {
    
    @Query(value = "select * from imagemessage im where im.identifier = :identifier", nativeQuery = true)
    List<ImageMessage> findByIdentifier(@Param("identifier") String identifier);

    @Modifying
    @Transactional
    @Query(value = "update imagemessage im set im.readusers = :readusers where im.imageid = :chatid", nativeQuery = true)
    int updateReadusersByChatid(@Param("chatid") String chatid, @Param("readusers") String readusers);
}
