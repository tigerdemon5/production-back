package com.boot.swlugweb.v1.signup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
//회원가입
public interface SignupUserRuleTypeRepository extends JpaRepository<SignupUserRuleTypeDomain, String> {
    Optional<SignupUserRuleTypeDomain> findByUserId(String userId);
}
