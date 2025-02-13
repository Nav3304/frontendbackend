package com.example.demo21.dto;

import lombok.Data;

import java.util.List;

@Data
public class SuppliersResponse {
    private String id;
    private String name;
    private String logoUrl;
    private List<String> category;
    private List<String> subcategory;
    private List<String> productCategory;

    public SuppliersResponse(String id, String name, List<String> category, List<String> subcategory, List<String> productCategory, String logoUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.productCategory = productCategory;
        this.logoUrl = logoUrl;
    }
}
