package com.boot.swlugweb.v1.blog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BlogDto {
    private String id;
    @JsonIgnore
    private Integer boardCategory;
    private String boardTitle;
    private LocalDateTime createAt;
    @JsonIgnore
    private String userId;
    private String nickname;
    private String categoryName;
    private List<String> tag;
    private List<String> image;
    @JsonIgnore
    private Boolean isPin = false;
    @JsonIgnore
    private Integer isSecure = 0;
    @JsonIgnore
    private Integer isDelete = 0;
    private String thumbnailImage; // 필드는 유지

    public String getThumbnailUrl() {
        if (image != null && !image.isEmpty()) {
            String firstImage = image.get(0);
            return firstImage.startsWith("/api/blog/images/")
                    ? firstImage
                    : "/api/blog/images/" + firstImage;
        }
        return "/img/apply_swlug.png";
    }
}