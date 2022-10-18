package Backend.FinalProject.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_VALIDCONTENT(HttpStatus.NOT_FOUND,"400","유효하지 않는 내용입니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String errorMessage;
}
