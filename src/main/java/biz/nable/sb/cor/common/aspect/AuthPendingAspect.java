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

import biz.nable.sb.cor.common.bean.CommonSearchBean;
import biz.nable.sb.cor.common.service.impl.ApprovalComponent;

/**
 * @author Roshan Wijendra
 *
 */
@Aspect
@Order(3)
@Component
public class AuthPendingAspect {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	ApprovalComponent approvalComponent;

	@Around(value = "@annotation(biz.nable.sb.cor.common.annotation.AuthPending)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		logger.info("Start AuthPending annotation");
		joinPoint.proceed(joinPoint.getArgs());

		Object[] args = joinPoint.getArgs();
		CommonSearchBean commonSearchBean = (CommonSearchBean) args[0];

		return approvalComponent.getAuthPending(commonSearchBean);
	}

}
