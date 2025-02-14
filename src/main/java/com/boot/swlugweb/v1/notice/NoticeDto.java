package com.boot.swlugweb.v1.notice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class NoticeDto {

    @Id
    private String id;
    private String userId;
    private String nickname;
    private String noticeTitle;
    private LocalDateTime createAt;
    private List<String> tag;
    private Boolean isPin = false;
    private Integer isSecure = 0;
    private Integer isDelete = 0;

    private List<String> image; //추가

    // 추가된 필드
    private Long displayNumber;
}