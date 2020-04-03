package biz.nable.sb.cor.common.service.impl.workflow;

import biz.nable.sb.cor.common.bean.workflow.*;
import biz.nable.sb.cor.common.db.criteria.TempCustomRepository;
import biz.nable.sb.cor.common.db.entity.workflow.CommonTemp;
import biz.nable.sb.cor.common.db.entity.workflow.CommonTempHis;
import biz.nable.sb.cor.common.db.repository.workflow.CommonTempHisRepository;
import biz.nable.sb.cor.common.db.repository.workflow.CommonTempRepository;
import biz.nable.sb.cor.common.exception.InvalidRequestException;
import biz.nable.sb.cor.common.exception.SystemException;
import biz.nable.sb.cor.common.request.workflow.CreateWorkflowRequest;
import biz.nable.sb.cor.common.response.workflow.CreateWorkflowResponse;
import biz.nable.sb.cor.common.service.impl.CommonConverter;
import biz.nable.sb.cor.common.utility.ErrorCode;
import biz.nable.sb.cor.common.utility.SignatureComponent;
import biz.nable.sb.cor.common.utility.workflow.WorkflowStatus;
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
import java.util.Date;
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
			logger.info("Start createCommonTemp for create workflow Request : {}", log);
		} catch (JsonProcessingException e) {
			logger.error("commonTemp Json parser error {}", e);
		}
		CommonResponseBean commonResponse;
		String userId = commonRequestBean.getUserId();
		final String type = commonRequestBean.getType();
		String referenceNo = commonRequestBean.getReferenceId();

		CommonTemp commonTemp = getCommonTempToWorkflowCreation(referenceNo, type, userId);
		Boolean isExisting = null != commonTemp.getId() && commonTemp.getId() > 0;

		buildCommonTemp(commonRequestBean.getCommonTempBean(), commonTemp, userId, type, WorkflowStatus.PENDING);
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
		commonTemp.setReferenceId(referenceNo);
		commonTemp.setHashTags(hashTags);
		commonTemp = commonTempRepository.save(commonTemp);
		logger.info("saved to db");

		commonTempHis.setTempId(commonTemp.getId());

		if (Boolean.FALSE.equals(isExisting)) {
			CreateWorkflowResponse createWorkflowResponse = createWorkflow(commonRequestBean);
			commonTemp.setWorkflowId(createWorkflowResponse.getWorkflowBean().getWorkflowId());
			commonTempHis.setWorkflowId(createWorkflowResponse.getWorkflowBean().getWorkflowId());

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
		commonResponse.setWorkflowId(commonTemp.getWorkflowId());
		return commonResponse;
	}

	private CreateWorkflowResponse createWorkflow(CommonRequestBean commonRequestBean) {
		logger.info("Start send to create workflow request");
		HttpHeaders headers = new HttpHeaders();
		headers.add("userId", commonRequestBean.getUserId());
		CreateWorkflowRequest request = new CreateWorkflowRequest();
		request.setReferenceId(commonRequestBean.getReferenceId());
		request.setCompanyId(commonRequestBean.getCompanyId());
		request.setType(commonRequestBean.getType());
		request.setSubType(commonRequestBean.getSubType());
		request.setDomain(commonRequestBean.getDomain());
		request.setAmount(commonRequestBean.getAmount());
		request.setCreatedBy(commonRequestBean.getUserId());
		request.setCreatedDate(new Date());
		request.setModifiedBy(commonRequestBean.getModifiedBy());
		request.setModifiedDate(commonRequestBean.getModifiedDate());
		request.setStatus(commonRequestBean.getStatus());
		request.setRemarks(commonRequestBean.getRemarks());
		request.setDbaccount(commonRequestBean.getDbaccount());

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
			 String type, WorkflowStatus status) {

		commonTemp.setType(type);
		commonTemp.setCreatedBy(userId);
		commonTemp.setCreatedDate(new Date());
		commonTemp.setLastUpdatedBy(userId);
		commonTemp.setStatus(status);
		commonTemp.setRequestPayload(commonConverter.pojoToMap(createCommonRequest));
		return commonTemp;
	}

    private CommonTemp getCommonTempToWorkflowCreation(String referenceNo, String type, String userId) {
        Optional<CommonTemp> optional = commonTempRepository.findByReferenceIdAndTypeAndStatus(referenceNo,type, WorkflowStatus.PENDING);
        CommonTemp commonTemp;
        if (optional.isPresent()) {
            commonTemp = optional.get();
            if (!type.equals(commonTemp.getType())) {
                logger.info(messageSource.getMessage(ErrorCode.NOTHER_PENDING_WORKFLOW_RECORD_FOUND, null,
                        LocaleContextHolder.getLocale()));
                throw new InvalidRequestException(
                        messageSource.getMessage(ErrorCode.NOTHER_PENDING_WORKFLOW_RECORD_FOUND, null,
                                LocaleContextHolder.getLocale()),
                        ErrorCode.NOTHER_PENDING_WORKFLOW_RECORD_FOUND);
            } else if (!userId.equals(commonTemp.getCreatedBy())) {
                throw new InvalidRequestException(
                        messageSource.getMessage(ErrorCode.USER_NOT_PERMITTED, null, LocaleContextHolder.getLocale()),
                        ErrorCode.USER_NOT_PERMITTED);
            }
        } else {
            commonTemp = new CommonTemp();
        }

        return commonTemp;
    }
}
