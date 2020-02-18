package biz.nable.sb.cor.common.db.entity;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import biz.nable.sb.cor.common.db.audit.Auditable;
import biz.nable.sb.cor.common.utility.ActionTypeEnum;
import biz.nable.sb.cor.common.utility.ApprovalStatus;
import biz.nable.sb.cor.common.utility.HashMapConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "SB_COR_COMMON_TEMP_HIS")
public class CommonTempHis extends Auditable {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMMON_TEMP_H_SEQ")
	@SequenceGenerator(name = "COMMON_TEMP_H_SEQ", sequenceName = "SB_COR_COMMON_TEMP_HIS_SEQ", allocationSize = 1)
	private Long id;
	private Long tempId;
	private String referenceNo;
	private String approvalId;
	private String comment;
	private ApprovalStatus status;
	private String requestType;
	private ActionTypeEnum actionType;
	@Convert(converter = HashMapConverter.class)
	@Column(length = 4000)
	private Map<String, Object> requestPayload;
}
