package com.boot.swlugweb.v1.intro;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/intro")
public class IntroController {

    @GetMapping
    public String intro() {
        return "intro page";
    }
}
