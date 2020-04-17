/*
*Copyright (c) 2019 N*Able (pvt) Ltd.
*/
package biz.nable.sb.cor.common.bean.workflow;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * @Author		:Shehan
 * @Date		:06/04/2020
 * @Description	:This bean class is to send output parameters to the common util.
 */
@Getter
@Setter
@ToString
public class AssignBean {

	private String assignId;

	private String level;

	private String type;

	private String subType;

	private String flag;

	private String companyId;

	private String groupName;

	private String workflowId;

	private String referenceId;

	private String option;

	private String ruleId;
	
	private String explan;

	private String status;



}
