package org.example.stationery_shop.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.repository.MailRepository;
import org.example.stationery_shop.repository.UserRepository;
import org.example.stationery_shop.service.MailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;
    private final MailRepository mailRepository;
    private final UserRepository userRepository;

    @Override
    public void sendVerifyMail(String to, String verifyLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Xác nhận đăng ký tài khoản");
        msg.setText("""
                Chào bạn,
                
                Vui lòng bấm link sau để xác nhận email:
                %s
                
                Link sẽ hết hạn sau một thời gian.
                """.formatted(verifyLink));

        mailSender.send(msg);
    }
}

