package biz.nable.sb.cor.common.db.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import biz.nable.sb.cor.common.db.entity.CommonTemp;
import biz.nable.sb.cor.common.utility.ApprovalStatus;

@Repository
public interface CommonTempRepository extends CrudRepository<CommonTemp, Long>, JpaSpecificationExecutor<CommonTemp> {

	List<CommonTemp> findByRequestTypeAndStatusAndHashTagsLike(String requestType, ApprovalStatus pending,
			String searchBy);

	List<CommonTemp> findByRequestTypeAndStatus(String requestType, ApprovalStatus pending);

	Optional<CommonTemp> findByReferenceNoAndRequestTypeAndStatus(String referenceNo, String requestType,
			ApprovalStatus pending);

	Optional<CommonTemp> findByApprovalIdAndStatus(String approvalId, ApprovalStatus pending);

}
