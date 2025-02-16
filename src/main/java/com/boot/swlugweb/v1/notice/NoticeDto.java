package com.boot.swlugweb.v1.notice;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String userId;
    private String nickname;
    private String noticeTitle;
    private LocalDateTime createAt;
    private List<String> tag;
    @JsonIgnore
    private Boolean isPin = false;
    @JsonIgnore
    private Integer isSecure = 0;
    @JsonIgnore
    private Integer isDelete = 0;

    private List<String> image; //추가

    // 추가된 필드
    private Long displayNumber;
}