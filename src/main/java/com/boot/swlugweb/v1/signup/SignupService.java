package com.boot.swlugweb.v1.signup;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
//회원가입
@Service
@RequiredArgsConstructor
public class SignupService {
    @Autowired
    private final SignupUserInfoRepository signupUserInfoRepository;
    @Autowired
    private final SignupUsersRepository signupUsersRepository;
    @Autowired
    private final SignupUserRuleTypeRepository signupUserRuleTypeRepository;
    private final PasswordEncoder passwordEncoder;

    // ID 중복 체크 메서드 추가
    public boolean existsById(String userId) {
        return signupUsersRepository.existsByuserId(userId);
    }

    // 기존 메서드들
    public SignupUserInfoDomain ConvertUserInfoToDomain(SignupRequestDto signupRequestDto, SignupUsersDomain signupUsersDomain, SignupUserRuleTypeDomain signupUserRuleTypeDomain){
        SignupUserInfoDomain signupUserInfoDomain = new SignupUserInfoDomain();
        signupUserInfoDomain.setEmail(signupRequestDto.getEmail());
        signupUserInfoDomain.setPhone(signupRequestDto.getPhone());
        signupUserInfoDomain.setSignupUsers(signupUsersDomain);
        signupUserInfoDomain.setSignupUserRuleType(signupUserRuleTypeDomain);
        return signupUserInfoDomain;
    }

    private SignupUsersDomain convertUsersToDomain(SignupRequestDto signupRequestDto) {
        SignupUsersDomain signupUsersDomain = new SignupUsersDomain();
        signupUsersDomain.setPw(passwordEncoder.encode(signupRequestDto.getPw()));
        signupUsersDomain.setUserId(signupRequestDto.getUserId());
        return signupUsersDomain;
    }

    private SignupUserRuleTypeDomain convertUserRuleTypeToDomain(SignupRequestDto signupRequestDto) {
        SignupUserRuleTypeDomain signupUserRuleTypeDomain = new SignupUserRuleTypeDomain();
        signupUserRuleTypeDomain.setNickname(signupRequestDto.getNickname());
        signupUserRuleTypeDomain.setRole_type(2);  // 대기 회원으로 설정
        signupUserRuleTypeDomain.setUserId(signupRequestDto.getUserId());
        return signupUserRuleTypeDomain;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void registerUser(SignupRequestDto signupRequestDto) {
        if (signupUsersRepository.existsByuserId(signupRequestDto.getUserId())) {
            throw new IllegalArgumentException("이미 사용중인 ID입니다.");
        }

        SignupUsersDomain signupUsersDomain = convertUsersToDomain(signupRequestDto);
        System.out.println("users id:" + signupUsersDomain.getUserId());
        signupUsersDomain = signupUsersRepository.save(signupUsersDomain);
        System.out.println("save");

        SignupUserRuleTypeDomain signupUserRuleTypeDomain = convertUserRuleTypeToDomain(signupRequestDto);
        System.out.println("user_type id:" + signupUserRuleTypeDomain.getUserId());
        signupUserRuleTypeDomain = signupUserRuleTypeRepository.save(signupUserRuleTypeDomain);
        System.out.println("save");

        SignupUserInfoDomain signupUserInfoDomain = ConvertUserInfoToDomain(signupRequestDto, signupUsersDomain, signupUserRuleTypeDomain);
        signupUserInfoRepository.save(signupUserInfoDomain);
        System.out.println("save");

        System.out.println("success to save the data in tables");
    }
}