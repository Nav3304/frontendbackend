package com.example.demo21.service.Implementation;

import com.example.demo21.dto.ProductEnquiryRequest;
import com.example.demo21.dto.ProductResponse;
import com.example.demo21.dto.SubCategoryResponse;
import com.example.demo21.entity.*;
import com.example.demo21.repository.*;
import com.example.demo21.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

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

    @Override
    public List<ProductResponse> getAllCategory () {
        List<CategoryDocument> categoryDocument=categoryRepository.findAll();
        List<ProductResponse> productResponseList=new ArrayList<>();
        for(CategoryDocument cd: categoryDocument){
            ProductResponse pr=new ProductResponse();
            pr.setId(cd.getId());
            pr.setName(cd.getName());
            pr.setDescription(cd.getDescription());
            pr.setImageUrl(cd.getImages());
//            pr.setProperties(cd.getProperties());
            pr.setTags(cd.getTags());
            pr.setFeatured(cd.isFeatured());
            pr.setBestSeller(cd.isBestSeller());

            productResponseList.add(pr);
        }
        return productResponseList;
    }

    @Override
    public List<SubCategoryResponse> getAllSubCategory () {
        List<SubCategoryDocument> subCategoryDocumentList=subCategoryRepository.findAll();
        List<SubCategoryResponse> productResponseList=new ArrayList<>();
        Map<String, String> mp = categoryData(true);
        for(SubCategoryDocument sub: subCategoryDocumentList){
            SubCategoryResponse subCat=new SubCategoryResponse();
            subCat.setId(sub.getId());
            subCat.setName(sub.getName());
            subCat.setDescription(sub.getDescription());
            subCat.setCategoryName(mp.get(sub.getCategoryId()));
            subCat.setImages(sub.getImages());
            subCat.setTags(sub.getTags());
            subCat.setFeatured(sub.isFeatured());
            subCat.setBestSeller(sub.isBestSeller());
            productResponseList.add(subCat);
        }
        return productResponseList;
    }

    @Override
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
            return response;
        }
        else return null;
    }

    @Override
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
    public List<ProductResponse> getAllProductCategory () {
        List<ProductCategoryDocument> productCategoryDocumentList=productCategoryRepository.findAll();
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
    public List<ProductResponse> getProductCategoryByCategoryName(String categoryName) {
        Map<String, String> categoryList = categoryData(false);
        Map<String, String> categoryList2 = categoryData(true);

        String categoryId1="";
        String categoryId2="";
        if(categoryName.equals("Replacement Parts")) {
            categoryId1  = categoryList.get(categoryName);
            categoryId2=categoryList.get("Fluids & Lubricants");
        }
        else {
            categoryId1=categoryList.get("Tools & Equipments");
            categoryId2=categoryList.get("Industrial & Safety");
        }

        List<ProductCategoryDocument> productDocuments = productCategoryRepository.findByCategoryIds(categoryId1,categoryId2);
        List<ProductResponse> productResponses = new ArrayList<>();

        for (ProductCategoryDocument productDocument : productDocuments) {
            ProductResponse response = new ProductResponse();
            response.setId(productDocument.getId());
            response.setName(productDocument.getName());
            response.setDescription(productDocument.getDescription());
            response.setImageUrl(productDocument.getImageUrl());
            response.setCategoryName(categoryList2.get(productDocument.getCategoryId()));
            response.setSubCategoryName(categoryList2.get(productDocument.getSubCategoryId()));
            response.setTags(productDocument.getTags());
            response.setFeatured(productDocument.isFeatured());
            response.setBestSeller(productDocument.isBestSeller());
            productResponses.add(response);
        }

        return productResponses;
    }

    @Override
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
            return response;
        }
        return null;
    }

    @Override
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

    public Map<String,String> categoryData(boolean value){
        List<CategoryDocument> categoryDocumentsList=categoryRepository.findAll();
        Map<String,String> mpList=new HashMap<>();
        if(value) {
            for (CategoryDocument cat : categoryDocumentsList) {
                mpList.put(cat.getId(), cat.getName());
            }
        }
        else {
            for (CategoryDocument cat : categoryDocumentsList) {
                mpList.put(cat.getName(), cat.getId());
            }
        }
        return mpList;
    }
    public Map<String,String> subCategoryData(){
        List<SubCategoryDocument> categoryDocumentsList=subCategoryRepository.findAll();
        Map<String,String> mpList=new HashMap<>();
        for(SubCategoryDocument cat: categoryDocumentsList){
            mpList.put(cat.getId(),cat.getName());
        }
        return mpList;
    }
}
