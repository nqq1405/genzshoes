package com.quyquang.genzshoes3.repository;

import com.quyquang.genzshoes3.entity.Verify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifyRepository extends JpaRepository<Verify,Long> {
    @Query(nativeQuery = true, value = "SELECT * FROM verify v WHERE v.token=?1")
    Verify findByToken(String token);
}
