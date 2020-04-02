/*
*Copyright (c) 2019 N*Able (pvt) Ltd.
*/
package biz.nable.sb.cor.common.response.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

/*
 * @Author		:Shehan
 * @Date		:03/04/2020
 * @Description	:This response class is to provide common parameters.
 */

@Getter
@Setter
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse {

	private int returnCode;

	private String returnMessage;

	@JsonInclude(Include.NON_NULL)
	private String errorCode;

	@JsonInclude(Include.NON_NULL)
	private String errorMessage;

}
