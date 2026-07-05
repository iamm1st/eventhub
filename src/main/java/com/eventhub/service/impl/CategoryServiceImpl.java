package com.eventhub.service.impl;

import com.eventhub.dto.request.CategoryCreateRequest;
import com.eventhub.dto.request.CategoryUpdateRequest;
import com.eventhub.dto.response.CategoryResponse;
import com.eventhub.entity.EventCategory;
import com.eventhub.exception.category.CategoryAlreadyExistsException;
import com.eventhub.exception.category.CategoryInUseException;
import com.eventhub.exception.category.CategoryNotFoundException;
import com.eventhub.mapper.CategoryMapper;
import com.eventhub.repository.EventCategoryRepository;
import com.eventhub.repository.EventRepository;
import com.eventhub.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final EventCategoryRepository eventCategoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return eventCategoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        EventCategory category = findCategoryById(id);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        String normalizedName = normalizeName(request.getName());

        if (eventCategoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new CategoryAlreadyExistsException(normalizedName);
        }

        EventCategory category = EventCategory.builder()
                .name(normalizedName)
                .build();

        EventCategory savedCategory = eventCategoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        EventCategory category = findCategoryById(id);
        String normalizedName = normalizeName(request.getName());

        eventCategoryRepository.findByNameIgnoreCase(normalizedName)
                .filter(existingCategory -> !existingCategory.getId().equals(id))
                .ifPresent(existingCategory -> {
                    throw new CategoryAlreadyExistsException(normalizedName);
                });

        category.setName(normalizedName);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        EventCategory category = findCategoryById(id);

        if (eventRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException(id);
        }

        eventCategoryRepository.delete(category);
    }

    private EventCategory findCategoryById(Long id) {
        return eventCategoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private String normalizeName(String name) {
        return name.trim();
    }
}