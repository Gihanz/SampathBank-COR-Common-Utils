package biz.nable.sb.cor.common.bean.workflow;

import biz.nable.sb.cor.common.response.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResponseWorkflowBean extends CommonResponse {
	private CommonTempWorkflowBean commonTempWorkflowBean;
	private String tempId;
	private String workflowId;
}
