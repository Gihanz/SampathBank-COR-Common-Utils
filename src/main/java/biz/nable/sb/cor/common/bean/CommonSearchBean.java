package biz.nable.sb.cor.common.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import biz.nable.sb.cor.common.utility.ActionTypeEnum;
import biz.nable.sb.cor.common.utility.ApprovalStatus;
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
	private ApprovalStatus status;
	private String requestType;
	private ActionTypeEnum actionType;
	@JsonIgnore
	private List<TempDto> tempList;
}
