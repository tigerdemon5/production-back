package com.boot.swlugweb.v1.signup;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
//회원가입
@Entity
@Getter
@Setter
@Table(name = "user_info")
public class SignupUserInfoDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //auto_increment 설정
    @Column(name = "user_info_num")
    private Integer userInfoNum;

    @Version
    private Long version;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "users_num")
    private SignupUsersDomain signupUsers;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "type_num")
    private SignupUserRuleTypeDomain signupUserRuleType;

}
