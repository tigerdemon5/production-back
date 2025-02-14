package com.boot.swlugweb.v1.notice;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
public class NoticeCreateDto {
    private String noticeTitle;
    private String noticeContents;

    private List<String> imageUrl;
    private List<MultipartFile> imageFiles; //업로드할 이미지 파일 목록
}
