package com.example.demo21.service.Implementation;

import com.example.demo21.dto.ProductResponse;
import com.example.demo21.dto.SuppliersResponse;
import com.example.demo21.entity.CategoryDocument;
import com.example.demo21.entity.ProductCategoryDocument;
import com.example.demo21.entity.SubCategoryDocument;
import com.example.demo21.entity.SuppliersDocument;
import com.example.demo21.repository.CategoryRepository;
import com.example.demo21.repository.ProductCategoryRepository;
import com.example.demo21.repository.SubCategoryRepository;
import com.example.demo21.repository.SuppliersRepository;
import com.example.demo21.service.SuppliersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SuppliersServiceImpl implements SuppliersService {
    @Autowired
    private SuppliersRepository suppliersRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private SubCategoryRepository subCategoryRepository;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Override
    public List<SuppliersResponse> getSuppliersByProductCategory (String query) {
//        List<SuppliersDocument> suppliers = suppliersRepository.searchByName(query);
//
//        List<SuppliersResponse> response = new ArrayList<>();
//        for (SuppliersDocument supplier : suppliers) {
//            SuppliersResponse suppliersResponse = new SuppliersResponse();
//            suppliersResponse.setId(supplier.getId());
//            suppliersResponse.setName(supplier.getName());
//            suppliersResponse.setLogoUrl(supplier.getLogoUrl());
//            suppliersResponse.setCategory(supplier.getCategory());  // Assuming headings field in Supplier
//            suppliersResponse.setSubcategory(supplier.getSubcategory());
//            suppliersResponse.setProductCategory(supplier.getProductCategory());
//            response.add(suppliersResponse);
//        }

        return null;
    }

    @Override
    public List<SuppliersResponse> getSuppliersBySubCategory (String query) {
//        String regex = Pattern.compile("&").matcher(query).replaceAll("(and|&)");
//        List<SuppliersDocument> suppliers = suppliersRepository.searchByNameBySubCategory(regex);
//        List<SuppliersResponse> response = new ArrayList<>();
//        for (SuppliersDocument supplier : suppliers) {
//            SuppliersResponse suppliersResponse = new SuppliersResponse();
//            suppliersResponse.setId(supplier.getId());
//            suppliersResponse.setName(supplier.getName());
//            suppliersResponse.setLogoUrl(supplier.getLogoUrl());
//            suppliersResponse.setCategory(supplier.getCategory());  // Assuming headings field in Supplier
//            suppliersResponse.setSubcategory(supplier.getSubcategory());
//            suppliersResponse.setProductCategory(supplier.getProductCategory());
//            response.add(suppliersResponse);
//        }

        return null;
    }

    @Override
    public Map<String, List<ProductResponse>> getSuppliersByName(String name) {
        // Fetch the supplier document
        SuppliersDocument suppliersDocument = suppliersRepository.findByName(name);
        List<String> categoryIds = suppliersDocument.getCategories();
        List<String> subCategoryIds = suppliersDocument.getSubCategories();
        List<String> productCategoryNames = suppliersDocument.getProductCategories();

        // Fetch the category, subcategory, and product category documents
        List<CategoryDocument> categoryDocumentList = categoryRepository.findByNameList(categoryIds);
        List<SubCategoryDocument> subCategoryDocumentList = subCategoryRepository.findByNameList(subCategoryIds);
        List<ProductCategoryDocument> productCategoryDocumentList = productCategoryRepository.findByNameList(productCategoryNames);

        // Map category IDs to category names for quick lookup
        Map<String, String> categoryIdToNameMap = categoryDocumentList.stream()
                .collect(Collectors.toMap(CategoryDocument::getId, CategoryDocument::getName));

        // Map subcategory IDs to subcategory names for quick lookup
        Map<String, String> subCategoryIdToNameMap = subCategoryDocumentList.stream()
                .collect(Collectors.toMap(SubCategoryDocument::getId, SubCategoryDocument::getName));

        // Map product category IDs to their names for quick lookup
        Map<String, String> productCategoryIdToNameMap = productCategoryDocumentList.stream()
                .collect(Collectors.toMap(ProductCategoryDocument::getId, ProductCategoryDocument::getName));

        // Group product categories by subcategory ID
        Map<String, List<ProductCategoryDocument>> productCategoryMap = productCategoryDocumentList.stream()
                .collect(Collectors.groupingBy(ProductCategoryDocument::getSubCategoryId));

        // Map the subcategory name to its associated product categories
        Map<String, List<ProductResponse>> response = new HashMap<>();
        for (SubCategoryDocument subCategory : subCategoryDocumentList) {
            String subCategoryName = subCategory.getName(); // Use subcategory name as the key
            String subCategoryId = subCategory.getId();

            // Get product categories for the subcategory
            List<ProductCategoryDocument> productCategories = productCategoryMap.getOrDefault(subCategoryId, new ArrayList<>());

            // Map product categories to ProductResponse
            List<ProductResponse> productResponses = productCategories.stream()
                    .map(product -> new ProductResponse(
                            product.getName(), // Product name
                            product.getImageUrl(), // Product image URLs
                            categoryIdToNameMap.get(product.getCategoryId()), // Fetch category name from the category map
                            subCategoryIdToNameMap.get(product.getSubCategoryId()) // Fetch subcategory name from the subcategory map
                    ))
                    .collect(Collectors.toList());

            response.put(subCategoryName, productResponses);
        }

        return response;
    }



    public Map<String,String> subCategoryData(List<SubCategoryDocument> categoryDocumentsList){
        Map<String,String> mpList=new HashMap<>();
        for(SubCategoryDocument cat: categoryDocumentsList){
            mpList.put(cat.getId(),cat.getName());
        }
        return mpList;
    }

}
