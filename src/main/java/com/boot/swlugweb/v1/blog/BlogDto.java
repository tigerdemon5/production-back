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

            // R2 등 완전한 URL(http/https)은 그대로 반환
            if (firstImage.startsWith("http://") || firstImage.startsWith("https://")) {
                return firstImage;
            }

            // 과거 로컬 파일시스템 방식으로 저장된 파일명 호환용 (혹시 남아있는 옛날 글 대비)
            return firstImage.startsWith("/api/blog/images/")
                    ? firstImage
                    : "/api/blog/images/" + firstImage;
        }
        return "/img/apply_swlug.png";
    }
}