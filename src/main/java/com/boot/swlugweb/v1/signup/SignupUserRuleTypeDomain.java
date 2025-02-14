package com.boot.swlugweb.v1.signup;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="user_type")
public class SignupUserRuleTypeDomain {
    //회원가입
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_num")
    private Integer typeNum;

    @Version
    private Long version;

    private String nickname;

    private Integer role_type;

    private String userId;


}
