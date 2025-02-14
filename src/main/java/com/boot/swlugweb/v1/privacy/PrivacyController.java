package com.boot.swlugweb.v1.privacy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/privacy")
public class PrivacyController {
    @GetMapping
    public String privacy(){ return "i am minji this is myyy!! he he";}
}



