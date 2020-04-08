/*
*Copyright (c) 2019 N*Able (pvt) Ltd.
*/
package biz.nable.sb.cor.common.response.workflow;

import biz.nable.sb.cor.common.bean.workflow.AssignBean;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/*
 * @Author		:Shehan
 * @Date		:06/04/2020
 * @Description	:This response class is to provide assignments related response.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowAssignmentsResponse extends CommonResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonInclude(Include.NON_NULL)
	private List<AssignBean> workflowAssignments;

	public WorkflowAssignmentsResponse(int returnCode, String returnMessage, String errorCode, List<AssignBean> workflowAssignments) {
		super(returnCode, returnMessage, errorCode);
		this.workflowAssignments = workflowAssignments;
	}

}
