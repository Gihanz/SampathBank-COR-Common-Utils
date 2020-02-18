package biz.nable.sb.cor.common.bean;

import java.util.Date;
import java.util.Map;

import biz.nable.sb.cor.common.utility.ActionTypeEnum;
import lombok.Setter;

import lombok.Getter;

@Getter
@Setter
public class TempDto {
	private Long id;
	private String signature;
	private String approvalId;
	private String requestType;
	private String referenceNo;
	private String createdBy;
	private Date createdDate;
	private String lastUpdatedBy;
	private Date lastUpdatedDate;
	private String userGroup;
	private ActionTypeEnum actionType;
	private Map<String, Object> requestPayload;
}
