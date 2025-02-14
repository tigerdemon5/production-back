package com.boot.swlugweb.v1.mypage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "SwlugWebTest")
@Getter
@Setter
public class MyPageBlogDomain {
    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("board_category")
    private long boardCategory;

    @Field("board_title")
    private String boardTitle;

    @Field("board_contents")
    private String boardContents;

    @Field("created_at")
    private LocalDateTime createAt;

    @Field("is_pin")
    private boolean isPin;

    @Field("is_secure")
    private int isSecure;

    @Field("is_delete")
    private int isDelete;
}
