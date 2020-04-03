package biz.nable.sb.cor.common.db.audit.workflow;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import java.util.Date;

import static javax.persistence.TemporalType.TIMESTAMP;

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

}
