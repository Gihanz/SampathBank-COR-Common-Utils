package biz.nable.sb.cor.common.db.repository.workflow;

import biz.nable.sb.cor.common.db.entity.workflow.CommonTempWorkflow;
import biz.nable.sb.cor.common.utility.workflow.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CommonTempWorkflowRepository extends CrudRepository<CommonTempWorkflow, Long>, JpaSpecificationExecutor<CommonTempWorkflow> {

	Optional<CommonTempWorkflow> findByReferenceIdAndTypeAndStatus(String referenceId, String type, WorkflowStatus status);

}
