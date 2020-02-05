package biz.nable.sb.cor.common.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WebHookBean {
	private String webHookType;
	private String type;
	private String status;
	private Object data;
}
