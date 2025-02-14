package com.boot.swlugweb.v1.main;

import com.boot.swlugweb.v1.mypage.MyPageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainService {
    private final MainRepository mainRepository;
    private final MyPageRepository myPageRepository;

    public MainService(MainRepository mainRepository, MyPageRepository myPageRepository) {
        this.mainRepository = mainRepository;
        this.myPageRepository = myPageRepository;
    }

    public List<MainDomain> getLatestPosts() {
        // 최신순 정렬 후 3개 가져오기
        Pageable pageable = PageRequest.of(0, 3, Sort.by("created_at").descending());
        List<MainDomain> posts = mainRepository.findByBoardCategoryOrderByCreatedAtDesc(pageable);

        // 각 게시물에 대해 닉네임 설정
        return posts.stream()
                .map(post -> {
                    String nickname = myPageRepository.findNickname(post.getUserId());
                    post.setNickname(nickname);
                    return post;
                })
                .collect(Collectors.toList());
    }
}