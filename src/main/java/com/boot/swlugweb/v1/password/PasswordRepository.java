package com.boot.swlugweb.v1.password;

import com.boot.swlugweb.v1.signup.SignupUserInfoDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
//비밀번호 수정
@Repository
public interface PasswordRepository extends JpaRepository<SignupUserInfoDomain, Integer> {
    @Query("SELECT ui FROM SignupUserInfoDomain ui WHERE ui.signupUsers.userId = :userId AND ui.email = :email")
    Optional<SignupUserInfoDomain> findByUserIdAndEmail(@Param("userId") String userId, @Param("email") String email);
}