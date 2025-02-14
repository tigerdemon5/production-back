package com.boot.swlugweb.v1.notice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends MongoRepository<NoticeDomain, String> {
    @Query(value = "{ " +
            "'board_category': 0, " +
            "'board_title': { $regex: ?0, $options: 'i' }, " +
            "'is_delete': ?1 }",
            sort = "{ 'is_pin': -1, 'created_at': -1, '_id': -1 }")
    Page<NoticeDto> findByBoardTitleContainingAndIsDelete(
            String searchTerm,
            Integer isDelete,
            Pageable pageable
    );

    @Query(value = "{ 'board_category': 0, 'is_delete': ?0 }",
            sort = "{ 'is_pin': -1, 'created_at': -1, '_id': -1 }")
    Page<NoticeDto> findByIsDeleteOrderByIsPinDescCreateAtDesc(
            Integer isDelete,
            Pageable pageable
    );

    @Query(count = true, value = "{ 'board_category': ?0, 'is_delete': ?1 }")
    long countByBoardCategoryAndIsDelete(Integer boardCategory, Integer isDelete);

    @Query(value = "{ " +
            "'board_category': 0, " +
            "'is_delete': ?0, " +
            "'created_at': { $gt: ?1 }" +
            "}",
            count = true)
    long countOlderNotices(int isDelete, LocalDateTime createAt);

    // 이전 게시물 조회 (더 최신 글 중 가장 가까운 1개)
    @Query(value = "{ 'board_category': 0, 'is_delete': 0, 'created_at': { $gt: ?0 }}")
    List<NoticeDomain> findPrevNotices(LocalDateTime createAt);

    // 다음 게시물 조회 (더 오래된 글 중 가장 가까운 1개)
    @Query(value = "{ 'board_category': 0, 'is_delete': 0, 'created_at': { $lt: ?0 }}")
    List<NoticeDomain> findNextNotices(LocalDateTime createAt);
}
