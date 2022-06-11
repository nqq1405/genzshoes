package com.quyquang.genzshoes3.controller.shop;

import com.quyquang.genzshoes3.config.Constants;
import com.quyquang.genzshoes3.entity.Provider;
import com.quyquang.genzshoes3.entity.User;
import com.quyquang.genzshoes3.entity.Verify;
import com.quyquang.genzshoes3.exception.BadRequestException;
import com.quyquang.genzshoes3.model.dto.UserDTO;
import com.quyquang.genzshoes3.model.mapper.UserMapper;
import com.quyquang.genzshoes3.model.request.ChangePasswordRequest;
import com.quyquang.genzshoes3.model.request.CreateUserRequest;
import com.quyquang.genzshoes3.model.request.LoginRequest;
import com.quyquang.genzshoes3.model.request.UpdateProfileRequest;
import com.quyquang.genzshoes3.repository.UserRepository;
import com.quyquang.genzshoes3.repository.VerifyRepository;
import com.quyquang.genzshoes3.security.CustomUserDetails;
import com.quyquang.genzshoes3.security.JwtTokenUtil;
import com.quyquang.genzshoes3.service.UserService;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

import static com.quyquang.genzshoes3.config.Constants.MAX_AGE_COOKIE;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VerifyRepository verifyRepository;

    @Value("${spring.mail.username}")
    private String sender;

    @GetMapping("/users")
    public ResponseEntity<Object> getListUsers() {
        List<UserDTO> userDTOS = userService.getListUsers();
        return ResponseEntity.ok(userDTOS);
    }

    @PostMapping("/api/admin/users")
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        User user = userService.createUser(createUserRequest);
        return ResponseEntity.ok(UserMapper.toUserDTO(user));
    }

    @PostMapping("/api/register")
    public ResponseEntity<Object> register(@Valid @RequestBody CreateUserRequest createUserRequest, HttpServletResponse response) {
        //Create user
        User user = userService.createUser(createUserRequest);

        //Gen token
        UserDetails principal = new CustomUserDetails(user);
        String token = jwtTokenUtil.generateToken(principal);

        //Add token on cookie to login
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setMaxAge(MAX_AGE_COOKIE);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(UserMapper.toUserDTO(user));
    }

    @PostMapping("/api/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        //Authenticate
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            ));
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

            List<String> roles = user.getUser().getRoles();
            if(roles.contains("ADMIN"))
                throw new BadRequestException("Email hoặc mật khẩu không chính xác!");
//            if(user.getUser().getProvider() == Provider.GOOGLE)
//                throw new BadRequestException("Tài khoản của bạn trước đó đã đăng nhâp bằng google hãy sử dụng cách thức đăng nhập bằng google để tiếp tục.");

            //Gen token
            String token = jwtTokenUtil.generateToken((CustomUserDetails) authentication.getPrincipal());

            //Add token to cookie to login
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setMaxAge(MAX_AGE_COOKIE);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return ResponseEntity.ok(UserMapper.toUserDTO(((CustomUserDetails)authentication.getPrincipal()).getUser()));
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception ex) {
            throw new BadRequestException("Email hoặc mật khẩu không chính xác!");
        }
    }

    @GetMapping("/tai-khoan")
    public String getProfilePage(Model model) {
        return "shop/account";
    }

    @PostMapping("/api/change-password")
    public ResponseEntity<Object> changePassword(@Valid @RequestBody ChangePasswordRequest passwordReq) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        userService.changePassword(user, passwordReq);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @PutMapping("/api/update-profile")
    public ResponseEntity<Object> updateProfile(@Valid @RequestBody UpdateProfileRequest profileReq) {
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        user = userService.updateProfile(user, profileReq);
        UserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ResponseEntity.ok("Cập nhật thành công");
    }

    // onerror="this.src='shop/images/default.png'"
    // forgot password
    @GetMapping("/forgot-password")
    public String getShowForgotPasswordFrom() {
        return "shop/forgot_password";
    }

    @PostMapping("/forgot-password")
    public String postProcessForgotPassword(HttpServletRequest req, Model model) {
        String email = req.getParameter("email");
        String token = RandomString.make(35);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "không tìm thấy email của bạn.");
            return "shop/forgot_password";
        }
        try {
            String resetPasswordLink = getSiteURL(req) + "/reset_password?token=" + token;
            sendEmail(email, resetPasswordLink);
            Verify verify = new Verify();
            verify.setUser(user);
            verify.setToken(token);
            verify.setExpiredAt(LocalDateTime.now().plusMinutes(Constants.TIME_EXPIRED_VERIFY));
            verifyRepository.save(verify);
            model.addAttribute("message", "chúng tôi đã gửi 1 email đặt lại mật khẩu đến email của bạn.");
        } catch (UnsupportedEncodingException | MessagingException  e) {
            System.out.println(e);
            model.addAttribute("error", "Error while sending email");
        } catch (Exception e) {
            System.out.println(e);
        }
        return "shop/forgot_password";
    }

    private void sendEmail(String recipientEmail, String link) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(sender, "Genz Shoes");
        helper.setTo(recipientEmail);

        String subject = "Đặt lại mật khẩu";
        String content = "<p>Xin chào,</p>"
                + "<p>Bạn đã yêu cầu đặt lại mật khẩu của mình.</p>"
                + "<p>Nhấp vào liên kết bên dưới để thay đổi mật khẩu của bạn:</p>"
                + "<p><a href=\"" + link + "\">Đặt lại mật khẩu</a></p>"
                + "<p>hiệu lực của link đặt lại mật khẩu là 15 phút.</p>"
                + "<br>"
                + "<p>Bỏ qua email này nếu bạn nhớ mật khẩu của mình,"
                + "hoặc bạn đã không thực hiện yêu cầu.</p>";
        helper.setSubject(subject);
        helper.setText(content, true);
        javaMailSender.send(message);
    }

    private String getSiteURL(HttpServletRequest req) {
        return req.getRequestURL().toString()
                .replace(req.getServletPath(), "");
    }

    @GetMapping("/reset_password")
    public String getShowResetPasswordForm(@Param(value = "token") String token, Model model) {
        Verify verify = verifyRepository.findByToken(token);
        if (token.isEmpty() || verify == null) {
            model.addAttribute("title", "Không tìm thấy");
            model.addAttribute("message", "Xác thực không thành công kiểm tra lại email chúng tôi đã gửi cho bạn.");
            return "shop/message_password";
        }
        if (LocalDateTime.now().isAfter(verify.getExpiredAt())) {
            model.addAttribute("title", "Time out");
            model.addAttribute("message", "link của bạn đã hết hạn.");
            return "shop/message_password";
        }
        return "shop/reset_password";

    }

    @PostMapping("/reset_password")
    public String postProcessResetPassword(HttpServletRequest req, Model model) {
        String token = req.getParameter("token");
        String password = req.getParameter("password");
        Verify verify = verifyRepository.findByToken(token);
        User user = null;
        model.addAttribute("title", "Đặt lại mật khẩu của bạn.");

        if (verify == null) {
            model.addAttribute("title", "Không tìm thấy thông tin yêu cầu ");
            model.addAttribute("message", "Invalid Token");
            return "shop/message_password";
        } else {
            user = verify.getUser();
            userService.resetPassword(user, password);
            verifyRepository.delete(verify);
            model.addAttribute("message", "Bạn đã thay đổi thành công mật khẩu của bạn.");
            model.addAttribute("trangchu", "<br/> <a href='/'>Trang chu<a/>");
        }
        return "shop/message_password";
    }
}
