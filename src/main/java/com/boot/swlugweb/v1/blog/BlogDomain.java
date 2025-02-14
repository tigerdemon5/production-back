package com.boot.swlugweb.v1.blog;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "SwlugWebTest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogDomain {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("board_category")
    private Integer boardCategory;

    @Field("board_title")
    private String boardTitle;

    @Field("board_contents")
    private String boardContents;

    @Field("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // JSON 직렬화 시 포맷 지정
    private LocalDateTime createAt;

    @Field("tag")
    private List<String> tag;

    @Field("image")
    private List<String> image;

    @Field("thumbnail_image")
    private String thumbnailImage;

    @Field("is_pin")
    private Boolean isPin = false;

    @Field("is_secure")
    private Integer isSecure = 0;

    @Field("is_delete")
    private Integer isDelete = 0;



}
