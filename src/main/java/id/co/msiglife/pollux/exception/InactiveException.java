package id.co.msiglife.pollux.exception;

import id.co.msiglife.pollux.dto.LoginUserRequest;
import lombok.Getter;

@Getter
public class InactiveException extends RuntimeException {
  private final Integer processId;

  public InactiveException(String message, Integer processId) {
    super(message);
    this.processId = processId;
  }
}
