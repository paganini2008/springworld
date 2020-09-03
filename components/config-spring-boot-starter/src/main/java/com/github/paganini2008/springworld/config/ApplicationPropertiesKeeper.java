package com.github.paganini2008.springworld.config;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.StringValueResolver;

import com.github.paganini2008.devtools.ObjectUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
import com.github.paganini2008.devtools.collection.PropertyChangeEvent;
import com.github.paganini2008.devtools.collection.PropertyChangeListener;
import com.github.paganini2008.devtools.reflection.FieldFilters;
import com.github.paganini2008.devtools.reflection.FieldUtils;
import com.github.paganini2008.devtools.regex.RegexUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ApplicationPropertiesKeeper
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ApplicationPropertiesKeeper implements BeanPostProcessor, EmbeddedValueResolverAware, ApplicationContextAware {

	private static final String DEFAULT_PLACEHOLDER_PREFIX = "@{";

	private static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final String DEFAULT_PLACEHOLDER_PATTERN = "\\@\\{(.*)\\}";

	@Autowired
	private ApplicationProperties applicationProperties;

	private ApplicationContext applicationContext;

	private StringValueResolver stringValueResolver;

	@Override
	public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
		List<Field> fields = FieldUtils.getFields(bean.getClass(), FieldFilters.isAnnotationPresent(Value.class));
		if (CollectionUtils.isEmpty(fields)) {
			return bean;
		}
		for (final Field field : fields) {
			final String propertyName = field.getName();
			Value valueAnn = field.getAnnotation(Value.class);
			String value = valueAnn.value();
			if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
				String key = resolvePlaceholder(value);
				String strVal = "${" + key + "}";
				String resultVal = stringValueResolver.resolveStringValue(strVal);
				BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(bean);
				bw.setPropertyValue(propertyName, resultVal);

				applicationProperties.addEventListener(new PropertyChangeListener<Properties>() {

					public void onEventFired(PropertyChangeEvent<Properties> event) {
						Properties latest = event.getLatestVersion();
						Properties current = event.getCurrentVersion();
						Map<Object, Object> difference = MapUtils.compareDifference(latest, current);
						if (difference != null && difference.containsKey(key)) {
							Object previousValue = null;
							Object currentValue = stringValueResolver.resolveStringValue(strVal);
							try {
								BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(bean);
								bw.setPropertyValue(propertyName, currentValue);
								currentValue = bw.getPropertyValue(propertyName);
								if (ObjectUtils.equals(previousValue, currentValue)) {
									log.warn("[BeanPropertyChange] It seems true that property name '" + propertyName
											+ "' overridden by other PropertySource. So there is nothing to happen on the property.");
								} else {
									log.info("[BeanPropertyChange] propertyName: " + propertyName + ", previousValue: " + previousValue
											+ ", currentValue: " + currentValue);
									applicationContext.publishEvent(
											new BeanPropertyChangeEvent(bean, beanName, propertyName, previousValue, currentValue));
								}

							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}

						}
					}
				});
			}
		}
		return bean;
	}

	private static String resolvePlaceholder(String represent) {
		String key = RegexUtils.match(represent, DEFAULT_PLACEHOLDER_PATTERN, 0, 1, 0);
		int index = key.indexOf(':');
		if (index > 0) {
			key = key.substring(0, index);
		}
		return key;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
		this.stringValueResolver = stringValueResolver;
	}

}
