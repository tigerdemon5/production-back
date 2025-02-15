package com.boot.swlugweb.v1.apply;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

//회원가입
@RestController
@RequestMapping("/api/apply")
@RequiredArgsConstructor
public class ApplyController {

    private final ApplyService applyService;

    @GetMapping
    public Boolean apply() {
        return applyService.isApplicationPeriod();
    }

}
