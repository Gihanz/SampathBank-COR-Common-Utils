package biz.nable.sb.cor.common.template;

import biz.nable.sb.cor.common.bean.ApprovalBean;
import biz.nable.sb.cor.common.response.CommonResponse;

public interface CommonApprovalTemplate {
	public CommonResponse doApprove(ApprovalBean approvalBean);
}
