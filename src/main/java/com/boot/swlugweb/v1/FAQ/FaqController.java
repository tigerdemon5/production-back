package com.boot.swlugweb.v1.FAQ;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaqController {

    @GetMapping("/api/faq")
    public Integer faq() {
        return 0;
    }
}
