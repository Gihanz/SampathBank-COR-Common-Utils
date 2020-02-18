/*******************************************************************************
 * Copyright N*able, 2020
 *******************************************************************************/
package biz.nable.sb.cor.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import biz.nable.sb.cor.common.annotation.FindTempRecordByRef;
import biz.nable.sb.cor.common.annotation.TempRecord;
import biz.nable.sb.cor.common.bean.CommonRequestBean;
import biz.nable.sb.cor.common.bean.CommonResponseBean;
import biz.nable.sb.cor.common.bean.CommonSearchBean;
import biz.nable.sb.cor.common.bean.FindTempByRefBean;
import biz.nable.sb.cor.common.service.impl.CommonTempComponent;
import biz.nable.sb.cor.common.utility.ActionTypeEnum;

/**
 * @author Roshan Wijendra
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
		logger.info("Start TempRecord annotation");
		Object result = joinPoint.proceed(joinPoint.getArgs());
		if (((CommonResponseBean) result).getReturnCode() != HttpStatus.OK.value()) {
			return result;
		}
		Object[] args = joinPoint.getArgs();

		CommonRequestBean commonRequestBean = (CommonRequestBean) args[0];
		String requestId = (String) args[1];
		ActionTypeEnum actionType = tempRecord.actionType();
		return commonTempComponent.createCommonTemp(commonRequestBean, requestId, actionType);
	}

	@Around(value = "@annotation(biz.nable.sb.cor.common.annotation.FindTempRecord)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		logger.info("Start FindTempRecord List annotation");
		joinPoint.proceed(joinPoint.getArgs());

		Object[] args = joinPoint.getArgs();
		CommonSearchBean commonRequestBean = (CommonSearchBean) args[0];

		CommonSearchBean bean = new CommonSearchBean();
		bean.setTempList(commonTempComponent.getTempRecord(commonRequestBean));
		return bean;
	}

	@Around(value = "@annotation(findTempRecordByRef)")
	public Object around(ProceedingJoinPoint joinPoint, FindTempRecordByRef findTempRecordByRef) throws Throwable {
		logger.info("Start FindTempRecordByRef annotation");
		joinPoint.proceed(joinPoint.getArgs());

		Object[] args = joinPoint.getArgs();
		FindTempByRefBean findTempByRefBean = (FindTempByRefBean) args[0];

		return commonTempComponent.getCommonTempByRef(findTempByRefBean);
	}

}
