/**
 * 
 */
package biz.nable.sb.cor.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;
import lombok.Setter;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
@Getter
@Setter
public class SystemException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String errorCode;

	public SystemException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public SystemException(String message, Throwable cause, String errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}

}
