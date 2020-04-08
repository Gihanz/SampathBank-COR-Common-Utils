package biz.nable.sb.cor.common.bean.workflow;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class CommonRequestWorkflowBean {

	private CommonTempWorkflowBean commonTempWorkflowBean;
	private String userId;
	private String referenceId;
	private String companyId;
	private String type;
	private String subType;
	private String domain;
	private BigDecimal amount;
	private String modifiedBy;
	private Date modifiedDate;
	private String status;
	private String remarks;
	private String dbaccount;

	private String hashTags;
	private String tempId;
}
