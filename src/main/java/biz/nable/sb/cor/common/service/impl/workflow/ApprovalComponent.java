package biz.nable.sb.cor.common.service.impl.workflow;

import biz.nable.sb.cor.common.bean.workflow.CommonSearchBean;
import biz.nable.sb.cor.common.bean.workflow.TempDto;
import biz.nable.sb.cor.common.db.criteria.workflow.TempCustomRepository;
import biz.nable.sb.cor.common.db.entity.CommonTemp;
import biz.nable.sb.cor.common.exception.InvalidRequestException;
import biz.nable.sb.cor.common.exception.RecordNotFoundException;
import biz.nable.sb.cor.common.exception.SystemException;
import biz.nable.sb.cor.common.request.CreateApprovalRequest;
import biz.nable.sb.cor.common.response.ApprovalResponse;
import biz.nable.sb.cor.common.response.workflow.WorkflowAssignmentsResponse;
import biz.nable.sb.cor.common.utility.ErrorCode;
import biz.nable.sb.cor.common.utility.SignatureComponent;
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
public class ApprovalComponent {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	RestTemplate restTemplate;
	@Autowired
	TempCustomRepository tempCustomRepository;

	@Value("${nable.biz.common.util.workflow.assigns.service.url}")
	private String workflowAssignsServiceUrl;

	@Autowired
	SignatureComponent signatureComponent;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public CommonSearchBean getAssignsDetails(CommonSearchBean commonSearchBean) {
		String searchBy = commonSearchBean.getHashTags();
		String requestType = commonSearchBean.getRequestType();
		List<TempDto> list = new ArrayList<>();
		logger.info("Start getAssignDetails record requestType: {}, status: {}, searchBy: {}", requestType,
				WorkflowStatus.PENDING, searchBy);
		String userId = commonSearchBean.getUserId();
		commonSearchBean.setUserId(null);
		commonSearchBean.setStatus(WorkflowStatus.PENDING);
		List<CommonTemp> commonTemps = tempCustomRepository.findTempRecordList(commonSearchBean);

		if (!commonTemps.isEmpty()) {
			logger.info("{} Temp records found", commonTemps.size());
			commonSearchBean.setUserId(userId);
			WorkflowAssignmentsResponse assignmentDetailsResponse = getAssigns(commonSearchBean);

			for (CommonTemp commonTemp : commonTemps) {
				try {
					if (assignmentDetailsResponse.getWorkflowAssignments().stream()
							.anyMatch(o -> o.getAssignId().equals(commonTemp.getAssignId()))) {
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

	private WorkflowAssignmentsResponse getAssigns(CommonSearchBean commonSearchBean) {

		String requestId = "";
		logger.info("Start the getAssigns process - RequestId : {}", requestId);
		HttpHeaders headers = new HttpHeaders();
		//headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("userGroup", commonSearchBean.getUserGroup());
		headers.set("userId", commonSearchBean.getUserId());
		HttpEntity<CreateApprovalRequest> entity = new HttpEntity<>(headers);
		try {
			String assignListUrl = workflowAssignsServiceUrl + "/assign";

			ResponseEntity<WorkflowAssignmentsResponse> responseEntity = restTemplate.exchange(assignListUrl, HttpMethod.GET,
					entity, WorkflowAssignmentsResponse.class);
			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Failed to retrieve getAssignmentData(Status: {})", responseEntity.getStatusCode());
				throw new SystemException(responseEntity.getBody().getReturnMessage(),
						responseEntity.getBody().getErrorCode());
			} else {
				logger.info("Successfully fetch the getAssignmentData");
				return responseEntity.getBody();
			}
		} catch (Exception e) {
			throw new SystemException(
					messageSource.getMessage(ErrorCode.UNKNOWN_ERROR, null, LocaleContextHolder.getLocale()),
					ErrorCode.UNKNOWN_ERROR);
		}
	}

}
