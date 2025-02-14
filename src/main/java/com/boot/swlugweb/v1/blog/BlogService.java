package com.boot.swlugweb.v1.blog;

import com.boot.swlugweb.v1.mypage.MyPageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BlogService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final BlogRepository blogRepository;
    private final MyPageRepository myPageRepository;

    public BlogService(BlogRepository blogRepository, MyPageRepository myPageRepository) {
        this.blogRepository = blogRepository;
        this.myPageRepository = myPageRepository;
    }

    // BlogService.java
    public String saveImage(MultipartFile file) throws IOException {
        try {
            // 파일 유효성 검사
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Empty file");
            }

            // 파일 크기 검사 (10MB)
            if (file.getSize() > 20 * 1024 * 1024) {
                throw new IllegalArgumentException("File size exceeds maximum limit");
            }

            // 파일 확장자 검사
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFilename).toLowerCase();
            Set<String> allowedExtensions = new HashSet<>(Arrays.asList(
                    "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "tiff", "tif", "svg"
            ));

            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException("Invalid file extension");
            }

            // 고유한 파일명 생성
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            Path uploadPath = Paths.get(uploadDir);

            // 업로드 디렉토리가 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }

            // 파일 저장
            Path destinationFile = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved at: " + destinationFile.toAbsolutePath());

            // 접근 가능한 URL 반환
            return "/api/blog/images/" + newFilename;
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 파일 확장자 추출 메서드
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }
    // 이미지 삭제 메소드
    public void deleteImage(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("/api/blog/images/")) {
            String filename = imageUrl.substring("/api/blog/images/".length());
            try {
                Path imagePath = Paths.get(uploadDir).resolve(filename);
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BlogDomain createBlog(BlogCreateDto blogCreateDto, String userId) throws IOException {
        BlogDomain blogDomain = new BlogDomain();

        blogDomain.setUserId(userId);
        blogDomain.setBoardCategory(blogCreateDto.getBoardCategory());
        blogDomain.setBoardTitle(blogCreateDto.getBoardTitle());
        blogDomain.setBoardContents(blogCreateDto.getBoardContent());
        blogDomain.setCreateAt(LocalDateTime.now());
        blogDomain.setTag(blogCreateDto.getTag());
        blogDomain.setIsPin(false);
        blogDomain.setIsSecure(0);
        blogDomain.setIsDelete(0);

        List<String> uploadedImageUrls = new ArrayList<>();
        if (blogCreateDto.getImageFiles() != null && !blogCreateDto.getImageFiles().isEmpty()) {
            for (MultipartFile file : blogCreateDto.getImageFiles()) {
                try {
                    String imageUrl = saveImage(file);
                    uploadedImageUrls.add(imageUrl);
                } catch (Exception e) {
                    uploadedImageUrls.forEach(this::deleteImage);
                    throw e;
                }
            }
        }

        // HTML 컨텐츠에서 이미지 URL 추출
        Pattern pattern = Pattern.compile("src=\"(/api/blog/images/[^\"]+)\"");
        Matcher matcher = pattern.matcher(blogCreateDto.getBoardContent());
        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            if (!uploadedImageUrls.contains(imageUrl)) {
                uploadedImageUrls.add(imageUrl);
            }
        }

        blogDomain.setImage(uploadedImageUrls);
        return blogRepository.save(blogDomain);

    }

    public void updateBlog(BlogUpdateRequestDto blogUpdateRequestDto, String userId) throws IOException {
        BlogDomain blog = blogRepository.findById(blogUpdateRequestDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        if (!blog.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized");
        }

        if (blogUpdateRequestDto.getBoardTitle() != null) {
            blog.setBoardTitle(blogUpdateRequestDto.getBoardTitle());
        }
        if (blogUpdateRequestDto.getBoardContent() != null) {
            blog.setBoardContents(blogUpdateRequestDto.getBoardContent());
        }
        if (blogUpdateRequestDto.getTag() != null) {
            blog.setTag(blogUpdateRequestDto.getTag());
        }

        List<String> currentImageUrls = blog.getImage() != null ? new ArrayList<>(blog.getImage()) : new ArrayList<>();
        List<String> updatedImageUrls = blogUpdateRequestDto.getImageUrls() != null ?
                blogUpdateRequestDto.getImageUrls() : new ArrayList<>();

        List<String> imagesToDelete = new ArrayList<>(currentImageUrls);
        imagesToDelete.removeAll(updatedImageUrls);
        for (String imageUrl : imagesToDelete) {
            deleteImage(imageUrl);
        }

        if (blogUpdateRequestDto.getImageFiles() != null) {
            for (MultipartFile file : blogUpdateRequestDto.getImageFiles()) {
                try {
                    String imageUrl = saveImage(file);
                    updatedImageUrls.add(imageUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }

        blog.setImage(updatedImageUrls);
        blog.setCreateAt(LocalDateTime.now());
        blogRepository.save(blog);
    }

    public void deleteBlog(BlogDeleteRequestDto blogDeleteRequestDto, String userId) {
        BlogDomain blog = blogRepository.findById(blogDeleteRequestDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        if (!blog.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized");}

        // 연결된 이미지들 삭제
        if (blog.getImage() != null) {
            for (String imageUrl : blog.getImage()) {
                deleteImage(imageUrl);
            }
        }

        blogRepository.deleteById(blogDeleteRequestDto.getId());
    }

    private String getCategoryName(Integer category) {
        switch (category) {
            case 1: return "성과";
            case 2: return "정보";
            case 3: return "후기";
            case 4: return "활동";
            default: return "";
        }
    }

    public BlogPageResponseDto getBlogsWithPaginationg(int page, Integer category, String searchTerm, int size, List<String> tags) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<BlogDto> blogPage;
        List<Integer> categories;

        if (category == null || category < 1 || category > 4) {
            categories = Arrays.asList(1, 2, 3, 4);
        } else {
            categories = List.of(category);
        }

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            if (tags == null || tags.isEmpty()) {
                blogPage = blogRepository.findByBlogIsDeleteOrderByIsPinDescCreateAtDesc(categories, 0, pageable);
            } else {
                blogPage = blogRepository.findByBlogIsDeleteOrderByIsPinDescCreateAtDescAndTag(categories, tags, 0, pageable);
            }
        } else {
            try {
                String decodedSearchTerm = java.net.URLDecoder.decode(searchTerm, "UTF-8");
                String regexPattern = ".*" + decodedSearchTerm.trim()
                        .replaceAll("[\\s]+", " ")
                        .replaceAll(" ", "(?:[ ]|)") + ".*";

                if (tags == null || tags.isEmpty()) {
                    blogPage = blogRepository.findByBlogTitleContainingAndIsDelete(
                            categories, regexPattern, 0, pageable
                    );
                } else {
                    blogPage = blogRepository.findByBlogTitleContainingAndIsDeleteAndTag(categories, regexPattern, tags, 0, pageable);
                }

            } catch (Exception e) {
                throw new RuntimeException("검색어 처리 중 오류가 발생했습니다.", e);
            }
        }

        // 닉네임과 카테고리 이름을 설정
        List<BlogDto> blogsWithInfo = blogPage.getContent().stream()
                .map(blog -> {
                    String nickname = myPageRepository.findNickname(blog.getUserId());
                    blog.setNickname(nickname);
                    blog.setCategoryName(getCategoryName(blog.getBoardCategory()));
                    return blog;
                })
                .collect(Collectors.toList());

        return new BlogPageResponseDto(
                blogsWithInfo,
                blogPage.getTotalElements(),
                blogPage.getTotalPages(),
                page
        );
    }
    public BlogDetailResponseDto getBlogDetail(String id) {
        BlogDomain blog = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id+" Blog post not found"));

        String nickname = myPageRepository.findNickname(blog.getUserId());

        if (blog.getBoardCategory() == 0) {
            throw new IllegalArgumentException("Invalid blog category");
        }

        BlogDetailResponseDto blogDetailResponseDto = new BlogDetailResponseDto();
        blogDetailResponseDto.setId(blog.getId());
        blogDetailResponseDto.setUserId(blog.getUserId());
        blogDetailResponseDto.setBoardTitle(blog.getBoardTitle());
        blogDetailResponseDto.setBoardCategory(blog.getBoardCategory());
        blogDetailResponseDto.setBoardContents(blog.getBoardContents());
        blogDetailResponseDto.setNickname(nickname);
        blogDetailResponseDto.setCreateAt(blog.getCreateAt());
        blogDetailResponseDto.setTag(blog.getTag());
        blogDetailResponseDto.setImage(blog.getImage());
        blogDetailResponseDto.setThumbnailImage(blog.getThumbnailImage());

        return blogDetailResponseDto;
    }

    public Map<String, BlogSummaryDto> getAdjacentBlogs(String id) {
        Map<String, BlogSummaryDto> result = new HashMap<>();

        BlogDomain currentBlog = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + " 게시물이 없습니다."));

        LocalDateTime currentCreateAt = currentBlog.getCreateAt();
        Integer currentCategory = currentBlog.getBoardCategory();

        List<Integer> categories;
        if (currentCategory == null) {
            categories = Arrays.asList(1, 2, 3, 4);
        } else {
            categories = Collections.singletonList(currentCategory);
        }

        List<BlogDomain> prevBlogs = blogRepository.findPrevBlogs(categories, currentCreateAt);
        if (!prevBlogs.isEmpty()) {
            BlogDomain prevBlog = prevBlogs.get(0);
            BlogSummaryDto prevDto = new BlogSummaryDto();
            prevDto.setId(prevBlog.getId());
            prevDto.setBlogTitle(prevBlog.getBoardTitle());
            result.put("previous", prevDto);
        }

        List<BlogDomain> nextBlogs = blogRepository.findNextBlogs(categories, currentCreateAt);
        if (!nextBlogs.isEmpty()) {
            BlogDomain nextBlog = nextBlogs.get(0);
            BlogSummaryDto nextDto = new BlogSummaryDto();
            nextDto.setId(nextBlog.getId());
            nextDto.setBlogTitle(nextBlog.getBoardTitle());
            result.put("next", nextDto);
        }

        return result;
    }

    public List<String> getAllTags() {
        return blogRepository.findAllTags();
    }
}