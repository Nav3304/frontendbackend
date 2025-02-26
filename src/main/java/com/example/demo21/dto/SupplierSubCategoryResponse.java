package com.example.demo21.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SupplierSubCategoryResponse {
    private Integer position;
    private List<ProductResponse> productCategory=new ArrayList<>();;
}
