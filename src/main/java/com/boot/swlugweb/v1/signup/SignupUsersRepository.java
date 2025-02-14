package com.boot.swlugweb.v1.signup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//회원가입
@Repository
public interface SignupUsersRepository extends JpaRepository<SignupUsersDomain, String> {
    boolean existsByuserId(String userId);
}
