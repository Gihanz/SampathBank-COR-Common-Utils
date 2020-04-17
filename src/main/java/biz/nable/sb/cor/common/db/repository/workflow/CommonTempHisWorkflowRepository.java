package biz.nable.sb.cor.common.db.repository.workflow;

import biz.nable.sb.cor.common.db.entity.workflow.CommonTempWorkflowHis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonTempHisWorkflowRepository extends CrudRepository<CommonTempWorkflowHis, Long> {

}
