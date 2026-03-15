package com.imes.assignment.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long internId;
    private String internName;
    private String content;
    private String attachmentUrl;
    private LocalDateTime submittedAt;
    private Boolean isLate;
    private Double score;
    private String mentorComments;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
}
