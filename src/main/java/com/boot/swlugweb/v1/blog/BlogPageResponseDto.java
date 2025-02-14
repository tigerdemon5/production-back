package com.boot.swlugweb.v1.blog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogPageResponseDto {
    private List<BlogDto> blogs;
    private long totalElements;
    private long totalPages;
    private int currentPage;
}
