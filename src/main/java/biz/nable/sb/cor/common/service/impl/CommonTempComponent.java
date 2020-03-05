package biz.nable.sb.cor.common.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import biz.nable.sb.cor.common.bean.CommonRequestBean;
import biz.nable.sb.cor.common.bean.CommonResponseBean;
import biz.nable.sb.cor.common.bean.CommonSearchBean;
import biz.nable.sb.cor.common.bean.CommonTempBean;
import biz.nable.sb.cor.common.bean.FindByApprovalIdBean;
import biz.nable.sb.cor.common.bean.FindTempByRefBean;
import biz.nable.sb.cor.common.bean.TempDto;
import biz.nable.sb.cor.common.db.criteria.TempCustomRepository;
import biz.nable.sb.cor.common.db.entity.CommonTemp;
import biz.nable.sb.cor.common.db.entity.CommonTempHis;
import biz.nable.sb.cor.common.db.repository.CommonTempHisRepository;
import biz.nable.sb.cor.common.db.repository.CommonTempRepository;
import biz.nable.sb.cor.common.exception.InvalidRequestException;
import biz.nable.sb.cor.common.exception.RecordNotFoundException;
import biz.nable.sb.cor.common.exception.SystemException;
import biz.nable.sb.cor.common.request.CreateApprovalRequest;
import biz.nable.sb.cor.common.response.CreateApprovalResponse;
import biz.nable.sb.cor.common.utility.ActionTypeEnum;
import biz.nable.sb.cor.common.utility.ApprovalStatus;
import biz.nable.sb.cor.common.utility.ErrorCode;
import biz.nable.sb.cor.common.utility.SignatureComponent;

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
	private String approvalServiceUrl;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	MessageSource messageSource;

	@Transactional
	public CommonResponseBean createCommonTemp(CommonRequestBean commonRequestBean, String requestId,
			ActionTypeEnum actionType) {
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

		commonResponse = new CommonResponseBean();
		commonResponse.setErrorCode(ErrorCode.OPARATION_SUCCESS);
		commonResponse.setReturnCode(HttpStatus.OK.value());
		commonResponse.setReturnMessage(
				messageSource.getMessage(ErrorCode.OPARATION_SUCCESS, null, LocaleContextHolder.getLocale()));
		commonResponse.setCommonTempBean(commonRequestBean.getCommonTempBean());
		commonResponse.setTempId(String.valueOf(commonTemp.getId()));

		if (Boolean.FALSE.equals(isExisting)) {
			CreateApprovalResponse createApprovalResponse = createApproval(userId, userGroup, requestId, requestType,
					referenceNo, actionType);
			commonTemp.setApprovalId(createApprovalResponse.getApprovalBean().getApprovalId());
			commonTempHis.setApprovalId(createApprovalResponse.getApprovalBean().getApprovalId());

			commonTempRepository.save(commonTemp);
			logger.info("Update temp");
		}
		commonTempHisRepository.save(commonTempHis);
		logger.info("save to history");

		return commonResponse;
	}

	private CreateApprovalResponse createApproval(String userId, String userGroup, String requestId, String requestType,
			String referenceId, ActionTypeEnum actionType) {
		logger.info("Start send to create approval request");
		HttpHeaders headers = new HttpHeaders();
		headers.add("request-id", requestId);
		headers.add("userId", userId);
		headers.add("userGroup", userGroup);
		CreateApprovalRequest request = new CreateApprovalRequest();
		request.setApprovalStatus(ApprovalStatus.PENDING.name());
		request.setEnteredBy(userId);
		request.setUserGroup(userGroup);
		request.setReferenceId(referenceId);
		request.setType(requestType);
		request.setActionType(actionType.name());
		request.setEnteredDate(new Date());

		HttpEntity<CreateApprovalRequest> entity = new HttpEntity<>(request, headers);
		CreateApprovalResponse response = null;
		try {
			String createApprovalUrl = approvalServiceUrl + "/v1/approval";
			ResponseEntity<CreateApprovalResponse> responseEntity = restTemplate.postForEntity(createApprovalUrl,
					entity, CreateApprovalResponse.class);
			if (null != responseEntity.getBody()) {
				response = responseEntity.getBody();
			} else {
				logger.error("Create approval request reseved null responce");
				throw new SystemException(messageSource.getMessage(ErrorCode.CREATE_APPROVAL_ERROR, null,
						LocaleContextHolder.getLocale()), ErrorCode.CREATE_APPROVAL_ERROR);
			}
		} catch (HttpClientErrorException e) {
			logger.error("Error occred while creating Approval request: ", e);
			response = commonConverter.jsonToObject(e.getResponseBodyAsString(), CreateApprovalResponse.class);
			throw new SystemException(response.getReturnMessage(), String.valueOf(response.getReturnCode()));
		}
		logger.info("End create approval request successfuly");
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

	private CommonTemp getCommonTempToAuth(String referenceNo, String requestType, ActionTypeEnum actionType,
			String userId) {
		Optional<CommonTemp> optional = commonTempRepository.findByReferenceNoAndRequestTypeAndStatus(referenceNo,
				requestType, ApprovalStatus.PENDING);
		CommonTemp commonTemp;
		if (optional.isPresent()) {
			commonTemp = optional.get();
			if (!actionType.equals(commonTemp.getActionType())) {
				logger.info(messageSource.getMessage(ErrorCode.NOTHER_PENDING_APPROVE_RECORD_FOUND, null,
						LocaleContextHolder.getLocale()));
				throw new InvalidRequestException(
						messageSource.getMessage(ErrorCode.NOTHER_PENDING_APPROVE_RECORD_FOUND, null,
								LocaleContextHolder.getLocale()),
						ErrorCode.NOTHER_PENDING_APPROVE_RECORD_FOUND);
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

	public List<TempDto> getTempRecord(CommonSearchBean commonSearchBean) {
		String searchBy = commonSearchBean.getHashTags();
		String requestType = commonSearchBean.getRequestType();
		List<TempDto> list = new ArrayList<>();
		logger.info("Start fetching temp record requestType: {}, status: {}, searchBy: {}", requestType,
				ApprovalStatus.PENDING, searchBy);
		commonSearchBean.setStatus(ApprovalStatus.PENDING);
		List<CommonTemp> commonTemps = tempCustomRepository.findTempRecordList(commonSearchBean);

		if (!commonTemps.isEmpty()) {
			logger.info("{} Temp records found", commonTemps.size());
			for (CommonTemp commonTemp : commonTemps) {
				TempDto dto = new TempDto();
				try {
					BeanUtils.copyProperties(dto, commonTemp);
					dto.setSignature(signatureComponent.genarateSignature(commonTemp));
				} catch (Exception e) {
					throw new InvalidRequestException(
							messageSource.getMessage(ErrorCode.DATA_COPY_ERROR, null, LocaleContextHolder.getLocale()),
							ErrorCode.DATA_COPY_ERROR);
				}
				list.add(dto);
			}
		} else {
			String msg = messageSource.getMessage(ErrorCode.NO_TEMP_RECORD_FOUND, null,
					LocaleContextHolder.getLocale());
			logger.error(msg);
			throw new RecordNotFoundException(msg, ErrorCode.NO_TEMP_RECORD_FOUND);
		}
		return list;
	}

	public TempDto getCommonTempByRef(FindTempByRefBean findTempByRefBean) {
		String referenceNo = findTempByRefBean.getReferenceNo();
		String requestType = findTempByRefBean.getRequestType();
		Optional<CommonTemp> optional = commonTempRepository.findByReferenceNoAndRequestTypeAndStatus(referenceNo,
				requestType, ApprovalStatus.PENDING);
		CommonTemp commonTemp;
		if (optional.isPresent()) {
			commonTemp = optional.get();
			TempDto tempDto = new TempDto();
			try {
				BeanUtils.copyProperties(tempDto, commonTemp);
				tempDto.setSignature(signatureComponent.genarateSignature(commonTemp));
			} catch (Exception e) {
				throw new InvalidRequestException(
						messageSource.getMessage(ErrorCode.DATA_COPY_ERROR, null, LocaleContextHolder.getLocale()),
						ErrorCode.DATA_COPY_ERROR);
			}
			return tempDto;
		} else {
			throw new RecordNotFoundException(
					messageSource.getMessage(ErrorCode.NO_TEMP_RECORD_FOUND, null, LocaleContextHolder.getLocale()),
					ErrorCode.NO_TEMP_RECORD_FOUND);
		}
	}

	public List<TempDto> getAuthPendingList(CommonSearchBean commonSearchBean) {
		String searchBy = commonSearchBean.getHashTags();
		String requestType = commonSearchBean.getRequestType();
		List<TempDto> list = new ArrayList<>();
		logger.info("Start fetching getAuthPendingList requestType: {}, status: {}, searchBy: {}", requestType,
				ApprovalStatus.PENDING, searchBy);

		commonSearchBean.setStatus(ApprovalStatus.PENDING);
		List<CommonTemp> commonTemps = tempCustomRepository.findTempRecordList(commonSearchBean);

		if (!commonTemps.isEmpty()) {
			logger.info("{} Temp records found", commonTemps.size());
			for (CommonTemp commonTemp : commonTemps) {
				TempDto dto = new TempDto();
				try {
					BeanUtils.copyProperties(dto, commonTemp);
					dto.setSignature(signatureComponent.genarateSignature(commonTemp));
				} catch (Exception e) {
					throw new InvalidRequestException(
							messageSource.getMessage(ErrorCode.DATA_COPY_ERROR, null, LocaleContextHolder.getLocale()),
							ErrorCode.DATA_COPY_ERROR);
				}
				list.add(dto);
			}
		} else {
			String msg = messageSource.getMessage(ErrorCode.NO_TEMP_RECORD_FOUND, null,
					LocaleContextHolder.getLocale());
			logger.error(msg);
			throw new RecordNotFoundException(msg, ErrorCode.NO_TEMP_RECORD_FOUND);
		}
		return list;
	}

	public TempDto getCommonTempByApproveId(FindByApprovalIdBean findTempByApprovalIdBean) {
		String approvalId = findTempByApprovalIdBean.getApprovalId();
		Optional<CommonTemp> optional = commonTempRepository.findByApprovalIdAndStatus(approvalId,
				ApprovalStatus.PENDING);
		CommonTemp commonTemp;
		if (optional.isPresent()) {
			commonTemp = optional.get();
			TempDto tempDto = new TempDto();
			try {
				BeanUtils.copyProperties(tempDto, commonTemp);
				tempDto.setSignature(signatureComponent.genarateSignature(commonTemp));
			} catch (Exception e) {
				throw new InvalidRequestException(
						messageSource.getMessage(ErrorCode.DATA_COPY_ERROR, null, LocaleContextHolder.getLocale()),
						ErrorCode.DATA_COPY_ERROR);
			}
			return tempDto;
		} else {
			throw new RecordNotFoundException(
					messageSource.getMessage(ErrorCode.NO_TEMP_RECORD_FOUND, null, LocaleContextHolder.getLocale()),
					ErrorCode.NO_TEMP_RECORD_FOUND);
		}
	}
}
