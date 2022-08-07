package com.quyquang.genzshoes3.controller.admin;

import static com.quyquang.genzshoes3.config.Constants.MAX_AGE_COOKIE;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quyquang.genzshoes3.entity.User;
import com.quyquang.genzshoes3.security.CustomUserDetails;
import com.quyquang.genzshoes3.security.JwtTokenUtil;
import com.quyquang.genzshoes3.service.UserService;

@Controller
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/admin/users")
    public String homePages(Model model,
                            @RequestParam(defaultValue = "", required = false) String fullName,
                            @RequestParam(defaultValue = "", required = false) String phone,
                            @RequestParam(defaultValue = "", required = false) String email,
                            @RequestParam(defaultValue = "", required = false) String address,
                            @RequestParam(defaultValue = "1", required = false) Integer page) {
        Page<User> users = userService.adminListUserPages(fullName, phone, email, page);
        model.addAttribute("users", users.getContent());
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("currentPage", users.getPageable().getPageNumber() + 1);
        return "admin/user/list";
    }

    @GetMapping("/api/admin/users/list")
    public ResponseEntity<Object> getListUserPages(@RequestParam(defaultValue = "", required = false) String fullName,
                                                   @RequestParam(defaultValue = "", required = false) String phone,
                                                   @RequestParam(defaultValue = "", required = false) String email,
                                                   @RequestParam(defaultValue = "", required = false) String address,
                                                   @RequestParam(defaultValue = "1", required = false) Integer page) {
        Page<User> users = userService.adminListUserPages(fullName, phone, email, page);
        return ResponseEntity.ok(users);
    }

    // login admin
    @GetMapping("/login/admin")
    public String getShowFormLogin(){
        return "admin/login_admin";
    }
    @PostMapping("/login/admin")
    public String postProcessLoginAdmin(Model model, HttpServletRequest req, HttpServletResponse response){
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if ( username.isEmpty() || password.isEmpty()){
            model.addAttribute("message", "Bạn cần nhập 2 trường trên để đăng nhập");
            return "admin/login_admin";
        }
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    username,
                    password
            ));
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

            List<String> roles = user.getUser().getRoles();
            if (!roles.contains("ADMIN")) {
                model.addAttribute("message", "Tài khoản hoặc mật khẩu không chính xác.");
                return "admin/login_admin";
            }

            //Gen token
            String token = jwtTokenUtil.generateToken((CustomUserDetails) authentication.getPrincipal());

            //Add token to cookie to login
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setMaxAge(MAX_AGE_COOKIE);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            return "redirect:/admin";
        }catch (Exception e){
            model.addAttribute("message", "Tài khoản hoặc mật khẩu không chính xác.");
            return "admin/login_admin";
        }
    }
}
