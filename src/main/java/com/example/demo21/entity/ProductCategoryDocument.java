package com.example.demo21.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "ProductCategory")
public class ProductCategoryDocument {
    private String id;
    private String name;
    private String description;
    private List<String> imageUrl;
    private String categoryId;
    private String subCategoryId;
    private List<String> tags;
    private boolean featured;
    private boolean bestSeller;
    private Integer productPosition;
    private Map<String,Integer> brandAndPosition;
}
