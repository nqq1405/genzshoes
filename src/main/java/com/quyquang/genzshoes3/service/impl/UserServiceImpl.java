package com.quyquang.genzshoes3.service.impl;

import com.quyquang.genzshoes3.entity.Provider;
import com.quyquang.genzshoes3.entity.User;
import com.quyquang.genzshoes3.exception.BadRequestException;
import com.quyquang.genzshoes3.model.dto.UserDTO;
import com.quyquang.genzshoes3.model.mapper.UserMapper;
import com.quyquang.genzshoes3.model.request.ChangePasswordRequest;
import com.quyquang.genzshoes3.model.request.CreateUserRequest;
import com.quyquang.genzshoes3.model.request.UpdateProfileRequest;
import com.quyquang.genzshoes3.repository.UserRepository;
import com.quyquang.genzshoes3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.quyquang.genzshoes3.config.Constants.LIMIT_USER;

@Component
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<UserDTO> getListUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : users) {
            userDTOS.add(UserMapper.toUserDTO(user));
        }
        return userDTOS;
    }

    @Override
    public Page<User> adminListUserPages(String fullName, String phone, String email, Integer page) {
        page--;
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, LIMIT_USER, Sort.by("created_at").descending());
        return userRepository.adminListUserPages(fullName, phone, email, pageable);
    }

    @Override
    public User createUser(CreateUserRequest createUserRequest) {
        User user = userRepository.findByEmail(createUserRequest.getEmail());
        if (user != null) {
            throw new BadRequestException("Email đã tồn tại trong hệ thống. Vui lòng sử dụng email khác!");
        }
        user = UserMapper.toUser(createUserRequest);
        userRepository.save(user);
        return user;
    }

    @Override
    public void changePassword(User user, ChangePasswordRequest changePasswordRequest) {
        //Kiểm tra mật khẩu
        if (!BCrypt.checkpw(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        String hash = BCrypt.hashpw(changePasswordRequest.getNewPassword(), BCrypt.gensalt(12));
        user.setPassword(hash);
        userRepository.save(user);
    }

    @Override
    public User updateProfile(User user, UpdateProfileRequest updateProfileRequest) {
        user.setFullName(updateProfileRequest.getFullName());
        user.setPhone(updateProfileRequest.getPhone());
        user.setAddress(updateProfileRequest.getAddress());

        return userRepository.save(user);
    }

    @Override
    public void resetPassword(User user, String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        user.setPassword(hash);
        userRepository.save(user);
    }

    @Override
    public void processOAuthPostLogin(String email, String name) {
        User existUser = userRepository.findByEmail(email);

        if (existUser == null) {
            User user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setPhone("Trống");
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            user.setProvider(Provider.GOOGLE);
            user.setStatus(true);
            user.setRoles(new ArrayList<>(Arrays.asList("USER")));
            userRepository.save(user);
        }else if (existUser.getProvider().equals(Provider.LOCAL)){
            existUser.setProvider(Provider.GOOGLE);
            userRepository.save(existUser);
        }
    }
}
