package com.quyquang.genzshoes3.service;


import com.quyquang.genzshoes3.entity.User;
import com.quyquang.genzshoes3.model.dto.UserDTO;
import com.quyquang.genzshoes3.model.request.ChangePasswordRequest;
import com.quyquang.genzshoes3.model.request.CreateUserRequest;
import com.quyquang.genzshoes3.model.request.UpdateProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    List<UserDTO> getListUsers();

    Page<User> adminListUserPages(String fullName, String phone, String email, Integer page);

    User createUser(CreateUserRequest createUserRequest);

    void changePassword(User user, ChangePasswordRequest changePasswordRequest);

    User updateProfile(User user, UpdateProfileRequest updateProfileRequest);
}
