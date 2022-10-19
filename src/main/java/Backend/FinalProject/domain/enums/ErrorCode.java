package Backend.FinalProject.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // == Application Service == //
    APPLICATION_NOTFOUND("ApplicationService submitApplication NOT FOUND", "NOT FOUND", "해당 게시글을 찾을 수 없습니다."),
    APPLICATION_EMPTY_CONTENT("ApplicationService submitApplication EMPTY", "EMPTY CONTENT", "내용을 적어주세요"),
    APPLICATION_MAX_NUM("ApplicationService submitApplication MAX NUM", "MAX NUM", "이미 정원이 다 찼습니다"),
    APPLICATION_ALREADY_SUBMIT("ApplicationService submitApplication ALREADY SUBMIT", "ALREADY SUBMIT", "이미 신청을 하셨습니다."),
    APPLICATION_INVALID_ACCESS("ApplicationService submitApplication INVALID ACCESS", "INVALID ACCESS", "모임 주최자는 신청할 수 없습니다."),
    APPLICATION_REGULATED_POST("ApplicationService submitApplication REGULATED POST", "REGULATED POST", "관리자에 의해 제재당한 게시글입니다."),

    //== ChatService ==//
    CHAT_INVALID_TOKEN("Invalid Token", "INVALID TOKEN", "유효하지 않은 토큰입니다."),
    CHAT_NO_AUTHOR("Invalid Member", "NO AUTHORIZATION", "해당 권한이 없습니다."),
    CHAT_INVALID_ROOM("Invalid RoomNumber", "INVALID ROOM NUMBER", "잘못된 방 번호입니다."),

    //== ChatRoomService ==//

    CHATROOM_NO_CHATROOM("No ChatRoom", "NO CHAT ROOM", "해당 방이 존재하지 않습니다."),
    CHATROOM_NOTFOUND("CHATROOM Not Found", "NOT FOUND", "채팅방을 찾을 수 없습니다."),
    CHATROOM_NO_CHATMEMBER("ChatRoom No Chat Member", "NO CHAT MEMBER", "채팅 멤버를 찾을 수 없습니다."),
    CHATROOM_NO_ACTIVEROOM("ChatRoom No Chat Rooms", "NO CHAT ROOMS", "아직 참여중인 모임이 존재하지 않습니다."),
















    ;
    private final String log;
    private final String Code;
    private final String message;
}
