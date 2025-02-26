package com.example.demo21.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "TempSuppliers")
public class SuppliersDocument {
    private String id;
    private String name;
    private String imageUrl;
    private String description;
    private List<String> category;
    private Map<String,Integer> subCategoryAndPosition;
    private List<String> productCategory;
}
