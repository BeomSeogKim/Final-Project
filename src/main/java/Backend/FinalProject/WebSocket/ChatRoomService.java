package Backend.FinalProject.WebSocket;

import Backend.FinalProject.Tool.Validation;
import Backend.FinalProject.WebSocket.domain.ChatRoom;
import Backend.FinalProject.domain.Member;
import Backend.FinalProject.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final Validation validation;

    @Transactional
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    public ResponseDto<?> findAllRoom(HttpServletRequest request) {
        ResponseDto<?> responseDto = validation.validateCheck(request);
        if (!responseDto.isSuccess()) {
            return responseDto;
        }
        Member member = (Member) responseDto.getData();
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByMemberId(member.getId());
        if (chatRooms.isEmpty()) {
            return ResponseDto.fail("NO CHATROOMS", "현재 활성화된 채팅방이 존재하지 않습니다.");
        }
        List<ChatRoomDto> chatRoomDtoList = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            chatRoomDtoList.add(
                    ChatRoomDto.builder()
                            .id(chatRoom.getId())
                            .roomId(chatRoom.getRoomId())
                            .name(chatRoom.getName())
                            .build()
            );
        }
        return ResponseDto.success(chatRoomDtoList);
    }

    public ResponseDto<?> findById(String roomId, HttpServletRequest request) {
//        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).orElse(null);
        ResponseDto<?> responseDto = validation.validateCheck(request);

        // 방 정보 내어주자
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).orElse(null);
        if (chatRoom == null) {
            return ResponseDto.fail("NO CHAT ROOM", "해당 방이 존재하지 않습니다.");
        }

        return ResponseDto.success(ChatRoomDto.builder()
                .id(chatRoom.getId())
                .roomId(chatRoom.getRoomId())
                .name(chatRoom.getName())
                .build());

    }



}
