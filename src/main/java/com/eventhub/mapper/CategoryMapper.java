package com.eventhub.mapper;

import com.eventhub.dto.response.CategoryResponse;
import com.eventhub.entity.EventCategory;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(EventCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}