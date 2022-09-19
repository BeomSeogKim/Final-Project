package Backend.FinalProject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private boolean success;
    private T data;

    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(true, data);
    }

    public static <T> ResponseDto<T> fail(T data) {
        return new ResponseDto<>(false, data);
    }

    @Getter
    @AllArgsConstructor
    static class Error {
        private String code;
        private String message;
    }

}
