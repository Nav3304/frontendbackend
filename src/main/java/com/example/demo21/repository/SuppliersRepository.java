package com.example.demo21.repository;

import com.example.demo21.entity.SuppliersDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.List;

@EnableMongoRepositories
public interface SuppliersRepository extends MongoRepository<SuppliersDocument,String> {
    @Query("{ 'productCategory': { '$regex': ?0} }")
    List<SuppliersDocument> searchByName(String name);

    @Query("{ 'subCategory': { '$regex': ?0, '$options': 'i' } }")
    List<SuppliersDocument> searchByNameBySubCategory(String regex);

    @Query("{ 'name': { '$regex': ?0, '$options': 'i' } }")
    SuppliersDocument findByName(String name);

}
