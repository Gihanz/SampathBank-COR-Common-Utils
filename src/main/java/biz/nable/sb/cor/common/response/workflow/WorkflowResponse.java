/*
*Copyright (c) 2019 N*Able (pvt) Ltd.
*/
package biz.nable.sb.cor.common.response.workflow;

import biz.nable.sb.cor.common.bean.ApprovalBean;
import biz.nable.sb.cor.common.bean.workflow.WorkflowBean;
import biz.nable.sb.cor.common.response.CommonResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * @Description	:This response class is to provide approval response.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse extends CommonResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<WorkflowBean> workflows = new ArrayList<>();

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String status;

}
