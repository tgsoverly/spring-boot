/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.logging.logback;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;
import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Logback {@link Action} to support {@code <springProperty>} tags. Allows logback
 * properties to be sourced from the Spring environment.
 *
 * @author Phillip Webb
 */
class SpringPropertyAction extends Action {

	private static final String SOURCE_ATTRIBUTE = "source";

	private final Environment environment;

	public SpringPropertyAction(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void begin(InterpretationContext ic, String elementName, Attributes attributes)
			throws ActionException {
		String name = attributes.getValue(NAME_ATTRIBUTE);
		String source = attributes.getValue(SOURCE_ATTRIBUTE);
		Scope scope = ActionUtil.stringToScope(attributes.getValue(SCOPE_ATTRIBUTE));
		if (OptionHelper.isEmpty(name) || OptionHelper.isEmpty(source)) {
			addError("The \"name\" and \"source\" attributes of <springProperty> must be set");
		}
		ActionUtil.setProperty(ic, name, getValue(source), scope);
	}

	private String getValue(String source) {
		if (this.environment == null) {
			addWarn("No Spring Environment available to resolve " + source);
			return null;
		}
		String value = this.environment.getProperty(source);
		if (value != null) {
			return value;
		}
		int lastDot = source.lastIndexOf(".");
		if (lastDot > 0) {
			String prefix = source.substring(0, lastDot + 1);
			RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(
					this.environment, prefix);
			return resolver.getProperty(source.substring(lastDot + 1));
		}
		return null;
	}

	@Override
	public void end(InterpretationContext ic, String name) throws ActionException {
	}

}
