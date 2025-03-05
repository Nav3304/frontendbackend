package com.example.demo21.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SubCategoryResponse {
    private String id;
    private String name;
    private String description;
    private String categoryName;
    private List<String> images;
    private List<String> tags;
    private boolean featured;
    private boolean bestSeller;
    private Map<String,Integer> productCategoryAndPosition;
}
