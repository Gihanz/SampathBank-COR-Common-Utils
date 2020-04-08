package biz.nable.sb.cor.common.service.impl.workflow;

import biz.nable.sb.cor.common.bean.workflow.CommonSearchBean;
import biz.nable.sb.cor.common.bean.workflow.TempDto;
import biz.nable.sb.cor.common.db.criteria.workflow.TempCustomWorkflowRepository;
import biz.nable.sb.cor.common.db.entity.CommonTemp;
import biz.nable.sb.cor.common.db.entity.workflow.CommonTempWorkflow;
import biz.nable.sb.cor.common.exception.InvalidRequestException;
import biz.nable.sb.cor.common.exception.RecordNotFoundException;
import biz.nable.sb.cor.common.exception.SystemException;
import biz.nable.sb.cor.common.request.CreateApprovalRequest;
import biz.nable.sb.cor.common.response.workflow.WorkflowResponse;
import biz.nable.sb.cor.common.utility.ErrorCode;
import biz.nable.sb.cor.common.utility.workflow.SignatureWorkflowComponent;
import biz.nable.sb.cor.common.utility.workflow.WorkflowStatus;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorkflowComponent {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	RestTemplate restTemplate;
	@Autowired
	TempCustomWorkflowRepository tempCustomWorkflowRepository;

	@Value("${nable.biz.common.util.workflow.service.url}")
	private String workflowServiceUrl;

	@Autowired
	SignatureWorkflowComponent signatureComponent;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public CommonSearchBean getWorkflowDetails(CommonSearchBean commonSearchBean) {
		String searchBy = commonSearchBean.getHashTags();
		String requestType = commonSearchBean.getType();
		List<TempDto> list = new ArrayList<>();
		logger.info("Start getWorkflowDetails record type: {}, status: {}, searchBy: {}", requestType,
				WorkflowStatus.PENDING, searchBy);
		String userId = commonSearchBean.getUserId();
		commonSearchBean.setUserId(null);
		commonSearchBean.setStatus(WorkflowStatus.PENDING);
		List<CommonTempWorkflow> commonTemps = tempCustomWorkflowRepository.findTempRecordList(commonSearchBean);

		if (!commonTemps.isEmpty()) {
			logger.info("{} Workflow Temp Records Found", commonTemps.size());
			commonSearchBean.setUserId(userId);
			WorkflowResponse workflowDetailsResponse = getWorkflows(commonSearchBean);

			for (CommonTempWorkflow commonTemp : commonTemps) {
				try {
					if (workflowDetailsResponse.getWorkflows().stream()
							.anyMatch(o -> o.getWorkflowId().equals(commonTemp.getWorkflowId()))) {
						TempDto dto = new TempDto();
						BeanUtils.copyProperties(dto, commonTemp);
						dto.setSignature(signatureComponent.genarateSignature(commonTemp));
						list.add(dto);
					}
				} catch (Exception e) {
					throw new InvalidRequestException(
							messageSource.getMessage(ErrorCode.DATA_COPY_ERROR, null, LocaleContextHolder.getLocale()),
							ErrorCode.DATA_COPY_ERROR);
				}
			}
		} else {
			String msg = messageSource.getMessage(ErrorCode.NO_TEMP_RECORD_FOUND, null,
					LocaleContextHolder.getLocale());
			logger.error(msg);
			throw new RecordNotFoundException(msg, ErrorCode.NO_TEMP_RECORD_FOUND);
		}
		commonSearchBean.setTempList(list);
		return commonSearchBean;
	}

	private WorkflowResponse getWorkflows(CommonSearchBean commonSearchBean) {

		String requestId = "";
		logger.info("Start the getWorkflows process - RequestId : {}", requestId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("request-id", requestId);
		headers.set("userId", commonSearchBean.getUserId());
		headers.set("companyId", commonSearchBean.getCompanyId());
		headers.set("type", commonSearchBean.getType());
		HttpEntity<CreateApprovalRequest> entity = new HttpEntity<>(headers);
		try {
			String assignListUrl = workflowServiceUrl + "/getWorkflowDetails";

			ResponseEntity<WorkflowResponse> responseEntity = restTemplate.exchange(assignListUrl, HttpMethod.GET,
					entity, WorkflowResponse.class);
			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Failed to retrieve getWorkflows(Status: {})", responseEntity.getStatusCode());
				throw new SystemException(responseEntity.getBody().getReturnMessage(),
						responseEntity.getBody().getErrorCode());
			} else {
				logger.info("Successfully fetch the getWorkflows");
				return responseEntity.getBody();
			}
		} catch (Exception e) {
			throw new SystemException(
					messageSource.getMessage(ErrorCode.UNKNOWN_ERROR, null, LocaleContextHolder.getLocale()),
					ErrorCode.UNKNOWN_ERROR);
		}
	}

}
