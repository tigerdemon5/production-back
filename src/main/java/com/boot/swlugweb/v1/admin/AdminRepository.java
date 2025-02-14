package com.boot.swlugweb.v1.admin;

import com.boot.swlugweb.v1.signup.SignupUserInfoDomain;
import com.boot.swlugweb.v1.signup.SignupUserRuleTypeDomain;
import com.boot.swlugweb.v1.signup.SignupUsersDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<SignupUserInfoDomain, Integer> {
    @Query("SELECT ui FROM SignupUserInfoDomain ui " +
            "JOIN FETCH ui.signupUsers u " +
            "JOIN FETCH ui.signupUserRuleType ut " +
            "WHERE u.userId = :userId")
    Optional<SignupUserInfoDomain> findByUserInfo(@Param("userId") String userId);

    @Query("SELECT ui FROM SignupUserInfoDomain ui " +
            "JOIN FETCH ui.signupUsers u " +
            "JOIN FETCH ui.signupUserRuleType ut")
    List<SignupUserInfoDomain> findByUser();

    @Modifying
    @Query(value = "UPDATE user_info ui " +
            "JOIN users u ON ui.users_num = u.users_num " +
            "JOIN user_type urt ON ui.type_num = urt.type_num " +
            "SET ui.email = :email, ui.phone = :phone, urt.nickname = :nickname, urt.role_type = :roleType " +
            "WHERE u.userId = :userId",
            nativeQuery = true)
    void updateUser(@Param("email") String email, @Param("phone") String phone, @Param("nickname") String nickname, @Param("roleType") Integer roleType, @Param("userId") String userId);
}