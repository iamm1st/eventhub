package com.eventhub.service;

import com.eventhub.dto.request.CategoryCreateRequest;
import com.eventhub.dto.request.CategoryUpdateRequest;
import com.eventhub.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<CategoryResponse> getAllCategories(Pageable pageable);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse updateCategory(Long id, CategoryUpdateRequest request);

    void deleteCategory(Long id);
}