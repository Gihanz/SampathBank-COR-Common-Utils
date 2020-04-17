package biz.nable.sb.cor.common.bean.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommonSearchBean implements SearchCriteriaObject {
	private String referenceNo;
	private String userId;
	private String hashTags;
	private String tempId;
	private String status;
	private String type;
	private String companyId;

	@JsonIgnore
	private List<TempDto> tempList;
}
