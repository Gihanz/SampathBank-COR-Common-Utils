package biz.nable.sb.cor.common.bean.workflow;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResponseBean {
	private CommonTempBean commonTempBean;
	private String tempId;
	private String workflowId;
}
