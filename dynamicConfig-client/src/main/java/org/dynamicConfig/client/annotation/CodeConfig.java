/**
 *
 */
package org.dynamicConfig.client.annotation;

import java.lang.annotation.*;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CodeConfig {
	String key() default "";

	String groupID() default "";

	String defaultValue() default "";
}
