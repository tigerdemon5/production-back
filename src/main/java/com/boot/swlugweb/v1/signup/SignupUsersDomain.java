package com.boot.swlugweb.v1.signup;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
//회원가입
@Entity
@Getter
@Setter
@Table(name ="users")
public class SignupUsersDomain {
    //users 테이블에 들어갈 객체 정의

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_num")
    private Integer usersNum;


    @Version
    private Long version;

    private String pw;
    private String userId;
    
}
