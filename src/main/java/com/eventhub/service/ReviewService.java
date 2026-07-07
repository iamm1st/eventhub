package com.eventhub.service;

import com.eventhub.dto.request.ReviewCreateRequest;
import com.eventhub.dto.request.ReviewUpdateRequest;
import com.eventhub.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse createReview(Long eventId, ReviewCreateRequest request);

    Page<ReviewResponse> getReviewsByEvent(Long eventId, Pageable pageable);

    ReviewResponse updateReview(Long id, ReviewUpdateRequest request);

    void deleteReview(Long id);
}