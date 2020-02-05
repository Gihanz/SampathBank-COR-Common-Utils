package biz.nable.sb.cor.common.db.audit;

import static javax.persistence.TemporalType.TIMESTAMP;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
	@Column(length = 12)
	protected String createdBy;

	@CreationTimestamp
	@Temporal(TIMESTAMP)
	@Column(nullable = false)
	protected Date createdDate;
	@Column(length = 12)
	protected String lastUpdatedBy;

	@UpdateTimestamp
	@Temporal(TIMESTAMP)
	@Column(nullable = false)
	protected Date lastUpdatedDate;

	private Date lastVerifiedDate;

	@Column(length = 12)
	private String lastVerifiedBy;
	private String userGroup;
}
