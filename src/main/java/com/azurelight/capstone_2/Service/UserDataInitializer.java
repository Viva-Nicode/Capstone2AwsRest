package com.azurelight.capstone_2.Service;

import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import com.azurelight.capstone_2.Repository.ChatMessageRepository;
import com.azurelight.capstone_2.Repository.ChatroomRepository;
import com.azurelight.capstone_2.Repository.ChatroomuserRepository;
import com.azurelight.capstone_2.Repository.FriendRepository;
import com.azurelight.capstone_2.Repository.ImageMessageRepository;
import com.azurelight.capstone_2.Repository.SystemMessageRepository;
import com.azurelight.capstone_2.Repository.UserRepository;
import com.azurelight.capstone_2.db.ChatMessage;
import com.azurelight.capstone_2.db.Chatroomuser;
import com.azurelight.capstone_2.db.Friend;
import com.azurelight.capstone_2.db.ImageMessage;
import java.util.Optional;
import com.azurelight.capstone_2.db.SystemMessage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;

import java.util.HashMap;

@Service
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDataInitializer {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private ChatroomuserRepository chatroomuserRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private SystemMessageRepository systemMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FCMService fs;

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private ImageMessageRepository imageMessageRepository;

    public List<HashMap<String, Object>> userFriendsFetcher(final String targetUserEmail) {
        List<HashMap<String, Object>> fetchResult = new ArrayList<HashMap<String, Object>>();
        List<Friend> friendslist = friendRepository.findByUserEmail(targetUserEmail);

        for (Friend f : friendslist) {
            if (!friendRepository.findByTwoUser(f.getFriendemail(), targetUserEmail).isEmpty()) {
                fetchResult.add(new HashMap<String, Object>(
                        Map.of("friendEmail", f.getFriendemail(),
                                "group", Optional.ofNullable(f.getFriendgroup()))));
            }
        }
        return fetchResult;
    }

    public List<HashMap<String, String>> userFriendRequestNotificationFetcher(final String targetUserEmail) {

        List<Friend> l = friendRepository.findByFriendemail(targetUserEmail);
        List<HashMap<String, String>> fetchResult = new ArrayList<HashMap<String, String>>();
        for (Friend f : l) {
            if (friendRepository.findByTwoUser(targetUserEmail, f.getUseremail()).isEmpty()) {
                fetchResult.add(new HashMap<String, String>(Map.of(
                        "notitype", "friendRequest",
                        "fromemail", f.getUseremail(),
                        "timestamp", f.getRecentTimestamAsString())));
            }
        }
        return fetchResult;
    }

    public List<HashMap<String, Object>> userMessagesFetcher(final String targetUserEmail) {
        List<HashMap<String, Object>> fetchResult = new ArrayList<HashMap<String, Object>>();
        List<Chatroomuser> chatroomuserlist = chatroomuserRepository.findByEmailOnlyTrue(targetUserEmail);
        Map<String, String> identifierToEmailConvertingTable = new HashMap<>();

        for (Chatroomuser chatroomuser : chatroomuserlist) {
            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("chatroomid", chatroomuser.getRoomid());

            List<SystemMessage> usersystemMessageOnlyEnter = systemMessageRepository
                    .findByIdentifierOnlyEnter(chatroomuser.getIdentifier());
            Date mostRecentEnterSystemLog = null;

            // allUserMessageList :
            // 채팅방 내 모든 유저들의 메시지 리스트
            // userlistInRoom :
            // 채팅방 내 모든 유저들(state고려x)의 identifier를 반복하며 메시지들을 가져와 allUserMessageList에 add한다.
            List<ChatMessage> allUserMessageList = new ArrayList<>();
            List<ImageMessage> allUserPhotoMessageList = new ArrayList<>();
            List<Chatroomuser> userlistInRoom = chatroomuserRepository.findByRoomid(chatroomuser.getRoomid());

            for (Chatroomuser cru : userlistInRoom) {
                allUserMessageList.addAll(chatMessageRepository.findByIdentifier(cru.getIdentifier()));
                allUserPhotoMessageList.addAll(imageMessageRepository.findByIdentifier(cru.getIdentifier()));
                identifierToEmailConvertingTable.put(cru.getIdentifier(), cru.getEmail());
            }

            messageInfoMap.put("audiencelist",
                    String.join(" ",
                            userlistInRoom.stream().filter(u -> u.isState()).map(Chatroomuser::getEmail).toList()));

            List<SystemMessage> allSystemMessageInroom = systemMessageRepository.findByRoomid(chatroomuser.getRoomid());

            // usersystemMessageOnlyEnter가 비어있지 않다면 가장 최근의 ENTER 시스템 메시지의 시간을 가져온다.
            if (!usersystemMessageOnlyEnter.isEmpty()) {
                Collections.sort(usersystemMessageOnlyEnter);
                mostRecentEnterSystemLog = usersystemMessageOnlyEnter.get(0).getTimestamp();
            }

            if (mostRecentEnterSystemLog != null) {
                List<ChatMessage> temp = new ArrayList<>();
                List<SystemMessage> temp2 = new ArrayList<>();
                List<ImageMessage> temp3 = new ArrayList<>();

                // 가장 최근의 ENTER보다 이후에 발생한 메시지는 temp에 추가한다.
                for (int idx = 0; idx < allUserMessageList.size(); idx++) {
                    if (allUserMessageList.get(idx).getTimestamp().compareTo(mostRecentEnterSystemLog) == 1)
                        temp.add(allUserMessageList.get(idx));
                }
                allUserMessageList = temp;

                for (int idx = 0; idx < allSystemMessageInroom.size(); idx++) {
                    if (allSystemMessageInroom.get(idx).getTimestamp().compareTo(mostRecentEnterSystemLog) == 1)
                        temp2.add(allSystemMessageInroom.get(idx));
                }
                allSystemMessageInroom = temp2;

                for (int idx = 0; idx < allUserPhotoMessageList.size(); idx++) {
                    if (allUserPhotoMessageList.get(idx).getTimestamp().compareTo(mostRecentEnterSystemLog) == 1)
                        temp3.add(allUserPhotoMessageList.get(idx));
                }
                allUserPhotoMessageList = temp3;
            }

            messageInfoMap.put("logs", new ArrayList<HashMap<String, Object>>());
            messageInfoMap.put("syslogs", new ArrayList<HashMap<String, Object>>());

            for (ChatMessage cm : allUserMessageList) {
                ((ArrayList<HashMap<String, Object>>) messageInfoMap.get("logs"))
                        .add(new HashMap<String, Object>(Map.of(
                                "messageType", "text",
                                "chatid", cm.getChatid(),
                                "writer", identifierToEmailConvertingTable.get(cm.getIdentifier()),
                                "detail", cm.getDetail(),
                                "timestamp", cm.getRecentTimestamAsString(),
                                "readusers", cm.getReadusers())));
            }

            for (SystemMessage sm : allSystemMessageInroom) {
                ((ArrayList<HashMap<String, Object>>) messageInfoMap.get("syslogs"))
                        .add(new HashMap<String, Object>(Map.of(
                                "sysid", sm.getSysid(),
                                "type", sm.getType(),
                                "timestamp", sm.getTimestamAsString(),
                                "detail", sm.getDetail())));
            }

            for (ImageMessage im : allUserPhotoMessageList) {
                ((ArrayList<HashMap<String, Object>>) messageInfoMap.get("logs"))
                        .add(new HashMap<String, Object>(Map.of(
                                "messageType", "photo",
                                "chatid", im.getImageid(),
                                "writer", identifierToEmailConvertingTable.get(im.getIdentifier()),
                                "detail", "photo",
                                "timestamp", im.getRecentTimestamAsString(),
                                "readusers", im.getReadusers())));
            }
            fetchResult.add(messageInfoMap);
        }
        return fetchResult;
    }
}
