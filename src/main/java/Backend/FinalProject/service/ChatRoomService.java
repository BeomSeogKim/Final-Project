package Backend.FinalProject.service;

import Backend.FinalProject.domain.ChatRoom;
import Backend.FinalProject.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    public List<ChatRoom> findAllRoom() {
        List<ChatRoom> chatRoomList = chatRoomRepository.findAll();
        return chatRoomList;
    }

    public ChatRoom findById(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).orElse(null);
        return chatRoom;

    }



}
