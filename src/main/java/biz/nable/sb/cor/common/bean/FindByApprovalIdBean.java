package biz.nable.sb.cor.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FindByApprovalIdBean {
	private String approvalId;
	private String userId;
	private String userGroup;
}