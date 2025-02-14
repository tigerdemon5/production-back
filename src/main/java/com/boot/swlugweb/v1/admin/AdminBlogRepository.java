package com.boot.swlugweb.v1.admin;

import com.boot.swlugweb.v1.blog.BlogDomain;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminBlogRepository extends MongoRepository<BlogDomain, String> {
}
