package biz.nable.sb.cor.common.bean.workflow;

import biz.nable.sb.cor.common.response.workflow.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResponseBean extends CommonResponse {
	private CommonTempBean commonTempBean;
	private String tempId;
	private String workflowId;
}
