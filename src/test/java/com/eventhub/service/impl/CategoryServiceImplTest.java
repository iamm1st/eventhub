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
import com.eventhub.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private EventCategoryRepository eventCategoryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategoryShouldSaveCategoryWithTrimmedName() {
        CategoryCreateRequest request = CategoryCreateRequest.builder().name(" IT ").build();

        CategoryResponse expectedResponse = CategoryResponse.builder().id(1L).name("IT").build();

        when(eventCategoryRepository.existsByNameIgnoreCase("IT")).thenReturn(false);
        when(eventCategoryRepository.save(any(EventCategory.class))).thenAnswer(invocation -> {
            EventCategory category = invocation.getArgument(0);
            category.setId(1L);
            return category;
        });
        when(categoryMapper.toResponse(any(EventCategory.class))).thenReturn(expectedResponse);

        CategoryResponse actualResponse = categoryService.createCategory(request);

        assertEquals(expectedResponse, actualResponse);
        verify(eventCategoryRepository).save(any(EventCategory.class));
    }

    @Test
    void createCategoryShouldThrowExceptionWhenNameAlreadyExists() {
        CategoryCreateRequest request = CategoryCreateRequest.builder().name("IT").build();

        when(eventCategoryRepository.existsByNameIgnoreCase("IT")).thenReturn(true);

        assertThrows(CategoryAlreadyExistsException.class, () -> categoryService.createCategory(request));

        verify(eventCategoryRepository, never()).save(any(EventCategory.class));
    }

    @Test
    void getCategoryByIdShouldReturnCategory() {
        EventCategory category = TestDataFactory.category(1L);

        CategoryResponse expectedResponse = CategoryResponse.builder().id(category.getId()).name(category.getName()).build();

        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);

        CategoryResponse actualResponse = categoryService.getCategoryById(category.getId());

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void getCategoryByIdShouldThrowExceptionWhenCategoryNotFound() {
        when(eventCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(99L));
    }

    @Test
    void updateCategoryShouldChangeCategoryName() {
        EventCategory category = TestDataFactory.category(1L);

        CategoryUpdateRequest request = CategoryUpdateRequest.builder().name(" Education ").build();

        CategoryResponse expectedResponse = CategoryResponse.builder().id(category.getId()).name("Education").build();

        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(eventCategoryRepository.findByNameIgnoreCase("Education")).thenReturn(Optional.empty());
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);

        CategoryResponse actualResponse = categoryService.updateCategory(category.getId(), request);

        assertEquals(expectedResponse, actualResponse);
        assertEquals("Education", category.getName());
    }

    @Test
    void deleteCategoryShouldDeleteCategoryWhenItIsNotUsed() {
        EventCategory category = TestDataFactory.category(1L);

        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(category.getId())).thenReturn(false);

        categoryService.deleteCategory(category.getId());

        verify(eventCategoryRepository).delete(category);
    }

    @Test
    void deleteCategoryShouldThrowExceptionWhenCategoryIsUsed() {
        EventCategory category = TestDataFactory.category(1L);

        when(eventCategoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(eventRepository.existsByCategoryId(category.getId())).thenReturn(true);

        assertThrows(CategoryInUseException.class, () -> categoryService.deleteCategory(category.getId()));

        verify(eventCategoryRepository, never()).delete(category);
    }
}