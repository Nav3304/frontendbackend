package com.example.demo21.service.Implementation;

import com.example.demo21.dto.ProductEnquiryRequest;
import com.example.demo21.dto.ProductResponse;
import com.example.demo21.dto.SubCategoryResponse;
import com.example.demo21.entity.*;
import com.example.demo21.repository.*;
import com.example.demo21.service.ProductService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductEnquiryRepository productEnquiryRepository;

    @Autowired
    private ContactServiceImpl contactService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Cache maps for faster access - initialized lazily to prevent startup errors
    private volatile Map<String, String> categoryIdToNameMap;
    private volatile Map<String, String> categoryNameToIdMap;
    private volatile Map<String, String> subCategoryIdToNameMap;
    
    // Instead of eager loading at startup, we'll initialize these maps lazily
    // This prevents application startup failures due to Redis connection issues
    
    // Helper method to get category name safely
    private String getCategoryName(String categoryId) {
        if (categoryIdToNameMap == null) {
            synchronized (this) {
                if (categoryIdToNameMap == null) {
                    try {
                        categoryIdToNameMap = categoryData(true);
                    } catch (Exception e) {
                        logger.error("Error initializing categoryIdToNameMap", e);
                        return null;
                    }
                }
            }
        }
        return categoryIdToNameMap.get(categoryId);
    }
    
    // Helper method to get subcategory name safely
    private String getSubCategoryName(String subCategoryId) {
        if (subCategoryIdToNameMap == null) {
            synchronized (this) {
                if (subCategoryIdToNameMap == null) {
                    try {
                        subCategoryIdToNameMap = subCategoryData();
                    } catch (Exception e) {
                        logger.error("Error initializing subCategoryIdToNameMap", e);
                        return null;
                    }
                }
            }
        }
        return subCategoryIdToNameMap != null ? subCategoryIdToNameMap.get(subCategoryId) : null;
    }
    
    // Helper method to get category id safely
    private String getCategoryId(String categoryName) {
        if (categoryNameToIdMap == null) {
            synchronized (this) {
                if (categoryNameToIdMap == null) {
                    try {
                        categoryNameToIdMap = categoryData(false);
                    } catch (Exception e) {
                        logger.error("Error initializing categoryNameToIdMap", e);
                        return null;
                    }
                }
            }
        }
        return categoryNameToIdMap != null ? categoryNameToIdMap.get(categoryName) : null;
    }

    @Override
    @Cacheable(value = "categories")
    public List<ProductResponse> getAllCategory() {
        try {
            List<CategoryDocument> categoryDocuments = categoryRepository.findAll();
            
            // Use stream for better performance
            return categoryDocuments.stream()
                    .map(this::mapCategoryToProductResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error in getAllCategory", e);
            return Collections.emptyList();
        }
    }
    
    // Helper method to map CategoryDocument to ProductResponse
    private ProductResponse mapCategoryToProductResponse(CategoryDocument cd) {
        ProductResponse pr = new ProductResponse();
        pr.setId(cd.getId());
        pr.setName(cd.getName());
        pr.setDescription(cd.getDescription());
        pr.setImageUrl(cd.getImages());
        pr.setTags(cd.getTags());
        pr.setFeatured(cd.isFeatured());
        pr.setBestSeller(cd.isBestSeller());
        return pr;
    }

    @Override
    @Cacheable(value = "subcategories")
    public List<SubCategoryResponse> getAllSubCategory() {
        try {
            List<SubCategoryDocument> subCategoryDocumentList = subCategoryRepository.findAll();
            
            // Use stream for better performance and reuse existing map
            return subCategoryDocumentList.stream()
                    .map(sub -> {
                        SubCategoryResponse subCat = new SubCategoryResponse();
                        subCat.setId(sub.getId());
                        subCat.setName(sub.getName());
                        subCat.setDescription(sub.getDescription());
                        subCat.setCategoryName(getCategoryName(sub.getCategoryId()));
                        subCat.setImages(sub.getImages());
                        subCat.setTags(sub.getTags());
                        subCat.setFeatured(sub.isFeatured());
                        subCat.setBestSeller(sub.isBestSeller());
                        return subCat;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error in getAllSubCategory", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable(value = "subcategories", key = "#id")
    public List<SubCategoryDocument> getSubCategoryByCategoryId (String id) {

        List<SubCategoryDocument> subCategoryDocument=subCategoryRepository.findByCategoryId(id);
        List<SubCategoryDocument> subCatName=new ArrayList<>();

        for(SubCategoryDocument cd:subCategoryDocument){
            SubCategoryDocument subCatDoc =new SubCategoryDocument();
            subCatDoc.setId(cd.getId());
            subCatDoc.setName(cd.getName());
            subCatName.add(subCatDoc);
        }
        return subCatName;
    }

    @Override
    @Cacheable(value = "products", key = "#categoryId + '_' + #subCategoryId")
    public List<ProductResponse> getProductCategoryByCatIdAndSubCatId(String categoryId, String subCategoryId) {
        List<ProductCategoryDocument> productCategoryDocumentList = new ArrayList<>();

        // If only categoryId is provided and subCategoryId is null
        if (categoryId != null && subCategoryId == null) {
            productCategoryDocumentList = productCategoryRepository.findByCategoryIds(categoryId,null);
        }
        // If both categoryId and subCategoryId are provided
        else {
            productCategoryDocumentList = productCategoryRepository.findByCategoryAndSubcategory(categoryId, subCategoryId);
        }

        // If no products are found, return an empty list
        if (productCategoryDocumentList.isEmpty()) {
            return new ArrayList<>();
        }

        List<ProductResponse> productResponseList = new ArrayList<>();
        Map<String, String> mp1 = categoryData(true);
        Map<String, String> mp2 = subCategoryData();
        for (ProductCategoryDocument pro : productCategoryDocumentList) {
            ProductResponse proRes = new ProductResponse();
            proRes.setId(pro.getId());
            proRes.setName(pro.getName());
            proRes.setDescription(pro.getDescription());
            proRes.setImageUrl(pro.getImageUrl());
            proRes.setCategoryName(mp1.get(pro.getCategoryId()));
            proRes.setSubCategoryName(mp2.get(pro.getSubCategoryId()));
            proRes.setTags(pro.getTags());
            proRes.setFeatured(pro.isFeatured());
            proRes.setBestSeller(pro.isBestSeller());
            productResponseList.add(proRes);
        }

        return productResponseList;
    }


    @Override
    @Cacheable(value = "products", key = "#name")
    public ProductResponse getProductCategoryByName (String name) {
        ProductCategoryDocument document = productCategoryRepository.findByName(name);
        if(document!=null) {
            Map<String, String> mp1 = categoryData(true);
            Map<String, String> mp2 = subCategoryData();
            ProductResponse response = new ProductResponse();
            response.setId(document.getId());
            response.setName(document.getName());
            response.setDescription(document.getDescription());
            response.setImageUrl(document.getImageUrl());
            response.setTags(document.getTags());
            response.setCategoryName(mp1.get(document.getCategoryId()));
            response.setSubCategoryName(mp2.get(document.getSubCategoryId()));
            response.setFeatured(document.isFeatured());
            response.setBestSeller(document.isBestSeller());
            response.setProductPosition(document.getProductPosition());
            response.setBrandAndPosition(document.getBrandAndPosition());
            return response;
        }
        else return null;
    }

    @Override
    @Cacheable(value = "products", key = "#subCategoryName")
    public List<ProductResponse> getProductCategoriesBySubCategoryName (String subCategoryName) {
        SubCategoryDocument subCategory = subCategoryRepository.findByName(subCategoryName);
        if (subCategory == null) {
            throw new RuntimeException("SubCategory not found with name: " + subCategoryName);
        }
        String subCategoryId = subCategory.getId();
        List<ProductCategoryDocument> productCategoryDocumentList=productCategoryRepository.findBySubCategoryId(subCategoryId);
        List<ProductResponse> productResponseList=new ArrayList<>();
        Map<String,String> mp1=categoryData(true);
        Map<String,String>mp2=subCategoryData();
        for (ProductCategoryDocument pro : productCategoryDocumentList) {
            ProductResponse proRes = new ProductResponse();
            proRes.setId(pro.getId());
            proRes.setName(pro.getName());
            proRes.setDescription(pro.getDescription());
            proRes.setImageUrl(pro.getImageUrl());
            proRes.setCategoryName(mp1.get(pro.getCategoryId()));
            proRes.setSubCategoryName(mp2.get(pro.getSubCategoryId()));
            proRes.setTags(pro.getTags());
            proRes.setFeatured(pro.isFeatured());
            proRes.setBestSeller(pro.isBestSeller());
            productResponseList.add(proRes);
        }
        return productResponseList;
    }

    @Override
    @Cacheable(value = "products")
    public List<ProductResponse> getAllProductCategory() {
        List<ProductCategoryDocument> productCategoryDocumentList = productCategoryRepository.findAll();
        
        // Use stream and reuse cached maps
        return productCategoryDocumentList.stream()
                .map(document -> {
                    ProductResponse proRes = new ProductResponse();
                    proRes.setId(document.getId());
                    proRes.setName(document.getName());
                    proRes.setDescription(document.getDescription());
                    proRes.setImageUrl(document.getImageUrl());
                    proRes.setTags(document.getTags());
                    proRes.setCategoryName(getCategoryName(document.getCategoryId()));
                    proRes.setSubCategoryName(getSubCategoryName(document.getSubCategoryId()));
                    proRes.setFeatured(document.isFeatured());
                    proRes.setBestSeller(document.isBestSeller());
                    return proRes;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "products", key = "#categoryName")
    public List<ProductResponse> getProductCategoryByCategoryName(String categoryName) {
        try {
            String categoryId1 = "";
            String categoryId2 = "";
            
            if(categoryName.equals("Replacement Parts")) {
                categoryId1 = getCategoryId(categoryName);
                categoryId2 = getCategoryId("Fluids & Lubricants");
            }
            else {
                categoryId1 = getCategoryId("Tools & Equipments");
                categoryId2 = getCategoryId("Industrial & Safety");
            }
            
            if (categoryId1 == null && categoryId2 == null) {
                logger.error("Category IDs not found for {}", categoryName);
                return Collections.emptyList();
            }

            List<ProductCategoryDocument> productDocuments = productCategoryRepository.findByCategoryIds(categoryId1, categoryId2);
            
            return productDocuments.stream()
                .map(productDocument -> {
                    ProductResponse response = new ProductResponse();
                    response.setId(productDocument.getId());
                    response.setName(productDocument.getName());
                    response.setDescription(productDocument.getDescription());
                    response.setImageUrl(productDocument.getImageUrl());
                    response.setCategoryName(getCategoryName(productDocument.getCategoryId()));
                    response.setSubCategoryName(getSubCategoryName(productDocument.getSubCategoryId()));
                    response.setTags(productDocument.getTags());
                    response.setFeatured(productDocument.isFeatured());
                    response.setBestSeller(productDocument.isBestSeller());
                    return response;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error in getProductCategoryByCategoryName for {}", categoryName, e);
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable(value = "subcategories", key = "#name")
    public SubCategoryResponse getSubCategoryByName (String name) {
        SubCategoryDocument entity = subCategoryRepository.findByName(name);
        Map<String,String> mp=categoryData(true);
        // If present, map the entity to the response and return
        if (entity!=null) {
            SubCategoryResponse response = new SubCategoryResponse();
            response.setId(entity.getId());
            response.setName(entity.getName());
            response.setDescription(entity.getDescription());
            response.setCategoryName(mp.get(entity.getCategoryId()));
            response.setImages(entity.getImages());
            response.setTags(entity.getTags());
            response.setFeatured(entity.isFeatured());
            response.setBestSeller(entity.isBestSeller());
            response.setProductCategoryAndPosition(entity.getProductCategoryAndPosition());
            return response;
        }
        return null;
    }

    @Override
    @CacheEvict(value = {"products", "categories", "subcategories"}, allEntries = true)
    public String saveEnquiry (ProductEnquiryRequest enquiryRequest) {
        ProductEnquiryDocument enquiry = new ProductEnquiryDocument();
        enquiry.setName(enquiryRequest.getName());
        enquiry.setEmail(enquiryRequest.getEmail());
        enquiry.setStore(enquiryRequest.getStore());
        enquiry.setProductName(enquiryRequest.getProductName());
        enquiry.setMessage(enquiryRequest.getMessage());

        String subject = "Product Enquiry";
        String text = "A new contact has been saved with the following details:\n\n"
                + "Name: " + enquiryRequest.getName() + "\n"
                + "Email: " + enquiryRequest.getEmail() + "\n"
                + "Product Name: " + enquiryRequest.getProductName() + "\n"
                + "Store: " + enquiryRequest.getStore() + "\n"
                + "Message: " + enquiryRequest.getMessage();

        contactService.sendEmail("adityagupta.bhl@gmail.com", subject, text);
        productEnquiryRepository.save(enquiry);
        return "Enquiry save successfully";
    }

    @Override
    @Cacheable(value = "products", key = "'bestsellers'")
    public List<ProductResponse> getAllBestSellingProduct () {
        List<ProductCategoryDocument> productCategoryDocumentList=productCategoryRepository.getAllBestSeller();
        List<ProductResponse> productCategoryResponseList=new ArrayList<>();
        Map<String,String>mp1=categoryData(true);
        Map<String,String>mp2=subCategoryData();
        for(ProductCategoryDocument document: productCategoryDocumentList){
            ProductResponse proRes=new ProductResponse();
            proRes.setId(document.getId());
            proRes.setName(document.getName());
            proRes.setDescription(document.getDescription());
            proRes.setImageUrl(document.getImageUrl());
            proRes.setTags(document.getTags());
            proRes.setCategoryName(mp1.get(document.getCategoryId()));
            proRes.setSubCategoryName(mp2.get(document.getSubCategoryId()));
            proRes.setFeatured(document.isFeatured());
            proRes.setBestSeller(document.isBestSeller());
            productCategoryResponseList.add(proRes);
        }
        return productCategoryResponseList;
    }

    @Override
    @Cacheable(value = "shopByCategory")
    public Map<String, Map<String, String>> getShopByCategory() {
        List<ProductCategoryDocument> productCategoryDocumentList = productCategoryRepository.findAll();
        Map<String, String> mp1 = categoryData(true);
        Map<String, String> mp2 = subCategoryData();
        Map<String, Map<String, String>> result = new HashMap<>();

        for (ProductCategoryDocument pro : productCategoryDocumentList) {
            String subCategoryName = mp2.get(pro.getSubCategoryId());
            if (subCategoryName == null) {
                continue;
            }

            result.computeIfAbsent(subCategoryName, k -> new HashMap<>())
                    .put(pro.getName(), mp1.get(pro.getCategoryId()));
        }

        return result;
    }

    @Cacheable(value = "categoryData", key = "#value")
    public Map<String,String> categoryData(boolean value) {
        try {
            // Check if the data is already cached in Redis for better performance
            String redisKey = "categoryData:" + value;
            Map<String,String> mpList = null;
            
            try {
                mpList = (Map<String,String>) redisTemplate.opsForValue().get(redisKey);
            } catch (Exception e) {
                logger.warn("Error retrieving from Redis cache: {}", e.getMessage());
            }
            
            if (mpList != null) {
                return mpList;
            }
            
            List<CategoryDocument> categoryDocumentsList = categoryRepository.findAll();
            
            if (value) {
                mpList = categoryDocumentsList.stream()
                        .collect(Collectors.toMap(
                            CategoryDocument::getId,
                            CategoryDocument::getName,
                            (v1, v2) -> v1, // In case of duplicate keys keep first value
                            HashMap::new
                        ));
            } else {
                mpList = categoryDocumentsList.stream()
                        .collect(Collectors.toMap(
                            CategoryDocument::getName,
                            CategoryDocument::getId,
                            (v1, v2) -> v1, // In case of duplicate keys keep first value
                            HashMap::new
                        ));
            }
            
            // Cache the result in Redis for faster subsequent access
            try {
                redisTemplate.opsForValue().set(redisKey, mpList, 48, TimeUnit.HOURS);
            } catch (Exception e) {
                logger.warn("Error storing in Redis cache: {}", e.getMessage());
            }
            
            return mpList;
        } catch (Exception e) {
            logger.error("Error generating category data map", e);
            return new HashMap<>(); // Return empty map instead of null to prevent NPE
        }
    }
    
    @Cacheable(value = "subcategoryData")
    public Map<String,String> subCategoryData() {
        try {
            // Check if the data is already cached in Redis for better performance
            String redisKey = "subcategoryData";
            Map<String,String> mpList = null;
            
            try {
                mpList = (Map<String,String>) redisTemplate.opsForValue().get(redisKey);
            } catch (Exception e) {
                logger.warn("Error retrieving from Redis cache: {}", e.getMessage());
            }
            
            if (mpList != null) {
                return mpList;
            }
            
            List<SubCategoryDocument> categoryDocumentsList = subCategoryRepository.findAll();
            
            // Use Java 8 streams for better performance
            mpList = categoryDocumentsList.stream()
                    .collect(Collectors.toMap(
                        SubCategoryDocument::getId,
                        SubCategoryDocument::getName,
                        (v1, v2) -> v1, // In case of duplicate keys keep first value
                        HashMap::new
                    ));
            
            // Cache the result in Redis for faster subsequent access
            try {
                redisTemplate.opsForValue().set(redisKey, mpList, 48, TimeUnit.HOURS);
            } catch (Exception e) {
                logger.warn("Error storing in Redis cache: {}", e.getMessage());
            }
            
            return mpList;
        } catch (Exception e) {
            logger.error("Error generating subcategory data map", e);
            return new HashMap<>(); // Return empty map instead of null to prevent NPE
        }
    }
}
