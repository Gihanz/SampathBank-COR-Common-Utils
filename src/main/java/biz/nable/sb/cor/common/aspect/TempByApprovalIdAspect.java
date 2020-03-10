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
import org.springframework.stereotype.Component;

import biz.nable.sb.cor.common.bean.FindByApprovalIdBean;
import biz.nable.sb.cor.common.service.impl.CommonTempComponent;

/**
 * @author Roshan Wijendra
 *
 */
@Aspect
@Order(2)
@Component
public class TempByApprovalIdAspect {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	CommonTempComponent commonTempComponent;

	@Around(value = "@annotation(biz.nable.sb.cor.common.annotation.FindByApprovalId)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		logger.info("Start FindTempRecordByApprovalId annotation");
		joinPoint.proceed(joinPoint.getArgs());

		Object[] args = joinPoint.getArgs();
		FindByApprovalIdBean findTempByApprovalIdBean = (FindByApprovalIdBean) args[0];

		return commonTempComponent.getCommonTempByApproveId(findTempByApprovalIdBean);
	}

}
