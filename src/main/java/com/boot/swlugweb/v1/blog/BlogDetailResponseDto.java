package com.boot.swlugweb.v1.blog;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BlogDetailResponseDto {
    private String id;
    private Integer boardCategory;
    private String boardTitle;
    private String boardContents;
    private LocalDateTime createAt;
    private String userId;
    private String nickname;
    private List<String> tag;
    private List<String> image;
    private String thumbnailImage;
}