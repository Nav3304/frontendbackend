package com.example.demo21.service.Implementation;

import com.example.demo21.dto.ProductResponse;
import com.example.demo21.dto.SupplierSubCategoryResponse;
import com.example.demo21.dto.SuppliersResponse;
import com.example.demo21.entity.*;
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
    public SuppliersResponse getSuppliersByName(String name) {
        SuppliersDocument suppliersDocument = suppliersRepository.findByName(name);
        if (suppliersDocument == null) {
            throw new RuntimeException("Supplier not found with name: " + name);
        }

        Map<String, Integer> subCategoryMap = suppliersDocument.getSubCategoryAndPosition();
        if (subCategoryMap == null || subCategoryMap.isEmpty()) {
            return new SuppliersResponse(
                    suppliersDocument.getId(),
                    suppliersDocument.getName(),
                    suppliersDocument.getImageUrl(),
                    suppliersDocument.getDescription(),
                    new HashMap<>() // Empty subCategory map
            );
        }

        // Get the list of sub-category names
        List<String> subCategoryList = new ArrayList<>(subCategoryMap.keySet());

        // Fetch product categories mapped by sub-category ID
        Map<String, ProductCategoryDocument> productCategoryMap = productCategoryData(subCategoryList);

        // Fetch sub-category names mapped by sub-category ID
        Map<String, String> subCatMap = subCategoryData(subCategoryList);

        Map<String, SupplierSubCategoryResponse> suppSubCat = new HashMap<>();

        for (Map.Entry<String, ProductCategoryDocument> entry : productCategoryMap.entrySet()) {
            ProductCategoryDocument prod = entry.getValue();
            String subCatId = prod.getSubCategoryId();
            String subCatName = subCatMap.get(subCatId);

            if (subCatName != null) {
                SupplierSubCategoryResponse tempSupSubCat = suppSubCat.computeIfAbsent(
                        subCatName,
                        k -> new SupplierSubCategoryResponse()
                );

                if (tempSupSubCat.getPosition() == null) {
                    Integer position = subCategoryMap.get(subCatId);
                    tempSupSubCat.setPosition(position);
                }

                // Convert each product into a ProductCategory object with name and image URL
                for (int i = 0; i < prod.getImageUrl().size(); i++) {
                    String imageUrl = prod.getImageUrl().get(i);
                    String productName = prod.getName(); // Assuming there's a corresponding product name list

                    ProductResponse productCategory = new ProductResponse(productName, imageUrl);
                    tempSupSubCat.getProductCategory().add(productCategory);
                }
            }
        }

        return new SuppliersResponse(
                suppliersDocument.getId(),
                suppliersDocument.getName(),
                suppliersDocument.getImageUrl(),
                suppliersDocument.getDescription(),
                suppSubCat
        );
    }




    @Override
    public List<SuppliersResponse> getAll () {
        List<SuppliersDocument> suppliersDocumentList = suppliersRepository.findAll();
        return suppliersDocumentList.stream().map(supplier -> new SuppliersResponse(supplier.getId(), supplier.getName(), supplier.getImageUrl())).collect(Collectors.toList());
    }


    public Map<String, String> categoryData (List<String> categoryList) {
        List<CategoryDocument> categoryDocumentsList = categoryRepository.findByIds(categoryList);
        Map<String, String> mpList = new HashMap<>();
        for (CategoryDocument cat : categoryDocumentsList) {
            mpList.put(cat.getId(), cat.getName());
        }
        return mpList;
    }

    public Map<String, String> subCategoryData (List<String> suCategoryList) {
        List<SubCategoryDocument> categoryDocumentsList = subCategoryRepository.findByIds(suCategoryList);
        Map<String, String> mpList = new HashMap<>();
        for (SubCategoryDocument cat : categoryDocumentsList) {
            mpList.put(cat.getId(), cat.getName());
        }
        return mpList;
    }
    public Map<String, ProductCategoryDocument> productCategoryData (List<String> ids) {
        List<ProductCategoryDocument> categoryDocumentsList = productCategoryRepository.findBySubCategoryId(ids);
        Map<String, ProductCategoryDocument> mpList = new HashMap<>();
        for (ProductCategoryDocument cat : categoryDocumentsList) {
            mpList.put(cat.getId(), cat);
        }
        return mpList;
    }

}
