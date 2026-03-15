package com.imes.assignment.dto;

import com.imes.assignment.entity.AssignmentStatus;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate deadline;
    private Long mentorId;
    private String mentorName;
    private AssignmentStatus status;
    private int submissionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
