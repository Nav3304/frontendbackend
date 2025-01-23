package com.example.demo21.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private List<String> imageUrl;
    private String categoryName;
    private String subCategoryName;
    private List<String> tags;
    private boolean featured;
    private boolean bestSeller;

    public ProductResponse (String name, List<String> imageUrl, String categoryName, String subCategoryName) {
        this.name=name;
        this.imageUrl=imageUrl;
        this.categoryName=categoryName;
        this.subCategoryName=subCategoryName;
    }
    @Override
    public String toString() {
        return "ProductResponse{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl=" + imageUrl +
                '}';
    }

}
