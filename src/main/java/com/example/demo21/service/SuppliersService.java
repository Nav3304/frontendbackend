package com.example.demo21.service;

import com.example.demo21.dto.ProductResponse;
import com.example.demo21.dto.SuppliersResponse;

import java.util.List;
import java.util.Map;

public interface SuppliersService {
   public List<SuppliersResponse> getSuppliersByProductCategory(String query);
   public List<SuppliersResponse> getSuppliersBySubCategory(String query);
   public SuppliersResponse getSuppliersByName(String name);
   public List<SuppliersResponse> getAll();
}
