package biz.nable.sb.cor.common.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import biz.nable.sb.cor.common.utility.ActionTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonSearchBean implements SearchCriteriaObject {
	private String referenceNo;
	private String userId;
	private String userGroup;
	private String hashTags;
	private String tempId;
	private String status;
	private String requestType;
	ActionTypeEnum actionType;
	@JsonIgnore
	private List<TempDto> tempList;
}
