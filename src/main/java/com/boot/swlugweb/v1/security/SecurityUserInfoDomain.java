package com.boot.swlugweb.v1.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_info")
public class SecurityUserInfoDomain {

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


    @OneToOne()
    @JoinColumn(name = "users_num")
    private SecurityUsersDomain securityUsers;

    @OneToOne()
    @JoinColumn(name = "type_num")
    private SecurityUserRuleTypeDomain securityUserRuleType;

}
