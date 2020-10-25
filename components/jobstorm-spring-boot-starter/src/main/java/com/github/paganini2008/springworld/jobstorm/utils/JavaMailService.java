package com.github.paganini2008.springworld.jobstorm.utils;

import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.github.paganini2008.devtools.StringUtils;

import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * JavaMailService
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class JavaMailService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;

	@Value("${jobstorm.mail.default.sender:jobstorm-supporter@yourmail.com}")
	private String defaultMailSender;

	@Value("${jobstorm.mail.default.subject:Job Interruption Warning}")
	private String defaultMailSubject;

	@Value("${jobstorm.mail.default.recipients:}")
	private String defaultRecipients;

	public void sendHtmlMail(String to, Map<String, Object> model) {
		try {
			MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage, true);
			mimeMessageHelper.setFrom(defaultMailSender);
			mimeMessageHelper.setTo(to.split(","));
			mimeMessageHelper.setSubject(defaultMailSubject);
			if (StringUtils.isNotBlank(defaultRecipients)) {
				mimeMessageHelper.setCc(defaultRecipients.split(","));
			}
			mimeMessageHelper.setText(getContent(model), true);
			javaMailSender.send(mimeMailMessage);
			log.info("Send mail to: {}", to);
		} catch (Exception e) {
			log.error("Failed to send html mail.", e.getMessage());
		}
	}

	private String getContent(Map<String, Object> model) throws Exception {
		Template template = freeMarkerConfigurer.getConfiguration().getTemplate("META-INF/static/email.template.ftl");
		return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
	}

}
