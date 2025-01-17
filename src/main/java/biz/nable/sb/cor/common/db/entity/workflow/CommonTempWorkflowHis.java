package biz.nable.sb.cor.common.db.entity.workflow;

import biz.nable.sb.cor.common.db.audit.workflow.AuditableWorkflow;
import biz.nable.sb.cor.common.utility.HashMapConverter;
import biz.nable.sb.cor.common.utility.workflow.WorkflowStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Map;

@Getter
@Setter
@ToString
@Entity
@Table(name = "SB_COR_COMMON_TEMP_WORKFLOW_HIS")
public class CommonTempWorkflowHis extends AuditableWorkflow {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMMON_TEMP_WORKFLOW_H_SEQ")
	@SequenceGenerator(name = "COMMON_TEMP_WORKFLOW_H_SEQ", sequenceName = "SB_COR_COMMON_TEMP_WORKFLOW_HIS_SEQ", allocationSize = 1)
	private Long id;
	private Long tempId;
	private String referenceId;
	private String workflowId;
	private String hashTags;
	private WorkflowStatus status;
	private String type;
	private String companyId;

	@Convert(converter = HashMapConverter.class)
	@Column(length = 4000)
	private Map<String, Object> requestPayload;
}
