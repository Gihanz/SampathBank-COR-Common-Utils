package biz.nable.sb.cor.common.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonRequestBean {
	private CommonTempBean commonTempBean;
	private String referenceNo;
	private String userId;
	private String userGroup;
	private String hashTags;
	private String tempId;
	private String status;
	private String requestType;
}
