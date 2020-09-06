package com.github.paganini2008.springworld.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.github.paganini2008.devtools.ObjectUtils;
import com.github.paganini2008.devtools.collection.CollectionUtils;
import com.github.paganini2008.devtools.collection.MapUtils;
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
public class ApplicationPropertiesKeeper implements BeanPostProcessor, ApplicationContextAware {

	private static final String DEFAULT_PLACEHOLDER_PREFIX = "@{";

	private static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private static final String DEFAULT_PLACEHOLDER_PATTERN = "\\@\\{(.*)\\}";

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private InternalStringValueResolver stringValueResolver;

	private ApplicationContext applicationContext;

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		List<Field> fields = FieldUtils.getFields(bean.getClass(), FieldFilters.isAnnotationPresent(Value.class));
		if (CollectionUtils.isEmpty(fields)) {
			return bean;
		}
		List<String> effectedKeys = new ArrayList<String>();
		for (final Field field : fields) {
			Value valueAnn = field.getAnnotation(Value.class);
			final String value = valueAnn.value();
			if (value.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && value.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
				final String key = resolvePlaceholder(value);
				effectedKeys.add(key);
				applicationProperties.addEventListener(event -> {
					Properties latest = event.getLatestVersion();
					Properties current = event.getCurrentVersion();
					Map<Object, Object> difference = MapUtils.compareDifference(latest, current);
					if (difference != null && difference.containsKey(key)) {
						Object previousValue = FieldUtils.readField(bean, field);
						Object currentValue = stringValueResolver.resolveStringValue(value);
						currentValue = stringValueResolver.getBeanFactory().getTypeConverter().convertIfNecessary(currentValue,
								field.getType(), field);
						try {
							FieldUtils.writeField(bean, field, currentValue);
							currentValue = FieldUtils.readField(bean, field);

							if (ObjectUtils.equals(previousValue, currentValue)) {
								log.warn("[BeanPropertyChange] It seems true that field '" + field.getName()
										+ "' overridden by other PropertySource. So there is nothing to happen on the property.");
							} else {
								log.info("[BeanPropertyChange] field: " + field.getName() + ", previousValue: " + previousValue
										+ ", currentValue: " + currentValue);
								applicationContext.publishEvent(
										new BeanPropertyChangeEvent(bean, beanName, field.getName(), previousValue, currentValue));
								log.info("[BeanPropertyChange] Publish BeanPropertyChangeEvent ok.");
							}

						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}

					}
				});

				if (log.isTraceEnabled()) {
					log.trace("Keep watching application property: {}", key);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(effectedKeys)) {
			applicationProperties.addEventListener(event -> {
				Properties latest = event.getLatestVersion();
				Properties current = event.getCurrentVersion();
				Map<Object, Object> difference = MapUtils.compareDifference(latest, current);
				if (MapUtils.isNotEmpty(difference)) {
					for (String key : effectedKeys) {
						if (difference.containsKey(key)) {
							applicationContext.publishEvent(new BeanObjectChangeEvent(bean, beanName));
							log.info("[BeanPropertyChange] Publish BeanChangeEvent ok.");
							break;
						}
					}
				}
			});
		}
		return bean;
	}

	private static String resolvePlaceholder(String represent) {
		String key = RegexUtils.match(represent, DEFAULT_PLACEHOLDER_PATTERN, 0, 1, 0);
		int index = key.indexOf(':');
		if (index > 0) {
			key = key.substring(0, index);
		}
		return key.trim();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
