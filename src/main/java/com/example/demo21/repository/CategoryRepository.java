package com.example.demo21.repository;

import com.example.demo21.entity.CategoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableMongoRepositories
public interface CategoryRepository extends MongoRepository<CategoryDocument,String> {
    @Query("{'name':{'$in':?0}}")
    List<CategoryDocument> findByNameList(List<String> name);
    @Query("{'_id': {'$in': ?0}}")
    List<CategoryDocument> findByIds(List<String> ids);
}
