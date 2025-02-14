package com.boot.swlugweb.v1.mypage;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyPageBlogRepository extends MongoRepository<MyPageBlogDomain, String> {
    @Query(value = "{ 'user_id': ?0, 'board_category' : {$ne: 0} }", sort = "{ 'created_at': -1 }")
    List<MyPageBlogDomain> findByUserId(@Param("userId") String userId);
}
