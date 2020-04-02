/*
*Copyright (c) 2019 N*Able (pvt) Ltd.
*/
package biz.nable.sb.cor.common.response.workflow;

import biz.nable.sb.cor.common.bean.workflow.WorkflowBean;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/*
 * @Author		:Shehan
 * @Date		:02/04/2020
 * @Description	:This response class is to provide created workflow related response.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class CreateWorkflowResponse extends CommonResponse {

	private WorkflowBean workflowBean;

	public CreateWorkflowResponse(){

	}
	public CreateWorkflowResponse(int returnCode, String returnMessage, String errorCode, WorkflowBean workflowBean) {
		super(returnCode, returnMessage, errorCode);
		this.workflowBean = workflowBean;
	}

}
