package com.boot.swlugweb.v1.apply;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class ApplyService {

    public boolean isApplicationPeriod() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime endDate = LocalDateTime.of(2025, 3, 15, 23, 59, 59);

        return !now.isAfter(endDate);
    }
}