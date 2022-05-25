package com.quyquang.genzshoes3.repository;

import com.quyquang.genzshoes3.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);

    @Query(value = "SELECT * " +
            "FROM users u WHERE u.full_name LIKE CONCAT('%',?1,'%') " +
            "AND u.phone LIKE CONCAT('%',?2,'%') " +
            "AND u.email LIKE CONCAT('%',?3,'%') ",nativeQuery = true)
    Page<User> adminListUserPages(String fullName, String phone, String email, Pageable pageable);

    // findUserByEmail
//    @Query(nativeQuery = true,
//        value = "SELECT * FROM users u WHERE FUNCTION('JSON_EXTRAC', u, '$.roles')=?1"
//    )
//    User findUserByEmail(String email);
    // findAdminByEmail
}
