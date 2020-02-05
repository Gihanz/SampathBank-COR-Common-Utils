/**
 * 
 */
package biz.nable.sb.cor.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import biz.nable.sb.cor.common.utility.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
@Getter
@Setter
public class InvalidRequestException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String errorCode;

	public InvalidRequestException(String message) {
		super(message);
		this.errorCode = ErrorCode.UNKNOWN_ERROR;
	}

	public InvalidRequestException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public InvalidRequestException(String message, Throwable cause, String errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}

}
