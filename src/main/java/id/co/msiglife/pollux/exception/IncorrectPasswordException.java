package id.co.msiglife.pollux.exception;

import id.co.msiglife.pollux.dto.LoginUserRequest;
import lombok.Getter;

@Getter
public class IncorrectPasswordException extends RuntimeException {
    private final Integer processId;

    public IncorrectPasswordException(String message, Integer processId) {
        super(message);
        this.processId = processId;
    }
}
