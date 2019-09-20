package com.batchRun.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;

public class MailMail
{
	private JavaMailSender mailSender;
	private SimpleMailMessage simpleMailMessage;
	private VelocityEngine velocityEngine;

	public void setSimpleMailMessage(SimpleMailMessage simpleMailMessage) {
		this.simpleMailMessage = simpleMailMessage;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public void sendMail(final TreeSet<String> name, final TreeSet<String> email, final TreeSet<Integer> days) {
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
            	String text ="";
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);

                message.setTo(email.last());
                message.setFrom(simpleMailMessage.getFrom()); 
                message.setSubject(simpleMailMessage.getSubject());
                Map<String, Object> model = new HashMap();
                model.put("user", name.last());
                if(days.last() == 20) {
                	text = VelocityEngineUtils.mergeTemplateIntoString(
                			velocityEngine, "finalReminderTemplate.vm", model);
                }else {
                	text = VelocityEngineUtils.mergeTemplateIntoString(
                			velocityEngine, "reminderTemplate.vm", model);
                }
                message.setText(text, true);
            }
        };
        this.mailSender.send(preparator);
        System.out.println("Mail Sent "+name.last());
	}
}
