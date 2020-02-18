package biz.nable.sb.cor.common.bean;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ApprovalBean {

	@JsonInclude(Include.NON_NULL)
	private String referenceId;

	@JsonInclude(Include.NON_NULL)
	private String type;

	private String verifiedBy;

	@JsonInclude(Include.NON_NULL)
	private String approvalId;

	@JsonInclude(Include.NON_NULL)
	private String approvalStatus;

	@JsonInclude(Include.NON_NULL)
	private Date enteredDate;

	@JsonInclude(Include.NON_NULL)
	private String enteredBy;

	@JsonInclude(Include.NON_NULL)
	private String comment;

	@JsonInclude(Include.NON_NULL)
	private String userGroup;

	@JsonInclude(Include.NON_NULL)
	private String actionType;

	@JsonInclude(Include.NON_NULL)
	private String signature;

}
