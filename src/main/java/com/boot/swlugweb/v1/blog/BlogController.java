package com.boot.swlugweb.v1.blog;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public ResponseEntity<BlogPageResponseDto> getBlogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") Integer category,
            @RequestParam(defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> tags
    ) {
        BlogPageResponseDto response = blogService.getBlogsWithPaginationg(page, category, searchTerm, size, tags);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detail")
    public ResponseEntity<BlogDetailResponseDto> getBlogDetail(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        BlogDetailResponseDto blog = blogService.getBlogDetail(id);
        return ResponseEntity.ok(blog);
    }

    // BlogController.java
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveBlog(
            @RequestPart(name = "blogCreateDto") BlogCreateDto blogCreateDto,
            @RequestPart(name = "imageFiles", required = false) List<MultipartFile> imageFiles,
            HttpSession session) {

        String userId = (String) session.getAttribute("USER");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // HTML 컨텐츠에서 이미지 처리를 위한 코드 추가
            if (blogCreateDto.getBoardContent() != null) {
                blogCreateDto.setImageFiles(imageFiles);
            }
            blogService.createBlog(blogCreateDto, userId);
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/api/blog")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateBlogPost(
            @RequestPart(name = "blogUpdateRequestDto") BlogUpdateRequestDto blogUpdateRequestDto,
            @RequestPart(name = "imageFiles", required = false) List<MultipartFile> imageFiles,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            if (imageFiles != null && !imageFiles.isEmpty()) {
                blogUpdateRequestDto.setImageFiles(imageFiles);
            }
            blogService.updateBlog(blogUpdateRequestDto, userId);

            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/api/blog")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteBlog(
            @RequestBody BlogDeleteRequestDto blogDeleteRequestDto,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            blogService.deleteBlog(blogDeleteRequestDto, userId);
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/api/blog")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> getTags() {
        List<String> tags = blogService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/adjacent")
    public ResponseEntity<Map<String, BlogSummaryDto>> searchBlogsByAdjacent(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        Map<String, BlogSummaryDto> adjacentBlogs = blogService.getAdjacentBlogs(id);
        return ResponseEntity.ok(adjacentBlogs);
    }

    // 새로운 이미지 업로드 엔드포인트
    @PostMapping("/upload-image")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("upload") MultipartFile file) {
        try {
            String imageUrl = blogService.saveImage(file);
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

    // 이미지 조회 엔드포인트
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

                // 캐시 설정 추가
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