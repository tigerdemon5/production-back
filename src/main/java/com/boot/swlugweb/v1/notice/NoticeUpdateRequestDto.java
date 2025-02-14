package com.boot.swlugweb.v1.notice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class NoticeUpdateRequestDto {
    private String id;
    private String noticeTitle;
    private String noticeContents;
    private List<String> imageUrl;//유지하려는 이미지 URL 목록
    private List<MultipartFile> imageFiles; // 새로 업로드할 이미지 파일 목록
}
