package com.boot.swlugweb.v1.blog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private LocalDateTime updateAt;
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

    //test0621
//    public String getThumbnailUrl() {
//        if (image != null && !image.isEmpty()) {
//            String firstImage = image.get(0);
//            // 드라이브 URL이면 그대로 반환, 아니라면 경로 덧붙이기
//            if (firstImage.startsWith("http")) {
//                return firstImage;
//            }
//            return "/api/blog/images/" + firstImage;
//        }
//        return "/img/apply_swlug.png";
//    }

//    public String getThumbnailUrl() {
//        if (image != null && !image.isEmpty()) {
//            String firstImage = image.get(0);
//            if (firstImage != null && !firstImage.isBlank()) {
//                return firstImage.startsWith("/api/blog/images/")
//                        ? firstImage
//                        : "/api/blog/images/" + firstImage;
//            }
//        }
//        return "/img/apply_swlug.png";
//    }

//    public String getThumbnailUrl() {
//        if (image != null && !image.isEmpty()) {
//            String firstImage = image.get(0);
//            return firstImage.startsWith("/api/blog/images/")
//                    ? firstImage
//                    : "/api/blog/images/" + firstImage;
//        }
//        return "/img/apply_swlug.png";
//    }



}