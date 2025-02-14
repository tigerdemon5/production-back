package com.boot.swlugweb.v1.main;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "SwlugWebTest")
@Getter
@Setter
public class MainDomain {
    @Id
    private String id;

    @Field("user_id")
    private String userId;

    private String nickname;  // 닉네임 필드 추가

    @Field("board_title")
    private String noticeTitle;  // boardTitle에서 noticeTitle로 변경

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("tag")
    private List<String> tag;

    @Field("is_pin")
    private Boolean isPin = false;

    @Field("is_secure")
    private Integer isSecure = 0;

    @Field("is_delete")
    private Integer isDelete = 0;
}