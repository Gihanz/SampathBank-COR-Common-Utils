/*
*Copyright (c) 2019 N*Able (pvt) Ltd.
*/
package biz.nable.sb.cor.common.request.workflow;

import biz.nable.sb.cor.common.bean.workflow.WorkflowBean;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/* @Author		:shehan_inova
 * @Date		:02/04/2020
 * @Description	: Interface between common util and workflow for send input parameters
 * to the API.
 */
@Getter
@Setter
@ToString
public class CreateWorkflowRequest extends WorkflowBean implements Serializable {

	private static final long serialVersionUID = 1L;

}
