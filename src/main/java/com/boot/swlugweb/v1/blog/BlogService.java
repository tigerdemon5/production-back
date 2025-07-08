package com.boot.swlugweb.v1.blog;

import com.boot.swlugweb.v1.mypage.MyPageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.io.Files.getFileExtension;

@Service
public class BlogService {

//    @Value("${file.upload-dir}")
//    private String uploadDir;

    private final BlogRepository blogRepository;
    private final MyPageRepository myPageRepository;
    private final GoogleDriveService googleDriveService;

    public BlogService(BlogRepository blogRepository, MyPageRepository myPageRepository,GoogleDriveService googleDriveService) {
        this.blogRepository = blogRepository;
        this.myPageRepository = myPageRepository;
        this.googleDriveService = googleDriveService;
    }
    public String saveImage(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Empty file");

        if (file.getSize() > 20 * 1024 * 1024) // 20MB 제한
            throw new IllegalArgumentException("File size exceeds maximum limit");

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename).toLowerCase();

        Set<String> allowedExtensions = Set.of(
                "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "tiff", "tif", "svg"
        );

        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Invalid file extension: " + extension);
        }

        return googleDriveService.uploadFileToDrive(file);
    }

    public BlogDomain createBlog(BlogCreateDto blogCreateDto, String userId) throws Exception {
        BlogDomain blogDomain = new BlogDomain();

        blogDomain.setUserId(userId);
        blogDomain.setBoardCategory(blogCreateDto.getBoardCategory());
        blogDomain.setBoardTitle(blogCreateDto.getBoardTitle());
        blogDomain.setBoardContents(blogCreateDto.getBoardContent());
        blogDomain.setCreateAt(LocalDateTime.now());
        blogDomain.setUpdateAt(LocalDateTime.now());
        blogDomain.setTag(blogCreateDto.getTag());
        blogDomain.setIsPin(false);
        blogDomain.setIsSecure(0);
        blogDomain.setIsDelete(0);

        List<String> uploadedImageUrls = new ArrayList<>();

        try {
            // 1️⃣ 업로드된 이미지 파일 처리 (saveImage 사용)
            if (blogCreateDto.getImageFiles() != null) {
                for (MultipartFile file : blogCreateDto.getImageFiles()) {
                    uploadedImageUrls.add(saveImage(file));
                }
            }

            // 2️⃣ 본문 HTML에 포함된 이미지 URL도 추출해서 저장
            Pattern pattern = Pattern.compile("src=[\"']([^\"']+)[\"']");
            Matcher matcher = pattern.matcher(blogCreateDto.getBoardContent());

            while (matcher.find()) {
                String imageUrl = matcher.group(1);
                if (!uploadedImageUrls.contains(imageUrl)) {
                    uploadedImageUrls.add(imageUrl);
                }
            }

        } catch (Exception e) {
            // 업로드한 이미지 모두 삭제
            uploadedImageUrls.forEach(this::deleteImage);
            throw e;
        }

        blogDomain.setImage(uploadedImageUrls);


        return blogRepository.save(blogDomain);
    }


    private void deleteImage(String imageUrl) {
        try {
            String fileId = extractFileIdFromUrl(imageUrl); // 🔍 fileId 추출
            googleDriveService.deleteFile(fileId);
            System.out.println("🗑️ Deleted fileId: " + fileId);
        } catch (Exception e) {
            System.err.println("❌ Failed to delete image: " + imageUrl);
            e.printStackTrace();
        }
    }


    // ✅ URL에서 fileId 추출
    public String extractFileIdFromUrl(String url) {
        if (url == null) return null;

        // 1. drive.google.com 링크
        if (url.contains("drive.google.com/file/d/")) {
            int start = url.indexOf("/d/") + 3;
            int end = url.indexOf("/", start);
            if (start > 2 && end > start) {
                return url.substring(start, end);
            }
        }

        // 2. lh3.googleusercontent.com 링크
        if (url.contains("lh3.googleusercontent.com/d/")) {
            int start = url.indexOf("/d/") + 3;
            int end = url.indexOf("?", start); // 쿼리 파라미터 제거 (optional)
            if (end == -1) end = url.length(); // ?가 없는 경우
            return url.substring(start, end);
        }

        // 3. https://drive.google.com/uc?id=FILE_ID
        Pattern pattern = Pattern.compile("id=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }



    public void updateBlog(BlogUpdateRequestDto dto, String userId) throws Exception {
        BlogDomain blog = blogRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        if (!blog.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized");
        }
        // 제목, 내용, 태그 수정
        if (dto.getBoardTitle() != null) blog.setBoardTitle(dto.getBoardTitle());
        if (dto.getBoardContent() != null) blog.setBoardContents(dto.getBoardContent());
        if (dto.getTag() != null) blog.setTag(dto.getTag());

        // 기존 이미지 목록
        List<String> currentImageUrls = blog.getImage() != null ? new ArrayList<>(blog.getImage()) : new ArrayList<>();
        List<String> updatedImageUrls = dto.getImageUrls() != null ? new ArrayList<>(dto.getImageUrls()) : new ArrayList<>();

        // 삭제 대상 이미지 계산
        List<String> imagesToDelete = new ArrayList<>(currentImageUrls);
        imagesToDelete.removeAll(updatedImageUrls);

        for (String imageUrl : imagesToDelete) {
            deleteImage(imageUrl); // Google Drive에서 삭제
        }

        // 새로 추가할 이미지 업로드
        if (dto.getImageFiles() != null && !dto.getImageFiles().isEmpty()) {
            for (MultipartFile file : dto.getImageFiles()) {
                String imageUrl = saveImage(file); // 업로드 후 URL 반환
                updatedImageUrls.add(imageUrl);
            }
        }

        blog.setImage(updatedImageUrls);
        blog.setUpdateAt(LocalDateTime.now());
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
        blogDetailResponseDto.setUpdateAt(blog.getUpdateAt());
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

    //구글 코드
    public List<String> getImageUrlsByBlogId(String blogId) {
        return blogRepository.findById(blogId)
                .map(BlogDomain::getImage)
                .orElse(Collections.emptyList());
    }


}