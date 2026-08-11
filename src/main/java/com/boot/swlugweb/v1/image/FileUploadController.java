package com.boot.swlugweb.v1.image;

import com.amazonaws.auth.policy.Resource;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@Log4j2
@RequestMapping("/api")
public class FileUploadController {

    private final ImageService imageService;

    public FileUploadController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public String status() {
        return "OK";
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam MultipartFile file)
            throws IOException {

        String url = imageService.upload(file);

        return ResponseEntity.ok(
                Map.of("url", url)
        );
    }

}
