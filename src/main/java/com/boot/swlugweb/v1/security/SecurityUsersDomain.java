package com.boot.swlugweb.v1.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name ="users")
public class SecurityUsersDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_num")
    private Integer usersNum;


    @Version
    private Long version;

    private String pw;
    private String userId;
}
