package biz.nable.sb.cor.common.db.criteria.workflow;

import biz.nable.sb.cor.common.bean.workflow.CommonSearchBean;
import biz.nable.sb.cor.common.db.entity.workflow.CommonTempWorkflow;
import biz.nable.sb.cor.common.db.repository.workflow.CommonTempWorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

@Component
public class TempCustomWorkflowRepository {

	@Autowired
	CommonTempWorkflowRepository commonTempRepository;

	@PersistenceContext
	private EntityManager entityManager;
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${system.find.init.date}")
	private String initFromDate;

	@Autowired
	MessageSource messageSource;

	public List<CommonTempWorkflow> findTempRecordList(CommonSearchBean searchBean) {
		logger.info("Start create findAssignList criteria");
		return commonTempRepository.findAll(new Specification<CommonTempWorkflow>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<CommonTempWorkflow> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<>();
				referenceNoCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				workflowTypeCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				userIdCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				hashTagsCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				statusCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				companyIdCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);

				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}

		});
	}

	private void referenceNoCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTempWorkflow> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getReferenceNo() && !commonSearchBean.getReferenceNo().isEmpty()) {
			predicates.add(criteriaBuilder
					.and(criteriaBuilder.equal(root.get("referenceNo"), commonSearchBean.getReferenceNo())));
		}
	}

	private void workflowTypeCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
											 Root<CommonTempWorkflow> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getType() && !commonSearchBean.getType().isEmpty()) {
			predicates.add(criteriaBuilder
					.and(criteriaBuilder.equal(root.get("type"), commonSearchBean.getType())));
		}
	}

	private void userIdCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTempWorkflow> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getUserId() && !commonSearchBean.getUserId().isEmpty()) {
			predicates.add(
					criteriaBuilder.and(criteriaBuilder.equal(root.get("createdBy"), commonSearchBean.getUserId())));
		}
	}

	private void hashTagsCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTempWorkflow> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getHashTags() && !commonSearchBean.getHashTags().isEmpty()) {
			predicates.add(criteriaBuilder
					.and(criteriaBuilder.like(root.get("hashTags"), "%" + commonSearchBean.getHashTags() + "%")));
		}
	}

	private void statusCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTempWorkflow> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getStatus()) {
			predicates.add(
					criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), commonSearchBean.getStatus())));
		}
	}

	private void companyIdCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
									   Root<CommonTempWorkflow> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getCompanyId()) {
			predicates.add(
					criteriaBuilder.and(criteriaBuilder.equal(root.get("companyId"), commonSearchBean.getCompanyId())));
		}
	}

}