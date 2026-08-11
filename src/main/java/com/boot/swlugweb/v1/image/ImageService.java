package com.boot.swlugweb.v1.image;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final AmazonS3 amazonS3;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public String upload(MultipartFile file) throws IOException {

        String ext = FilenameUtils.getExtension(file.getOriginalFilename());

        String key = UUID.randomUUID() + "." + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(bucket, key, file.getInputStream(), metadata);

        return publicUrl + "/" + key;
    }

    public void delete(String imageUrl){

        String key = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

        amazonS3.deleteObject(bucket, key);
    }
}
