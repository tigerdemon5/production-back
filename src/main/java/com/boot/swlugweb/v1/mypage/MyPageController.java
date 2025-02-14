package com.boot.swlugweb.v1.mypage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/mypage")
public class MyPageController {
    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping
    public ResponseEntity<MyPageResponseDto> getMyPage(HttpSession session) {
        String userId = (String) session.getAttribute("USER");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        MyPageResponseDto myPageInfo = myPageService.getUserInfo(userId);

        return ResponseEntity.ok(myPageInfo);
    }
}