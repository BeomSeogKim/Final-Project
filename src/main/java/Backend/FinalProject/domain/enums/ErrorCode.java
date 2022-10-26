package Backend.FinalProject.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // == Member Service ==//
    MEMBER_INVALID_PASSWORD("MemberService INVALID PASSWORD", "INVALID PASSWORD", "비밀번호 양식을 다시 확인해주세요"),
    MEMBER_INVALID_ID("MemberService INVALID ID","INVALID ID", "아이디 양식을 다시 확인해주세요"),
    MEMBER__NULL_DATA("MemberService createMember NULL_DATA", "NULL_DATA", "입력값을 다시 확인해주세요"),
    MEMBER_EMPTY_DATA("MemberService createMember EMPTY_DATA","EMPTY_DATA", "빈칸을 채워주세요"),
    MEMBER_DOUBLE_CHECK("MemberService createMember DOUBLE-CHECK_ERROR", "DOUBLE-CHECK_ERROR", "두 비밀번호가 일치하지 않습니다"),
    MEMBER_ALREADY_EXIST_ID("MemberService createMember ALREADY EXIST-ID", "ALREADY EXIST-ID", "이미 존재하는 아이디 입니다."),
    MEMBER_ALREADY_EXIST_NICKNAME("MemberService createMember ALREADY EXIST-NICKNAME", "ALREADY EXIST-NICKNAME", "이미 존재하는 닉네임 입니다."),
    MEMBER_REQUIRED_AGREEMENT("MemberService createMember NOT ALLOWED", "NOT ALLOWED", "이용약관을 동의해주세요"),
    MEMBER_NOT_FOUND("MemberService signoutMember NOT FOUND", "NOT FOUND", "해당 아이디를 찾을 수 없습니다. "),
    MEMBER_PASSWORD_NOT_MATCH("MemberService signoutMember PASSWORD NOT MATCH", "PASSWORD NOT MATCH", "비밀번호를 다시 확인해주세요"),
    MEMBER_DO_NOT_EXIST("MemberService login ", "INVALID ID", "존재하지 않는 아이디입니다."),
    MEMBER_REGULATED("MemberService login Regulation", "REGULATED MEMBER", "활동이 제재되었습니다. 관리자에게 문의해주세요"),

    // == Application Service == //
    APPLICATION_NOTFOUND("ApplicationService submitApplication NOT FOUND", "NOT FOUND", "해당 게시글을 찾을 수 없습니다."),
    APPLICATION_EMPTY_CONTENT("ApplicationService submitApplication EMPTY", "EMPTY CONTENT", "내용을 적어주세요"),
    APPLICATION_MAX_NUM("ApplicationService submitApplication MAX NUM", "MAX NUM", "이미 정원이 다 찼습니다"),
    APPLICATION_ALREADY_SUBMIT("ApplicationService submitApplication ALREADY SUBMIT", "ALREADY SUBMIT", "이미 신청을 하셨습니다."),
    APPLICATION_INVALID_ACCESS("ApplicationService submitApplication INVALID ACCESS", "INVALID ACCESS", "모임 주최자는 신청할 수 없습니다."),
    APPLICATION_REGULATED_POST("ApplicationService submitApplication REGULATED POST", "REGULATED POST", "관리자에 의해 제재당한 게시글입니다."),

    //== POSTService ==//
    POST_OVER_RECRUITMENT("PostService createPost MAXIMUM ERROR", "MAXIMUM ERROR", "모집 정원을 다시 확인해주세요"),
    POST_WRONG_DATE("PostService createPost WRONG DATE","WRONG DATE", "날짜 선택을 다시해주세요"),
    POST_NOT_FOUND("PostService NOT_FOUND", "NOT_FOUND", "존재하지 않는 게시글 id 입니다."),
    POST_REGULATED("PostService REGULATED POST", "REGULATED POST", "관리자에 의해 제재당한 게시글입니다."),

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
