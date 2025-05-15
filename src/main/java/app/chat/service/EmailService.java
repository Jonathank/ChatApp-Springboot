/**
 * 
 */
package app.chat.service;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/**
 * @author JONATHAN
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    
    public void sendVerificationOtpEmail(String userEmail, String otp,
	    String subject, String message) {
	try {
	   MimeMessage  mimeMessage = javaMailSender.createMimeMessage();
	   MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
	   helper.setSubject(subject);
	   helper.setTo(userEmail);
	   helper.setText(message);
	   javaMailSender.send(mimeMessage);
	   
	} catch (MailException | MessagingException e) {
	    throw new MailSendException("Failed to send email to " + userEmail);
	}
    }
}
