/*******************************************************************************
 * Copyright N*able, 2020
 *******************************************************************************/
package biz.nable.sb.cor.common.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import biz.nable.sb.cor.common.utility.ActionTypeEnum;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * @author Roshan Wijendra
 *
 */
public @interface TempRecord {

	ActionTypeEnum actionType();

}
