package com.clinomics.util;

import java.util.List;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.clinomics.entity.seq.Member;
import com.clinomics.entity.seq.Sample;
import com.clinomics.repository.seq.MemberRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Component
public class EmailSender {

    @Autowired
    private JavaMailSender javaMailSender;
	
	@Autowired
	MemberRepository memberRepository;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendMailToFail(List<Sample> failSamples) {
        try {
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);

            List<Member> members = memberRepository.findByInUseTrueAndIsFailedMailSentTrue();

            helper.setSubject("Chip 분석 실패 메일");

            List<String> emails = members.stream()
                .map(m -> m.getEmail())
                .collect(Collectors.toList());
            
            if (emails.size() < 1) {
                emails.add("eastpeople@clinomics.co.kr");
                emails.add("dlee@clinomics.co.kr");
            }

            helper.setTo(emails.toArray(new String[emails.size()]));

            Context context =  new Context();
            context.setVariable("samples", failSamples);

            String html = templateEngine.process("mail/template", context);
            helper.setText(html, true);
            
            javaMailSender.send(msg);

        } catch (MessagingException e) {
            
            e.printStackTrace();
        }
        
    }
}