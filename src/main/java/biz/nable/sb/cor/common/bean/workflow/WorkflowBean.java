/*
*Copyright (c) 2019 N*Able (pvt) Ltd.
*/
package biz.nable.sb.cor.common.bean.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/*
 * @Author		:shehan_inova
 * @Date		:02/04/2020
 * @Description	:This bean class is to send input parameters to the API.
 */

@Getter
@Setter
@ToString
public class WorkflowBean {

	@JsonInclude(Include.NON_NULL)
	private String workflowId;

	private String referenceId;

	private String companyId;

	private String type;

	private String action;

	private String subType;

	private String domain;

	private BigDecimal amount;

	@JsonInclude(Include.NON_NULL)
	private String createdBy;

	@JsonInclude(Include.NON_NULL)
	private Date createdDate;

	@JsonInclude(Include.NON_NULL)
	private String modifiedBy;

	@JsonInclude(Include.NON_NULL)
	private Date modifiedDate;

	@JsonInclude(Include.NON_NULL)
	private String status;

	@JsonInclude(Include.NON_NULL)
	private String remarks;

	private String dbaccount;
}
