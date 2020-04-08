/*******************************************************************************
 * Copyright N*able, 2020
 *******************************************************************************/
package biz.nable.sb.cor.common.aspect.workflow;

import biz.nable.sb.cor.common.annotation.workflow.TempRecordWorkflow;
import biz.nable.sb.cor.common.bean.workflow.CommonRequestWorkflowBean;
import biz.nable.sb.cor.common.bean.workflow.CommonResponseWorkflowBean;
import biz.nable.sb.cor.common.service.impl.workflow.CommonTempWorkflowComponent;
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
public class TempImplWorkflowAspect {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	CommonTempWorkflowComponent commonTempWorkflowComponent;

	@Around(value = "@annotation(tempRecordWorkflow)")
	public Object around(ProceedingJoinPoint joinPoint, TempRecordWorkflow tempRecordWorkflow) throws Throwable {
		logger.info("Start TempRecordWorkflow Annotation");
		Object result = joinPoint.proceed(joinPoint.getArgs());
		if (((CommonResponseWorkflowBean) result).getReturnCode() != HttpStatus.OK.value()) {
			return result;
		}
		Object[] args = joinPoint.getArgs();

		CommonRequestWorkflowBean commonRequestWorkflowBean = (CommonRequestWorkflowBean) args[0];
		String requestId = (String) args[1];
		return commonTempWorkflowComponent.createCommonTemp(commonRequestWorkflowBean, requestId);
	}

}
