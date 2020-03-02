package biz.nable.sb.cor.common.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import biz.nable.sb.cor.common.bean.ApprovalBean;
import biz.nable.sb.cor.common.bean.ApprovalResponseBean;
import biz.nable.sb.cor.common.bean.CommonSearchBean;
import biz.nable.sb.cor.common.bean.TempDto;
import biz.nable.sb.cor.common.db.criteria.TempCustomRepository;
import biz.nable.sb.cor.common.db.entity.CommonRejected;
import biz.nable.sb.cor.common.db.entity.CommonTemp;
import biz.nable.sb.cor.common.db.entity.CommonTempHis;
import biz.nable.sb.cor.common.db.repository.CommonRejectedRepository;
import biz.nable.sb.cor.common.db.repository.CommonTempHisRepository;
import biz.nable.sb.cor.common.db.repository.CommonTempRepository;
import biz.nable.sb.cor.common.exception.InvalidRequestException;
import biz.nable.sb.cor.common.exception.RecordNotFoundException;
import biz.nable.sb.cor.common.exception.SystemException;
import biz.nable.sb.cor.common.request.CreateApprovalRequest;
import biz.nable.sb.cor.common.response.ApprovalResponse;
import biz.nable.sb.cor.common.response.CommonResponse;
import biz.nable.sb.cor.common.utility.ApprovalStatus;
import biz.nable.sb.cor.common.utility.ErrorCode;
import biz.nable.sb.cor.common.utility.SignatureComponent;

@Component
public class ApprovalComponent {

	@Autowired
	private CommonTempRepository commonTempRepository;
	@Autowired
	private CommonTempHisRepository commonTempHisRepository;
	@Autowired
	private CommonRejectedRepository commonRejectedRepository;
	@Autowired
	private MessageSource messageSource;

	@Autowired
	RestTemplate restTemplate;
	@Autowired
	TempCustomRepository tempCustomRepository;

	@Value("${nable.biz.common.util.approval.service.url}")
	private String approvalServiceUrl;

	@Autowired
	SignatureComponent signatureComponent;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ApprovalResponseBean updateCommonTemp(ApprovalBean approvalBean) {
		logger.info("Start to update commonTemp");
		CommonTemp commonTemp = null;
		Optional<CommonTemp> optional = commonTempRepository.findByApprovalIdAndStatus(approvalBean.getApprovalId(),
				ApprovalStatus.PENDING);
		if (optional.isPresent()) {
			commonTemp = optional.get();
		} else {
			throw new RecordNotFoundException(
					messageSource.getMessage(ErrorCode.NO_TEMP_RECORD_FOUND, null, LocaleContextHolder.getLocale()),
					ErrorCode.NO_TEMP_RECORD_FOUND);
		}

		signatureComponent.validateSignature(approvalBean.getSignature(), commonTemp);
		ApprovalResponseBean commonResponse = new ApprovalResponseBean();
		CommonTempHis commonTempHis = new CommonTempHis();
		TempDto tempDto = new TempDto();
		commonTemp.setLastUpdatedBy(approvalBean.getEnteredBy());
		commonTemp.setComment(null != approvalBean.getComment() ? approvalBean.getComment() : "");
		commonTemp.setStatus(ApprovalStatus.valueOf(approvalBean.getApprovalStatus()));

		if (!ApprovalStatus.VERIFIED.name().equalsIgnoreCase(approvalBean.getApprovalStatus())) {
			addToCommonRejected(approvalBean, commonTemp);
		}

		try {
			BeanUtils.copyProperties(commonTempHis, commonTemp);
			BeanUtils.copyProperties(tempDto, commonTemp);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("CommonTemp to CommonTempHis data mapping error {}", e);
			throw new SystemException(
					messageSource.getMessage(ErrorCode.DATA_COPY_ERROR, null, LocaleContextHolder.getLocale()),
					ErrorCode.DATA_COPY_ERROR);
		}
		commonTempHis.setId(null);
		commonTempHis.setTempId(commonTemp.getId());
		commonTempHis.setComment(null != approvalBean.getComment() ? approvalBean.getComment() : "");
		commonTempHisRepository.save(commonTempHis);
		logger.info("Insert new history record to commonTempHis table");

		commonTempRepository.delete(commonTemp);
		logger.info("Delete commonTemp record");

		commonResponse.setTempDto(tempDto);
		commonResponse.setReturnCode(HttpStatus.OK.value());
		commonResponse.setErrorCode(ErrorCode.OPARATION_SUCCESS);
		commonResponse.setReturnMessage(
				messageSource.getMessage(ErrorCode.OPARATION_SUCCESS, null, LocaleContextHolder.getLocale()));
		logger.info("updateCommonTemp success");
		return commonResponse;
	}

	private CommonResponse addToCommonRejected(ApprovalBean approvalBean, CommonTemp commonTemp) {
		CommonResponse commonResponse = new CommonResponse();
		logger.info("Start to Insert commonRejected");
		CommonRejected commonRejected = new CommonRejected();
		try {
			BeanUtils.copyProperties(commonRejected, commonTemp);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("CommonTemp to CommonRejected data mapping error {}", e);
			throw new SystemException(
					messageSource.getMessage(ErrorCode.DATA_COPY_ERROR, null, LocaleContextHolder.getLocale()),
					ErrorCode.DATA_COPY_ERROR);
		}
		commonRejected.setId(null);
		commonRejected.setComment(approvalBean.getComment());
		commonRejectedRepository.save(commonRejected);
		logger.info("successfily Insert to commonRejected");
		commonResponse.setReturnCode(HttpStatus.OK.value());
		commonResponse.setErrorCode(ErrorCode.OPARATION_SUCCESS);
		commonResponse.setReturnMessage(
				messageSource.getMessage(ErrorCode.OPARATION_SUCCESS, null, LocaleContextHolder.getLocale()));
		return commonResponse;
	}

	public CommonSearchBean getAuthPending(CommonSearchBean commonSearchBean) {
		String searchBy = commonSearchBean.getHashTags();
		String requestType = commonSearchBean.getRequestType();
		List<TempDto> list = new ArrayList<>();
		logger.info("Start getAuthPending record requestType: {}, status: {}, searchBy: {}", requestType,
				ApprovalStatus.PENDING, searchBy);
		String userId = commonSearchBean.getUserId();
		commonSearchBean.setUserId(null);
		commonSearchBean.setStatus(ApprovalStatus.PENDING);
		List<CommonTemp> commonTemps = tempCustomRepository.findTempRecordList(commonSearchBean);

		if (!commonTemps.isEmpty()) {
			logger.info("{} Temp records found", commonTemps.size());
			commonSearchBean.setUserId(userId);
			ApprovalResponse approvalResponse = getMyApprovals(commonSearchBean);

			for (CommonTemp commonTemp : commonTemps) {
				try {
					if (approvalResponse.getApprovals().stream()
							.anyMatch(o -> o.getApprovalId().equals(commonTemp.getApprovalId()))) {
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

	private ApprovalResponse getMyApprovals(CommonSearchBean commonSearchBean) {

		String requestId = "";
		logger.info("Start the getMyApprovals process - RequestId : {}", requestId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("request-id", requestId);
		headers.set("userGroup", commonSearchBean.getUserGroup());
		headers.set("userId", commonSearchBean.getUserId());
		HttpEntity<CreateApprovalRequest> entity = new HttpEntity<>(headers);
		try {
			String authPendinghListUrl = approvalServiceUrl + "/v1/my/approval?type="
					+ commonSearchBean.getRequestType();
			ResponseEntity<ApprovalResponse> responseEntity = restTemplate.exchange(authPendinghListUrl, HttpMethod.GET,
					entity, ApprovalResponse.class);
			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Failed to retrieve getMyApprovals(Status: {})", responseEntity.getStatusCode());
				throw new SystemException(responseEntity.getBody().getReturnMessage(),
						responseEntity.getBody().getErrorCode());
			} else {
				logger.info("successfuly fetch the getMyApprovals");
				return responseEntity.getBody();
			}
		} catch (Exception e) {
			throw new SystemException(
					messageSource.getMessage(ErrorCode.UNKNOWN_ERROR, null, LocaleContextHolder.getLocale()),
					ErrorCode.UNKNOWN_ERROR);
		}
	}

}
