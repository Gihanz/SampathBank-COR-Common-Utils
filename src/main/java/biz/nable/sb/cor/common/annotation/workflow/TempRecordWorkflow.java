/*******************************************************************************
 * Copyright N*able, 2020
 *******************************************************************************/
package biz.nable.sb.cor.common.annotation.workflow;

import biz.nable.sb.cor.common.utility.ActionTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * @author shehan
 *
 */
public @interface TempRecordWorkflow {

	ActionTypeEnum actionType();

}
