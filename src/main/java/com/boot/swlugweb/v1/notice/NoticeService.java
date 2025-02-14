package com.boot.swlugweb.v1.notice;

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
public class NoticeService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    private final NoticeRepository noticeRepository;
    private final MyPageRepository myPageRepository;

    public NoticeService(NoticeRepository noticeRepository, MyPageRepository myPageRepository) {
        this.noticeRepository = noticeRepository;
        this.myPageRepository = myPageRepository;
    }

    // 이미지 저장 메소드
    public String saveImage(MultipartFile file) throws IOException {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Empty file");
            }

            if (file.getSize() > 20 * 1024 * 1024) {
                throw new IllegalArgumentException("File size exceeds maximum limit");
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFilename).toLowerCase();
            Set<String> allowedExtensions = new HashSet<>(Arrays.asList(
                    "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "tiff", "tif", "svg"
            ));

            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException("Invalid file extension");
            }

            String newFilename = UUID.randomUUID().toString() + "." + extension;
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path destinationFile = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return "/api/notice/images/" + newFilename;
        } catch (IOException e) {
            throw e;
        }
    }

    // 이미지 삭제 메소드
    private void deleteImage(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("/api/notice/images/")) {
            String filename = imageUrl.substring("/api/notice/images/".length());
            try {
                Path imagePath = Paths.get(uploadDir).resolve(filename);
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    public NoticePageResponseDto getNoticesWithPagination(int page, String searchTerm, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<NoticeDto> noticePage;

        long totalNotices = noticeRepository.countByBoardCategoryAndIsDelete(0, 0);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            noticePage = noticeRepository.findByIsDeleteOrderByIsPinDescCreateAtDesc(0, pageable);
        } else {
            try {
                String decodedSearchTerm = java.net.URLDecoder.decode(searchTerm, "UTF-8");
                String regexPattern = ".*" + decodedSearchTerm.trim()
                        .replaceAll("[\\s]+", " ")
                        .replaceAll(" ", "(?:[ ]|)") + ".*";

                noticePage = noticeRepository.findByBoardTitleContainingAndIsDelete(
                        regexPattern, 0, pageable);
            } catch (Exception e) {
                throw new RuntimeException("검색어 처리 중 오류가 발생했습니다", e);
            }
        }

        List<NoticeDto> noticesWithNumbers = noticePage.getContent().stream()
                .map(notice -> {
                    String nickname = myPageRepository.findNickname(notice.getUserId());
                    long olderCount = noticeRepository.countOlderNotices(0, notice.getCreateAt());
                    notice.setDisplayNumber(totalNotices - olderCount);
                    notice.setNickname(nickname);
                    return notice;
                })
                .collect(Collectors.toList());

        return new NoticePageResponseDto(
                noticesWithNumbers,
                noticePage.getTotalElements(),
                noticePage.getTotalPages(),
                page
        );
    }

    public NoticeDomain getNoticeDetail(String id) {
        NoticeDomain notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + " post not found"));

        if (notice.getBoardCategory() != 0) {
            throw new IllegalArgumentException("Invalid notice category");
        }

        return notice;
    }

    // 공지사항 생성
    public NoticeDomain createNotice(NoticeCreateDto noticeCreateDto, String userId) {
        NoticeDomain noticeDomain = new NoticeDomain();

        noticeDomain.setUserId(userId);
        noticeDomain.setBoardCategory(0);
        noticeDomain.setNoticeTitle(noticeCreateDto.getNoticeTitle());
        noticeDomain.setNoticeContents(noticeCreateDto.getNoticeContents());
        noticeDomain.setCreateAt(LocalDateTime.now());
        noticeDomain.setIsPin(false);
        noticeDomain.setIsDelete(0);

        // 이미지 URL 처리
        Pattern pattern = Pattern.compile("src=\"(/api/notice/images/[^\"]+)\"");
        Matcher matcher = pattern.matcher(noticeCreateDto.getNoticeContents());
        List<String> imageUrls = new ArrayList<>();
        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            imageUrls.add(imageUrl);
        }
        noticeDomain.setImage(imageUrls);

        return noticeRepository.save(noticeDomain);
    }

    // 공지사항 수정
    public void updateNotice(NoticeUpdateRequestDto noticeUpdateRequestDto, String userId) {
        NoticeDomain notice = noticeRepository.findById(noticeUpdateRequestDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Notice not found"));

        List<String> currentImageUrls = notice.getImage() != null ? new ArrayList<>(notice.getImage()) : new ArrayList<>();
        List<String> updatedImageUrls = new ArrayList<>();

        // 새로운 컨텐츠에서 이미지 URL 추출
        if (noticeUpdateRequestDto.getNoticeContents() != null) {
            Pattern pattern = Pattern.compile("src=\"(/api/notice/images/[^\"]+)\"");
            Matcher matcher = pattern.matcher(noticeUpdateRequestDto.getNoticeContents());
            while (matcher.find()) {
                updatedImageUrls.add(matcher.group(1));
            }
        }

        // 더 이상 사용되지 않는 이미지 삭제
        List<String> imagesToDelete = new ArrayList<>(currentImageUrls);
        imagesToDelete.removeAll(updatedImageUrls);
        for (String imageUrl : imagesToDelete) {
            deleteImage(imageUrl);
        }

        notice.setNoticeTitle(noticeUpdateRequestDto.getNoticeTitle());
        notice.setNoticeContents(noticeUpdateRequestDto.getNoticeContents());
        notice.setImage(updatedImageUrls);

        noticeRepository.save(notice);
    }

    // 공지사항 삭제
    public void deleteNotice(String id, String userId) {
        NoticeDomain notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + " 게시물이 없습니다."));

        // 연결된 이미지들 삭제
        if (notice.getImage() != null) {
            for (String imageUrl : notice.getImage()) {
                deleteImage(imageUrl);
            }
        }

        noticeRepository.deleteById(id);
    }

    public Map<String, NoticeSummaryDto> getAdjacentNotices(String id) {
        Map<String, NoticeSummaryDto> result = new HashMap<>();

        NoticeDomain currentNotice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + " 게시물이 없습니다."));
        LocalDateTime currentCreateAt = currentNotice.getCreateAt();

        // 이전글 조회
        List<NoticeDomain> prevNotices = noticeRepository.findPrevNotices(currentCreateAt);
        if (!prevNotices.isEmpty()) {
            NoticeDomain prevNotice = prevNotices.stream()
                    .min((a, b) -> a.getCreateAt().compareTo(b.getCreateAt()))
                    .get();
            NoticeSummaryDto prevDto = new NoticeSummaryDto();
            prevDto.setId(prevNotice.getId());
            prevDto.setNoticeTitle(prevNotice.getNoticeTitle());
            result.put("previous", prevDto);
        }

        // 다음글 조회
        List<NoticeDomain> nextNotices = noticeRepository.findNextNotices(currentCreateAt);
        if (!nextNotices.isEmpty()) {
            NoticeDomain nextNotice = nextNotices.stream()
                    .max((a, b) -> a.getCreateAt().compareTo(b.getCreateAt()))
                    .get();
            NoticeSummaryDto nextDto = new NoticeSummaryDto();
            nextDto.setId(nextNotice.getId());
            nextDto.setNoticeTitle(nextNotice.getNoticeTitle());
            result.put("next", nextDto);
        }

        return result;
    }
}