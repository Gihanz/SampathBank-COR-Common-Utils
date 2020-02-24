package biz.nable.sb.cor.common.db.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import biz.nable.sb.cor.common.bean.CommonSearchBean;
import biz.nable.sb.cor.common.db.entity.CommonTemp;
import biz.nable.sb.cor.common.db.repository.CommonTempRepository;

@Component
public class TempCustomRepository {

	@Autowired
	CommonTempRepository commonTempRepository;

	@PersistenceContext
	private EntityManager entityManager;
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${system.find.init.date}")
	private String initFromDate;

	@Autowired
	MessageSource messageSource;

	public List<CommonTemp> findTempRecordList(CommonSearchBean searchBean) {
		logger.info("Start create findCompanyList criteria");
		return commonTempRepository.findAll(new Specification<CommonTemp>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<CommonTemp> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {

				List<Predicate> predicates = new ArrayList<>();
				referenceNoCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				requestTypeCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				actionTypeCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				userGroupCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				userIdCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				hashTagsCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);
				statusCriteriaBuilder(predicates, criteriaBuilder, root, searchBean);

				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}

		});
	}

	private void referenceNoCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTemp> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getReferenceNo() && !commonSearchBean.getReferenceNo().isEmpty()) {
			predicates.add(criteriaBuilder
					.and(criteriaBuilder.equal(root.get("referenceNo"), commonSearchBean.getReferenceNo())));
		}
	}

	private void requestTypeCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTemp> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getRequestType() && !commonSearchBean.getRequestType().isEmpty()) {
			predicates.add(criteriaBuilder
					.and(criteriaBuilder.equal(root.get("requestType"), commonSearchBean.getRequestType())));
		}
	}

	private void actionTypeCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTemp> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getActionType()) {
			predicates.add(criteriaBuilder
					.and(criteriaBuilder.equal(root.get("actionType"), commonSearchBean.getActionType())));
		}
	}

	private void userGroupCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTemp> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getUserGroup() && !commonSearchBean.getUserGroup().isEmpty()) {
			predicates.add(
					criteriaBuilder.and(criteriaBuilder.equal(root.get("userGroup"), commonSearchBean.getUserGroup())));
		}
	}

	private void userIdCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTemp> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getUserId() && !commonSearchBean.getUserId().isEmpty()) {
			predicates.add(
					criteriaBuilder.and(criteriaBuilder.equal(root.get("createdBy"), commonSearchBean.getUserId())));
		}
	}

	private void hashTagsCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTemp> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getHashTags() && !commonSearchBean.getHashTags().isEmpty()) {
			predicates.add(criteriaBuilder
					.and(criteriaBuilder.like(root.get("hashTags"), "%" + commonSearchBean.getHashTags() + "%")));
		}
	}

	private void statusCriteriaBuilder(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
			Root<CommonTemp> root, CommonSearchBean commonSearchBean) {
		if (null != commonSearchBean.getStatus()) {
			predicates.add(
					criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), commonSearchBean.getStatus())));
		}
	}

}