package com.boot.swlugweb.v1.apply;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

//회원가입
@RestController
@RequestMapping("/api/apply")
@RequiredArgsConstructor
public class ApplyController {

//    지원페이지 무조건 열어두고 싶을 때
//    @GetMapping
//    public Boolean apply() {
//
//        return true;
//    }

    private final ApplyService applyService;

    @GetMapping
    public Boolean apply() {
        return applyService.isApplicationPeriod();
    }

}
