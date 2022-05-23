package com.quyquang.genzshoes3;

import com.quyquang.genzshoes3.entity.User;
import com.quyquang.genzshoes3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.mail.internet.MimeMessage;
import java.util.List;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class Genzshoes3Application {

//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    PasswordEncoder bCryptPasswordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(Genzshoes3Application.class, args);
    }

    @Bean
    CommandLineRunner runner(){
        return args -> {
//            var mailMessage = new SimpleMailMessage();
//
//            mailMessage.setTo("dothihien20211@gmail.com");
//            mailMessage.setSubject("Reset pass");
//            mailMessage.setText("text 1 message999999");
//
//            mailMessage.setFrom("resetpass1405@gmail.com");
//
//            javaMailSender.send(mailMessage);

//            User user = new User();
//            user.setId(10L);
//            user.setPhone("0969708715");
//            user.setPassword(bCryptPasswordEncoder.encode("admin2@123"));
//            user.setAddress("abc");
//            user.setAvatar(null);
//            user.setEmail("admin2@gmail.com");
//            user.setRoles(List.of("ADMIN","USER"));
//            user.setStatus(true);
//
//
//            userRepository.save(user);
        };
    }
}
