/*******************************************************************************
 * Copyright N*able, 2020
 *******************************************************************************/
package biz.nable.sb.cor.common.aspect.workflow;

import biz.nable.sb.cor.common.bean.workflow.CommonSearchBean;
import biz.nable.sb.cor.common.service.impl.workflow.WorkflowComponent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Roshan Wijendra
 *
 */
@Aspect
@Order(3)
@Component
public class WorkflowAspect {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
    WorkflowComponent workflowComponent;

	@Around(value = "@annotation(biz.nable.sb.cor.common.annotation.workflow.WorkflowDetails)")
	public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
		logger.info("Start WorkflowDetails annotation");
		joinPoint.proceed(joinPoint.getArgs());

		Object[] args = joinPoint.getArgs();
		CommonSearchBean commonSearchBean = (CommonSearchBean) args[0];

		return workflowComponent.getWorkflowDetails(commonSearchBean);
	}

}
