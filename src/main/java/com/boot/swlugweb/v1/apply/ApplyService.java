package com.boot.swlugweb.v1.apply;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class ApplyService {

    public boolean isApplicationPeriod() {
        LocalDateTime startDate = LocalDateTime.of(2025,2,16,00,00,00);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime endDate = LocalDateTime.of(2025, 3, 15, 23, 59, 59);

        return now.isAfter(startDate) && !now.isAfter(endDate);
    }
}