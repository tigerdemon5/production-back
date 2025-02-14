package com.boot.swlugweb.v1.blog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
public class BlogCreateDto {
    private Integer boardCategory;
    private String boardTitle;
    private String boardContent;
    private List<String> tag;
    private String thumbnailImage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createAt;
    private List<String> imageUrl;
    private List<MultipartFile> imageFiles;
}
