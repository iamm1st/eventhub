package com.eventhub.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerApplicationReviewRequest {

    @Size(max = 2000, message = "Admin comment must not exceed 2000 characters")
    private String adminComment;
}