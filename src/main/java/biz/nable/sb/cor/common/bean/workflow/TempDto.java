package biz.nable.sb.cor.common.bean.workflow;

import biz.nable.sb.cor.common.utility.ActionTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class TempDto {
	private Long id;
	private String signature;
	private String workflowId;
	private String companyId;
	private String type;
	private String referenceNo;
	private String lastUpdatedBy;
	private Date lastUpdatedDate;
	private Map<String, Object> requestPayload;
}
