package biz.nable.sb.cor.common.bean;

import biz.nable.sb.cor.common.utility.ActionTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TempBean {
	private String userId;
	private String userGroup;
	private String requestId;
	private String requestType;
	private String referenceNo;
	private ActionTypeEnum actionType;
	private Long tempId;
}
