/*
*Copyright (c) 2019 N*Able (pvt) Ltd.
*/
package biz.nable.sb.cor.common.request;

import java.io.Serializable;

import biz.nable.sb.cor.common.bean.ApprovalBean;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * @Description	:This dto class is to transfer data from and to front-end.
 */
@Getter
@Setter
@ToString
public class CreateApprovalRequest extends ApprovalBean implements Serializable {

	private static final long serialVersionUID = 1L;

}
