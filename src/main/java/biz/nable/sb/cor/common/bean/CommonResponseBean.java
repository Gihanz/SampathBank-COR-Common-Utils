package biz.nable.sb.cor.common.bean;

import biz.nable.sb.cor.common.response.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResponseBean extends CommonResponse {
	private CommonTempBean commonTempBean;
	private String tempId;
	private String approvalId;
}
