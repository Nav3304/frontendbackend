package com.example.demo21.controller;

import com.example.demo21.dto.ProductResponse;
import com.example.demo21.dto.SuppliersResponse;
import com.example.demo21.service.SuppliersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SuppliersController {
    @Autowired
    private SuppliersService suppliersService;

    @GetMapping("/search")
    public ResponseEntity<List<SuppliersResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok().body(suppliersService.getSuppliersByProductCategory(query));
    }

    @GetMapping("/subcategory/search")
    public ResponseEntity<List<SuppliersResponse>> searchBySubCategory(@RequestParam String query) {
        return ResponseEntity.ok().body(suppliersService.getSuppliersBySubCategory(query));
    }
    @GetMapping("/name")
    public ResponseEntity<Map<String, List<ProductResponse>>> getCategories(@RequestParam String names){
        names=slugToOriginalName(names);
        Map<String, List<ProductResponse>> response =suppliersService.getSuppliersByName(names);
        return  ResponseEntity.ok().body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SuppliersResponse>> getAllSuppliers(){
        List<SuppliersResponse> suppliersResponses=suppliersService.getAll();
        return ResponseEntity.ok().body(suppliersResponses);
    }

    private String slugToOriginalName(String slug) {
        if (slug == null || slug.isEmpty()) {
            return slug;
        }
        // Replace hyphens with spaces and convert the entire string to uppercase
        return slug.replace("-", " ").toUpperCase();
    }


}
