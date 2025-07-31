package id.co.msiglife.pollux.exception;

import id.co.msiglife.pollux.dto.LoginUserRequest;
import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final Integer processId;

    public NotFoundException(String message, Integer processId) {
        super(message);
        this.processId = processId;
    }
}
