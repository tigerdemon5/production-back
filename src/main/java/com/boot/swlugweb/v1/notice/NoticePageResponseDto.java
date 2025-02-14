package com.boot.swlugweb.v1.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticePageResponseDto {
    private List<NoticeDto> notices;      // 현재 페이지의 공지사항 목록
    private long totalElements;           // 전체 데이터 수
    private int totalPages;               // 전체 페이지 수
    private int currentPage;              // 현재 페이지 번호

}