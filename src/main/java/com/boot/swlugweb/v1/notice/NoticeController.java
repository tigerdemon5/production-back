package com.boot.swlugweb.v1.notice;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ResponseEntity<NoticePageResponseDto> getNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "10") int size
    ) {
        NoticePageResponseDto response = noticeService.getNoticesWithPagination(page, searchTerm, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detail")
    public ResponseEntity<NoticeDomain> getNoticeDetail(@RequestBody Map<String, String> request) {
        String id = request.get("id");

        NoticeDomain notice = noticeService.getNoticeDetail(id);
        return ResponseEntity.ok(notice);
    }
    
//    공지 저장
    @PostMapping("/save")
    public ResponseEntity<?> saveNotice(@RequestBody NoticeCreateDto noticeCreateDto,
                                        HttpSession session) {
        String userId = (String) session.getAttribute("USER");
        String role = (String) session.getAttribute("ROLE");

        // 임시로 권한 체크 제거, 로그인만 확인
        if (userId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        noticeService.createNotice(noticeCreateDto, userId);
        return ResponseEntity.ok().body("{\"redirect\": \"/api/notice\"}");
    }
    

    @PostMapping("/update")
    public ResponseEntity<String> updateNoticePost(
            @RequestBody NoticeUpdateRequestDto noticeUpdateRequestDto,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        noticeService.updateNotice(noticeUpdateRequestDto, userId);
        return ResponseEntity.ok().body("{\"redirect\": \"/api/notice\"}");
    }
    

    @PostMapping("/delete")
    public ResponseEntity<String> deleteNoticePost(
            @RequestBody Map<String, String> request,
            HttpSession session
    ) throws GeneralSecurityException, IOException {
        String id = request.get("id");
        String userId = (String) session.getAttribute("USER");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        noticeService.deleteNotice(id, userId);
//        return ResponseEntity.ok().body("{\"redirect\": \"/api/notice\"}");
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION,"/api/notice")
                .build();
    }

    @PostMapping("/adjacent")
    public ResponseEntity<Map<String, NoticeSummaryDto>> getAdjacentNotices(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        Map<String, NoticeSummaryDto> adjacentNotices = noticeService.getAdjacentNotices(id);
        return ResponseEntity.ok(adjacentNotices);
    }

    @PostMapping("/upload-image")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("upload") MultipartFile file) {
        try {
            String imageUrl = noticeService.saveImage(file);
            Map<String, Object> response = new HashMap<>();
            response.put("uploaded", true);
            response.put("url", imageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("uploaded", false);
            response.put("error", Map.of("message", "Image upload failed: " + e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path imagePath = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(imagePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                CacheControl cacheControl = CacheControl.maxAge(365, TimeUnit.DAYS);

                return ResponseEntity.ok()
                        .cacheControl(cacheControl)
                        .contentType(MediaType.parseMediaType(contentType))
                        .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
