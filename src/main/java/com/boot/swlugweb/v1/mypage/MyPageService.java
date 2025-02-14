package com.boot.swlugweb.v1.mypage;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MyPageService {
    private final MyPageRepository myPageRepository;
    private final MyPageBlogRepository myPageBlogRepository;

    public MyPageService(MyPageRepository myPageRepository, MyPageBlogRepository myPageBlogRepository) {
        this.myPageRepository = myPageRepository;
        this.myPageBlogRepository = myPageBlogRepository;
    }

    @Transactional(readOnly = true)
    public MyPageResponseDto getUserInfo(String userId) {

        MyPageResponseDto userInfo = myPageRepository.findByUserId(userId)
                .map(user -> new MyPageResponseDto(
                        user.getSignupUsers().getUserId(),
                        user.getSignupUserRuleType().getNickname(),
                        user.getPhone(),
                        user.getEmail()
                ))
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // 블로그 정보 추가
        List<MyPageBlogDomain> blogInfo = getMyPageBlogs(userId);
        userInfo.setBlogInfo(blogInfo);

        return userInfo;

    }

    public List<MyPageBlogDomain> getMyPageBlogs(String userId) {
        // findByUserId에 세션에서 가져온 userId 값 넣어줘야 함
        return myPageBlogRepository.findByUserId(userId);
    }
}