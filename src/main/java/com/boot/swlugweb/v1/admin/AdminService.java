package com.boot.swlugweb.v1.admin;

import com.boot.swlugweb.v1.signup.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    @Autowired
    private final AdminRepository adminRepository;
    private final AdminBlogRepository adminBlogRepository;
    @Autowired
    private final SignupUserInfoRepository signupUserInfoRepository;
    @Autowired
    private final SignupUsersRepository signupUsersRepository;
    @Autowired
    private final SignupUserRuleTypeRepository signupUserRuleTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupUserInfoDomain ConvertUserInfoToDomain(AdminCreateUserRequestDto requestDto, SignupUsersDomain signupUsersDomain, SignupUserRuleTypeDomain signupUserRuleTypeDomain){
        SignupUserInfoDomain signupUserInfoDomain = new SignupUserInfoDomain();
        signupUserInfoDomain.setEmail(requestDto.getEmail());
        signupUserInfoDomain.setPhone(requestDto.getPhone());
        signupUserInfoDomain.setSignupUsers(signupUsersDomain);
        signupUserInfoDomain.setSignupUserRuleType(signupUserRuleTypeDomain);
        return signupUserInfoDomain;
    }

    private SignupUsersDomain convertUsersToDomain(AdminCreateUserRequestDto requestDto) {
        SignupUsersDomain signupUsersDomain = new SignupUsersDomain();
        signupUsersDomain.setPw(passwordEncoder.encode(requestDto.getPw()));
        signupUsersDomain.setUserId(requestDto.getUserId());
        return signupUsersDomain;
    }

    private SignupUserRuleTypeDomain convertUserRuleTypeToDomain(AdminCreateUserRequestDto requestDto) {
        SignupUserRuleTypeDomain signupUserRuleTypeDomain = new SignupUserRuleTypeDomain();
        signupUserRuleTypeDomain.setNickname(requestDto.getNickname());
        signupUserRuleTypeDomain.setRole_type(requestDto.getRoleType());  // 대기 회원으로 설정
        signupUserRuleTypeDomain.setUserId(requestDto.getUserId());
        return signupUserRuleTypeDomain;
    }

    public List<AdminUserResponseDto> getUsers () {
        List<SignupUserInfoDomain> allUsers = adminRepository.findByUser();
        System.out.println(allUsers);

        List<AdminUserResponseDto> allUserList = new ArrayList<>();

        return allUsers.stream()
                .map(user -> new AdminUserResponseDto(
                        user.getUserInfoNum(),
                        user.getSignupUsers().getUserId(),
                        user.getSignupUserRuleType().getNickname(),
                        user.getSignupUserRuleType().getRole_type()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminUserInfoResponseDto getUserInfo (String userId) {

        AdminUserInfoResponseDto userInfo = adminRepository.findByUserInfo(userId)
                .map(user -> new AdminUserInfoResponseDto(
                        user.getUserInfoNum(),
                        user.getSignupUsers().getUserId(),
                        user.getSignupUserRuleType().getNickname(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getSignupUserRuleType().getRole_type()
                ))
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        return userInfo;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String updateUser(AdminUserUpdateRequestDto requestDto) {
        String url = "/api/admin/users";

        try {

            SignupUserInfoDomain userInfo = signupUserInfoRepository.findById(requestDto.getUserInfoNum())
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));;
            if (requestDto.getNickname() != null) {
                userInfo.setEmail(requestDto.getEmail());
            }
            if (requestDto.getPhone() != null) {
                userInfo.setPhone(requestDto.getPhone());
            }
            signupUserInfoRepository.save(userInfo);
            System.out.println("UserInfo 테이블 정보 Update");

            SignupUserRuleTypeDomain userRole = signupUserRuleTypeRepository.findByUserId(requestDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
            if (requestDto.getRoleType() != null) {
                userRole.setRole_type(requestDto.getRoleType());
            }
            if (requestDto.getNickname() != null) {
                userRole.setNickname(requestDto.getNickname());
            }
            signupUserRuleTypeRepository.save(userRole);
            System.out.println("UserRole 테이블 정보 Update");

            url = "/api/admin/users/detail?userId=" + requestDto.getUserId();

        } catch (Exception e) {
            System.out.println("error to save the data in tables: " + e.getMessage());
        }

        return url;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String createUser(AdminCreateUserRequestDto requestDto) {
        String url = "/api/admin/users";

        try {
            if (signupUsersRepository.existsByuserId(requestDto.getUserId())) {
                throw new IllegalArgumentException("이미 사용중인 ID입니다.");
            }

            SignupUsersDomain signupUsersDomain = convertUsersToDomain(requestDto);
            signupUsersDomain = signupUsersRepository.save(signupUsersDomain);

            SignupUserRuleTypeDomain signupUserRuleTypeDomain = convertUserRuleTypeToDomain(requestDto);
            System.out.println("user_type id:" + signupUserRuleTypeDomain.getUserId());
            signupUserRuleTypeDomain = signupUserRuleTypeRepository.save(signupUserRuleTypeDomain);


            SignupUserInfoDomain signupUserInfoDomain = ConvertUserInfoToDomain(requestDto, signupUsersDomain, signupUserRuleTypeDomain);
            signupUserInfoRepository.save(signupUserInfoDomain);

            System.out.println("success to save the data in tables");

            url = "/api/admin/users/detail?userId=" + signupUserRuleTypeDomain.getUserId();

        } catch (Exception e) {
            System.out.println("error to save the data in tables: " + e.getMessage());
        }

        return url;
    }

    @Transactional
    public String deleteUser (String userId) {
        String url = "/api/admin/users/detail?userId=" + userId;
        try {
            SignupUserInfoDomain userInfo = adminRepository.findByUserInfo(userId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자가 없습니다."));

            signupUserInfoRepository.delete(userInfo);
            url = "/api/admin/users";

        } catch (Exception e) {
            System.out.println("error to delete the data in tables: " + e.getMessage());
        }
        return url;
    }
}
