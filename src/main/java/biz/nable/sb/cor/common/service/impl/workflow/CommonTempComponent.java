package biz.nable.sb.cor.common.service.impl.workflow;

import biz.nable.sb.cor.common.bean.*;
import biz.nable.sb.cor.common.db.criteria.TempCustomRepository;
import biz.nable.sb.cor.common.db.entity.CommonTemp;
import biz.nable.sb.cor.common.db.entity.CommonTempHis;
import biz.nable.sb.cor.common.db.repository.CommonTempHisRepository;
import biz.nable.sb.cor.common.db.repository.CommonTempRepository;
import biz.nable.sb.cor.common.exception.InvalidRequestException;
import biz.nable.sb.cor.common.exception.RecordNotFoundException;
import biz.nable.sb.cor.common.exception.SystemException;
import biz.nable.sb.cor.common.request.CreateApprovalRequest;
import biz.nable.sb.cor.common.request.workflow.CreateWorkflowRequest;
import biz.nable.sb.cor.common.response.CreateApprovalResponse;
import biz.nable.sb.cor.common.response.workflow.CreateWorkflowResponse;
import biz.nable.sb.cor.common.service.impl.CommonConverter;
import biz.nable.sb.cor.common.utility.ActionTypeEnum;
import biz.nable.sb.cor.common.utility.ApprovalStatus;
import biz.nable.sb.cor.common.utility.ErrorCode;
import biz.nable.sb.cor.common.utility.SignatureComponent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class CommonTempComponent {
	@Autowired
	CommonTempRepository commonTempRepository;
	@Autowired
	CommonTempHisRepository commonTempHisRepository;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	CommonConverter commonConverter;

	@Autowired
	TempCustomRepository tempCustomRepository;

	@Autowired
	SignatureComponent signatureComponent;

	@Autowired
	RestTemplate restTemplate;

	@Value("${nable.biz.common.util.approval.service.url}")
	private String workflowServiceUrl;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	MessageSource messageSource;

	@Transactional
	public CommonResponseBean createCommonTemp(CommonRequestBean commonRequestBean, String requestId) {
		try {
			String log = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(commonRequestBean);
			logger.info("Start createCommonTemp with Action Type : {} Request : {}", actionType.name(), log);
		} catch (JsonProcessingException e) {
			logger.error("commonTemp Json parser error {}", e);
		}
		CommonResponseBean commonResponse;
		String userId = commonRequestBean.getUserId();
		String userGroup = commonRequestBean.getUserGroup();

		final String requestType = commonRequestBean.getRequestType();

		String referenceNo = commonRequestBean.getReferenceNo();

		CommonTemp commonTemp = getCommonTempToAuth(referenceNo, requestType, actionType, userId);
		Boolean isExisting = null != commonTemp.getId() && commonTemp.getId() > 0;
		buildCommonTemp(commonRequestBean.getCommonTempBean(), commonTemp, userId, userGroup, requestType,
				ApprovalStatus.PENDING, actionType);
		CommonTempHis commonTempHis = new CommonTempHis();
		try {
			String log = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(commonTemp);
			logger.info("save to commonTemp: {}", log);
		} catch (JsonProcessingException e) {
			logger.error("commonTemp Json parser error {}", e);
		}
		try {
			BeanUtils.copyProperties(commonTempHis, commonTemp);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("CommonTemp to CommonTempHis data mapping error {}", e);
			throw new SystemException(
					messageSource.getMessage(ErrorCode.DATA_COPY_ERROR, null, LocaleContextHolder.getLocale()),
					ErrorCode.DATA_COPY_ERROR);
		}
		String hashTags = commonRequestBean.getHashTags();
		referenceNo = null == referenceNo ? String.valueOf(commonTemp.getId()) : referenceNo;
		commonTemp.setReferenceNo(referenceNo);
		commonTemp.setHashTags(hashTags);
		commonTemp = commonTempRepository.save(commonTemp);
		logger.info("saved to db");

		commonTempHis.setTempId(commonTemp.getId());

		if (Boolean.FALSE.equals(isExisting)) {
			CreateWorkflowResponse createWorkflowResponse = createWorkflow(userId, userGroup, requestId, requestType,
					referenceNo, actionType);
			commonTemp.setApprovalId(createWorkflowResponse.getWorkflowBean().getApprovalId());
			commonTempHis.setApprovalId(createWorkflowResponse.getWorkflowBean().getApprovalId());

			commonTemp = commonTempRepository.save(commonTemp);
			logger.info("Update temp");
		}
		commonTempHisRepository.save(commonTempHis);
		logger.info("save to history");
		commonResponse = new CommonResponseBean();
		commonResponse.setErrorCode(ErrorCode.OPARATION_SUCCESS);
		commonResponse.setReturnCode(HttpStatus.OK.value());
		commonResponse.setReturnMessage(
				messageSource.getMessage(ErrorCode.OPARATION_SUCCESS, null, LocaleContextHolder.getLocale()));
		commonResponse.setCommonTempBean(commonRequestBean.getCommonTempBean());
		commonResponse.setTempId(String.valueOf(commonTemp.getId()));
		commonResponse.setApprovalId(commonTemp.getApprovalId());
		return commonResponse;
	}

	private CreateWorkflowResponse createWorkflow(String userId, String requestType, String referenceId) {
		logger.info("Start send to create workflow request");
		HttpHeaders headers = new HttpHeaders();
		headers.add("userId", userId);
		CreateWorkflowRequest request = new CreateWorkflowRequest();
		request.setReferenceId(referenceId);
		request.setCompanyId(userId);
		request.setType(userGroup);
		request.setSubType(referenceId);
		request.setDomain(actionType.name());
		request.setAmount(new Date());
		request.setCreatedBy(requestType);
		request.setCreatedDate(requestType);
		request.setModifiedBy(requestType);
		request.setModifiedDate(requestType);
		request.setStatus(requestType);
		request.setRemarks(requestType);
		request.setDbaccount(requestType);

		HttpEntity<CreateWorkflowRequest> entity = new HttpEntity<>(request, headers);
		CreateWorkflowResponse response = null;
		try {
			String createApprovalUrl = workflowServiceUrl + "/workflow/request";
			ResponseEntity<CreateWorkflowResponse> responseEntity = restTemplate.postForEntity(createApprovalUrl,
					entity, CreateWorkflowResponse.class);
			if (null != responseEntity.getBody()) {
				response = responseEntity.getBody();
			} else {
				logger.error("Create workflow request reserved null response");
				throw new SystemException(messageSource.getMessage(ErrorCode.CREATE_WORKFLOW_ERROR, null,
						LocaleContextHolder.getLocale()), ErrorCode.CREATE_WORKFLOW_ERROR);
			}
		} catch (HttpClientErrorException e) {
			logger.error("Error occred while creating workflow request: ", e);
			response = commonConverter.jsonToObject(e.getResponseBodyAsString(), CreateWorkflowResponse.class);
			throw new SystemException(response.getReturnMessage(), String.valueOf(response.getReturnCode()));
		}
		logger.info("End create workflow request successfuly");
		return response;
	}

	private CommonTemp buildCommonTemp(CommonTempBean createCommonRequest, CommonTemp commonTemp, String userId,
			String userGroup, String requestType, ApprovalStatus status, ActionTypeEnum actionType) {

		commonTemp.setRequestType(requestType);
		commonTemp.setCreatedBy(userId);
		commonTemp.setCreatedDate(new Date());
		commonTemp.setLastUpdatedBy(userId);
		commonTemp.setStatus(status);
		commonTemp.setActionType(actionType);
		commonTemp.setRequestPayload(commonConverter.pojoToMap(createCommonRequest));
		commonTemp.setUserGroup(userGroup);
		return commonTemp;
	}
}
