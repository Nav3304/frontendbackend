package com.example.demo21.repository;

import com.example.demo21.dto.ExtraDtoResponse;
import com.example.demo21.entity.CategoryDocument;
import com.example.demo21.entity.ProductCategoryDocument;
import com.example.demo21.entity.SubCategoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableMongoRepositories
public interface SubCategoryRepository extends MongoRepository<SubCategoryDocument,String> {
    @Query("{'categoryId': ?0}")
    List<SubCategoryDocument> findByCategoryId(String id);
    SubCategoryDocument findByName(String name);
    @Query("{ 'name': { '$regex': ?0,'$options': 'i'} }")
    List<SubCategoryDocument> searchByName(String name);

    @Query("{'name':{'$in':?0}}")
    List<SubCategoryDocument> findByNameList(List<String> name);

    @Query("{'_id': {'$in': ?0}}")
    List<SubCategoryDocument> findByIds(List<String> ids);
}
