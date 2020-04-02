/*******************************************************************************
 * Copyright N*able, 2020
 *******************************************************************************/
package biz.nable.sb.cor.common.aspect.workflow;

import biz.nable.sb.cor.common.annotation.TempRecord;
import biz.nable.sb.cor.common.bean.CommonRequestBean;
import biz.nable.sb.cor.common.bean.CommonResponseBean;
import biz.nable.sb.cor.common.service.impl.workflow.CommonTempComponent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author Shehan
 *
 */
@Aspect
@Order(2)
@Component
public class TempImplAspect {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	CommonTempComponent commonTempComponent;

	@Around(value = "@annotation(tempRecord)")
	public Object around(ProceedingJoinPoint joinPoint, TempRecord tempRecord) throws Throwable {
		logger.info("Start TempRecord Annotation");
		Object result = joinPoint.proceed(joinPoint.getArgs());
		if (((CommonResponseBean) result).getReturnCode() != HttpStatus.OK.value()) {
			return result;
		}
		Object[] args = joinPoint.getArgs();

		CommonRequestBean commonRequestBean = (CommonRequestBean) args[0];
		String requestId = (String) args[1];
		return commonTempComponent.createCommonTemp(commonRequestBean, requestId);
	}

}
