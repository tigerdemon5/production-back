package com.boot.swlugweb.v1.security;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityUserInfoRepository extends JpaRepository<SecurityUserInfoDomain, String> {

    // user_id로 사용자 검색
    //path에 앞에서 도메인에서 정의한 관계 이름 가져오기
    @EntityGraph(attributePaths = {"securityUsers", "securityUserRuleType"})
    Optional<SecurityUserInfoDomain> findBySecurityUsers_userId(String userId);

}
