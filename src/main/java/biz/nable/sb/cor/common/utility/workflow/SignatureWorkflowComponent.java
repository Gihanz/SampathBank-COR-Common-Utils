package biz.nable.sb.cor.common.utility.workflow;

import biz.nable.sb.cor.common.db.entity.CommonTemp;
import biz.nable.sb.cor.common.db.entity.workflow.CommonTempWorkflow;
import biz.nable.sb.cor.common.exception.InvalidRequestException;
import biz.nable.sb.cor.common.exception.SystemException;
import biz.nable.sb.cor.common.utility.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class SignatureWorkflowComponent {
	@Autowired
	private MessageSource messageSource;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public String genarateSignature(CommonTempWorkflow commonTemp) {
		String signatureData = commonTemp.getWorkflowId().concat(commonTemp.getReferenceId())
				.concat(commonTemp.getType()).concat(commonTemp.getCompanyId())
				.concat(commonTemp.getLastUpdatedDate().toString());
		logger.info(">>>>>>>>>>>>>> Start genrateSignature <<<<<<<<<<<<<<<<<<");
		String signature = "";

		logger.info("genrateSignature data : {}", signatureData);
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] signatureBytes = digest.digest(signatureData.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : signatureBytes) {
				sb.append(String.format("%02x", b));
			}
			signature = sb.toString();
			logger.info(">>>>>>>>>>>>>>>>>>>>> genrated Signature SHA-256 : {}", signature);

		} catch (NoSuchAlgorithmException e) {
			throw new SystemException(
					messageSource.getMessage(ErrorCode.UNKNOWN_ERROR, null, LocaleContextHolder.getLocale()),
					ErrorCode.UNKNOWN_ERROR);
		}

		return signature;
	}

	public Boolean validateSignature(String originalSignature, CommonTempWorkflow commonTemp) {
		String signature = genarateSignature(commonTemp);
		if (!originalSignature.equals(signature)) {
			throw new InvalidRequestException(messageSource.getMessage(ErrorCode.SIGNATURE_VARIFICATION_FAILED, null,
					LocaleContextHolder.getLocale()), ErrorCode.SIGNATURE_VARIFICATION_FAILED);
		} else {
			return true;
		}
	}
}
