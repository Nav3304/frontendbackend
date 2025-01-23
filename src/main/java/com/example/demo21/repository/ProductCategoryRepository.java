package com.example.demo21.repository;

import com.example.demo21.entity.ProductCategoryDocument;
import com.example.demo21.entity.SubCategoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@EnableMongoRepositories
public interface ProductCategoryRepository extends MongoRepository<ProductCategoryDocument,String> {
    @Query("{$and :[{'categoryId': ?0},{'subCategoryId': ?1}] }")
    List<ProductCategoryDocument> findByCategoryAndSubcategory(String categoryId, String subcategoryId);

    List<ProductCategoryDocument> findBySubCategoryId(String subCategoryId);
    @Query("{$or: [{'categoryId': ?0}, {'categoryId': ?1}]}")
    List<ProductCategoryDocument> findByCategoryIds(String categoryId1, String categoryId2);
    @Query("{'categoryId': ?0}")
    List<ProductCategoryDocument> findByCategoryId(String id);
    @Query("{'name':?0}")
    ProductCategoryDocument findByName(String name);
    @Query("{'bestSeller': true}")
    List<ProductCategoryDocument> getAllBestSeller();
    @Query("{ 'name': { '$regex': ?0, '$options': 'i' } }")
    List<ProductCategoryDocument> searchByName(String name);

    @Query("{'name':{'$in':?0}}")
    List<ProductCategoryDocument> findByNameList(List<String> name);
}
