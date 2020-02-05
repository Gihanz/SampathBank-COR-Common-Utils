package biz.nable.sb.cor.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FindTempByRefBean {
	private String referenceNo;
	private String requestType;
	private String userId;
	private String userGroup;
}
