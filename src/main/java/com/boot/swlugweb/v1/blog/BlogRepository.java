package com.boot.swlugweb.v1.blog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlogRepository extends MongoRepository<BlogDomain, String> {
    @Query(value = "{ " +
            "'board_category' : { $in: ?0 }, " +
            "'board_title': { $regex: ?1, $options: 'i' }, " +
            "'is_delete': ?2 }",
            sort = "{ 'is_pin': -1, 'created_at': -1, '_id': -1 }")
    Page<BlogDto> findByBlogTitleContainingAndIsDelete(
            List<Integer> category,
            String searchTerm,
            Integer isDelete,
            Pageable pageable
    );

    @Query(value = "{ " +
            "'board_category' : { $in: ?0 }, " +
            "'board_title': { $regex: ?1, $options: 'i' }, " +
            "'tag': { $in: ?2 }, " +
            "'is_delete': ?3 }",
            sort = "{ 'is_pin': -1, 'created_at': -1, '_id': -1 }")
    Page<BlogDto> findByBlogTitleContainingAndIsDeleteAndTag(
            List<Integer> category,
            String searchTerm,
            List<String> tag,
            Integer isDelete,
            Pageable pageable
    );

    @Query(value = "{ 'board_category' : { $in: ?0 }, 'is_delete': ?1 }",
            sort = "{ 'is_pin':  -1, 'created_at': -1, '_id': -1 }")
    Page<BlogDto> findByBlogIsDeleteOrderByIsPinDescCreateAtDesc(
            List<Integer> category,
            Integer isDelete,
            Pageable pageable
    );

    @Query(value = "{ 'board_category' : { $in: ?0 }, 'tag': { $in: ?1 }, 'is_delete': ?2 }",
            sort = "{ 'is_pin':  -1, 'created_at': -1, '_id': -1 }")
    Page<BlogDto> findByBlogIsDeleteOrderByIsPinDescCreateAtDescAndTag(
            List<Integer> category,
            List<String> tag,
            Integer isDelete,
            Pageable pageable
    );

    @Query(value = "{ 'board_category': { $in: ?0 }, 'is_delete': 0, 'created_at': { $gt: ?1 }}",
            sort = "{ 'created_at': 1 }")
    List<BlogDomain> findPrevBlogs(List<Integer> categories, LocalDateTime createAt);

    @Query(value = "{ 'board_category': { $in: ?0 }, 'is_delete': 0, 'created_at': { $lt: ?1 }}",
            sort = "{ 'created_at': -1 }")
    List<BlogDomain> findNextBlogs(List<Integer> categories, LocalDateTime createAt);

    // tags 필드에서 중복을 제거한 리스트 반환
    @Aggregation(pipeline = {
            "{ '$unwind': '$tag' }",   // tags 배열을 하나씩 풀어줌
            "{ '$group': { '_id': '$tag' } }", // 중복을 제거하고 하나씩 그룹화
            "{ '$project': { '_id': 0, 'tag': '$_id' } }" // _id를 제외하고 tag 값만 반환
    })
    List<String> findAllTags();
}