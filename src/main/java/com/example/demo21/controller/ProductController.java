package com.example.demo21.controller;


import com.example.demo21.dto.ProductEnquiryRequest;
import com.example.demo21.dto.ProductRequest;
import com.example.demo21.dto.ProductResponse;
import com.example.demo21.dto.SubCategoryResponse;
import com.example.demo21.entity.SubCategoryDocument;
import com.example.demo21.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping(value = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponse>> getAllCategory(){
        try {
            long startTime = System.nanoTime();
            List<ProductResponse> pr = productService.getAllCategory();
            long endTime = System.nanoTime();
            logger.info("getAllCategory API took {} ms", TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
            return ResponseEntity.ok().body(pr);
        } catch (Exception e) {
            logger.error("Error in getAllCategory: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping(value = "/subcategory", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SubCategoryResponse>> getAllSubCategory(){
        try {
            long startTime = System.nanoTime();
            List<SubCategoryResponse> pr = productService.getAllSubCategory();
            long endTime = System.nanoTime();
            logger.info("getAllSubCategory API took {} ms", TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
            return ResponseEntity.ok().body(pr);
        } catch (Exception e) {
            logger.error("Error in getAllSubCategory: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/subcategory/category/{id}")
    public ResponseEntity<List<SubCategoryDocument>> getAllCategory(@PathVariable ("id") String id){
        List<SubCategoryDocument> pr=productService.getSubCategoryByCategoryId(id);
        return ResponseEntity.ok().body(pr);
    }

    @GetMapping("/subcategory/{name}")
    public ResponseEntity<SubCategoryResponse> fetchSubCategoryByName(@PathVariable ("name") String name){
        String originalName = slugToOriginalName(name);
        SubCategoryResponse responses =productService.getSubCategoryByName(originalName);
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/product-category")
    public ResponseEntity<List<ProductResponse>> getProductsByCategoryAndSubCategory(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory) {
        List<ProductResponse> products = productService.getProductCategoryByCatIdAndSubCatId(category, subcategory);
        return ResponseEntity.ok(products);
    }
    @GetMapping("/product-category/{name}")
    public ResponseEntity<ProductResponse> fetchProductCategoryById(@PathVariable ("name") String name){
        String originalName = slugToOriginalName(name);
        ProductResponse responses = productService.getProductCategoryByName(originalName);
        return ResponseEntity.ok().body(responses);
    }

    private String slugToOriginalName(String slug) {
        if (slug == null || slug.isEmpty()) {
            return slug;
        }

        String formattedString = slug.replace("-", " ");
        formattedString = capitalizeFirstLetterOfEachWord(formattedString);

        // Replace placeholders with actual commas
        return formattedString.replace("~", ",");
    }

    private String capitalizeFirstLetterOfEachWord(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        // Handle parentheses and commas by adding spaces around them
        str = str.replace("(", " ( ").replace(")", " ) ").replace(",", " , ");
        String[] words = str.split("\\s+");
        StringBuilder capitalizedStr = new StringBuilder();

        for (String word : words) {
            if (word.equalsIgnoreCase("and")) {
                capitalizedStr.append(word.toLowerCase()).append(" ");
            } else if (word.equals("(") || word.equals(")") || word.equals(",")) {
                // Add parentheses and commas without spaces
                capitalizedStr.append(word);
            } else if (!word.isEmpty()) {
                capitalizedStr.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase()).append(" ");
            }
        }

        // Clean up any double spaces and ensure proper formatting of parentheses and commas
        return capitalizedStr.toString().trim()
                .replaceAll("\\s+", " ")
                .replace(" ( ", "(")
                .replace(" ) ", ")")
                .replace("( ", "(")
                .replace(" )", ")")
                .replace(" , ", ", ");
    }

    @GetMapping("/products-category/subCategoryName")
    public ResponseEntity<List<ProductResponse>> getProductsBySubCategoryName(
            @RequestParam String subCategoryName) {
        String originalName = slugToOriginalName(subCategoryName);
        List<ProductResponse> productCategories =
                productService.getProductCategoriesBySubCategoryName(originalName);
        return ResponseEntity.ok(productCategories);
    }

    @GetMapping(value = "/product-category/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResponse>> fetchAllProductCategory(){
        try {
            long startTime = System.nanoTime();
            List<ProductResponse> responses = productService.getAllProductCategory();
            long endTime = System.nanoTime();
            logger.info("fetchAllProductCategory API took {} ms", TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
            return ResponseEntity.ok().body(responses);
        } catch (Exception e) {
            logger.error("Error in fetchAllProductCategory: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    @GetMapping("/product-category/category/{categoryName}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategoryName(@PathVariable String categoryName) {
        try {
            List<ProductResponse> products = productService.getProductCategoryByCategoryName(categoryName);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }
    @PostMapping("/productenquiry")
    public ResponseEntity<String> submitEnquiry( @RequestBody ProductEnquiryRequest enquiryRequest) {
        try {
            String response=productService.saveEnquiry(enquiryRequest);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while submitting the enquiry.");
        }
    }

    @GetMapping("/bestsellingproduct")
    public ResponseEntity<List<ProductResponse>> fetchAllBestSellingProduct() {
        try {
            List<ProductResponse> response=productService.getAllBestSellingProduct();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    @GetMapping("/shop-by-category")
    public ResponseEntity<Map<String, Map<String, String>>> shopByCategory() {
        Map<String, Map<String, String>> response=productService.getShopByCategory();
       return ResponseEntity.ok().body(response);
    }

}
