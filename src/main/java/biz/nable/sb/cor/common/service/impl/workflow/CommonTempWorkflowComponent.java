package biz.nable.sb.cor.common.service.impl.workflow;

import biz.nable.sb.cor.common.bean.workflow.*;
import biz.nable.sb.cor.common.db.criteria.TempCustomRepository;
import biz.nable.sb.cor.common.db.entity.workflow.CommonTempWorkflow;
import biz.nable.sb.cor.common.db.entity.workflow.CommonTempWorkflowHis;
import biz.nable.sb.cor.common.db.repository.workflow.CommonTempHisWorkflowRepository;
import biz.nable.sb.cor.common.db.repository.workflow.CommonTempWorkflowRepository;
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
public class CommonTempWorkflowComponent {
	@Autowired
    CommonTempWorkflowRepository commonTempWorkflowRepository;
	@Autowired
    CommonTempHisWorkflowRepository commonTempHisWorkflowRepository;
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

	@Value("${nable.biz.common.util.workflow.service.url}")
	private String workflowServiceUrl;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	MessageSource messageSource;

	@Transactional
	public CommonResponseWorkflowBean createCommonTemp(CommonRequestWorkflowBean commonRequestWorkflowBean, String requestId) {
		try {
			String log = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(commonRequestWorkflowBean);
			logger.info("Start createCommonTemp for create workflow Request : {}", log);
		} catch (JsonProcessingException e) {
			logger.error("commonTempWorkflow Json parser error {}", e);
		}
		CommonResponseWorkflowBean commonResponse;
		String userId = commonRequestWorkflowBean.getUserId();
		final String type = commonRequestWorkflowBean.getType();
		String referenceNo = commonRequestWorkflowBean.getReferenceId();

		CommonTempWorkflow commonTempWorkflow = getCommonTempToWorkflowCreation(referenceNo, type, userId);
		Boolean isExisting = null != commonTempWorkflow.getId() && commonTempWorkflow.getId() > 0;

		buildCommonTemp(commonRequestWorkflowBean.getCommonTempWorkflowBean(), commonTempWorkflow, userId, type, WorkflowStatus.PENDING);
		CommonTempWorkflowHis commonTempWorkflowHis = new CommonTempWorkflowHis();

		try {
			String log = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(commonTempWorkflow);
			logger.info("save to commonTempWorkflow: {}", log);
		} catch (JsonProcessingException e) {
			logger.error("commonTempWorkflow Json parser error {}", e);
		}
		try {
			BeanUtils.copyProperties(commonTempWorkflowHis, commonTempWorkflow);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("CommonTempWorkflow to CommonTempWorkflowHis data mapping error {}", e);
			throw new SystemException(
					messageSource.getMessage(ErrorCode.DATA_COPY_ERROR, null, LocaleContextHolder.getLocale()),
					ErrorCode.DATA_COPY_ERROR);
		}
		String hashTags = commonRequestWorkflowBean.getHashTags();
		referenceNo = null == referenceNo ? String.valueOf(commonTempWorkflow.getId()) : referenceNo;
		commonTempWorkflow.setReferenceId(referenceNo);
		commonTempWorkflow.setHashTags(hashTags);
		commonTempWorkflow = commonTempWorkflowRepository.save(commonTempWorkflow);
		logger.info("saved to db");

		commonTempWorkflowHis.setTempId(commonTempWorkflow.getId());

		if (Boolean.FALSE.equals(isExisting)) {
			CreateWorkflowResponse createWorkflowResponse = createWorkflow(commonRequestWorkflowBean);
			commonTempWorkflow.setWorkflowId(createWorkflowResponse.getWorkflowBean().getWorkflowId());
			commonTempWorkflowHis.setWorkflowId(createWorkflowResponse.getWorkflowBean().getWorkflowId());

			commonTempWorkflow = commonTempWorkflowRepository.save(commonTempWorkflow);
			logger.info("Update temp");
		}
		commonTempHisWorkflowRepository.save(commonTempWorkflowHis);
		logger.info("save to history");
		commonResponse = new CommonResponseWorkflowBean();
		commonResponse.setErrorCode(ErrorCode.OPARATION_SUCCESS);
		commonResponse.setReturnCode(HttpStatus.OK.value());
		commonResponse.setReturnMessage(
				messageSource.getMessage(ErrorCode.OPARATION_SUCCESS, null, LocaleContextHolder.getLocale()));
		commonResponse.setCommonTempWorkflowBean(commonRequestWorkflowBean.getCommonTempWorkflowBean());
		commonResponse.setTempId(String.valueOf(commonTempWorkflow.getId()));
		commonResponse.setWorkflowId(commonTempWorkflow.getWorkflowId());
		return commonResponse;
	}

	private CreateWorkflowResponse createWorkflow(CommonRequestWorkflowBean commonRequestWorkflowBean) {
		logger.info("Start send to create workflow request");
		HttpHeaders headers = new HttpHeaders();
		headers.add("userId", commonRequestWorkflowBean.getUserId());
		CreateWorkflowRequest request = new CreateWorkflowRequest();
		request.setReferenceId(commonRequestWorkflowBean.getReferenceId());
		request.setCompanyId(commonRequestWorkflowBean.getCompanyId());
		request.setType(commonRequestWorkflowBean.getType());
		request.setSubType(commonRequestWorkflowBean.getSubType());
		request.setDomain(commonRequestWorkflowBean.getDomain());
		request.setAmount(commonRequestWorkflowBean.getAmount());
		request.setCreatedBy(commonRequestWorkflowBean.getUserId());
		request.setCreatedDate(new Date());
		request.setModifiedBy(commonRequestWorkflowBean.getModifiedBy());
		request.setModifiedDate(commonRequestWorkflowBean.getModifiedDate());
		request.setStatus(commonRequestWorkflowBean.getStatus());
		request.setRemarks(commonRequestWorkflowBean.getRemarks());
		request.setDbaccount(commonRequestWorkflowBean.getDbaccount());

		HttpEntity<CreateWorkflowRequest> entity = new HttpEntity<>(request, headers);
		CreateWorkflowResponse response = null;
		try {
			String createApprovalUrl = workflowServiceUrl + "/v1/workflow/request";
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

	private CommonTempWorkflow buildCommonTemp(CommonTempWorkflowBean createCommonRequest, CommonTempWorkflow commonTempWorkflow, String userId,
                                               String type, WorkflowStatus status) {

		commonTempWorkflow.setType(type);
		commonTempWorkflow.setCreatedBy(userId);
		commonTempWorkflow.setCreatedDate(new Date());
		commonTempWorkflow.setLastUpdatedBy(userId);
		commonTempWorkflow.setStatus(status);
		commonTempWorkflow.setRequestPayload(commonConverter.pojoToMap(createCommonRequest));
		return commonTempWorkflow;
	}

    private CommonTempWorkflow getCommonTempToWorkflowCreation(String referenceNo, String type, String userId) {
        Optional<CommonTempWorkflow> optional = commonTempWorkflowRepository.findByReferenceIdAndTypeAndStatus(referenceNo,type, WorkflowStatus.PENDING);
        CommonTempWorkflow commonTempWorkflow;
        if (optional.isPresent()) {
            commonTempWorkflow = optional.get();
            if (!type.equals(commonTempWorkflow.getType())) {
                logger.info(messageSource.getMessage(ErrorCode.NOTHER_PENDING_WORKFLOW_RECORD_FOUND, null,
                        LocaleContextHolder.getLocale()));
                throw new InvalidRequestException(
                        messageSource.getMessage(ErrorCode.NOTHER_PENDING_WORKFLOW_RECORD_FOUND, null,
                                LocaleContextHolder.getLocale()),
                        ErrorCode.NOTHER_PENDING_WORKFLOW_RECORD_FOUND);
            } else if (!userId.equals(commonTempWorkflow.getCreatedBy())) {
                throw new InvalidRequestException(
                        messageSource.getMessage(ErrorCode.USER_NOT_PERMITTED, null, LocaleContextHolder.getLocale()),
                        ErrorCode.USER_NOT_PERMITTED);
            }
        } else {
            commonTempWorkflow = new CommonTempWorkflow();
        }

        return commonTempWorkflow;
    }
}
