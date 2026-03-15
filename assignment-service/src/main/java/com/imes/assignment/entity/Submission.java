package com.imes.assignment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;
    
    @Column(name = "intern_id", nullable = false)
    private Long internId;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "attachment_url")
    private String attachmentUrl;
    
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;
    
    // Feedback fields
    @Column(name = "score")
    private Double score; // 0-10
    
    @Column(name = "mentor_comments", columnDefinition = "TEXT")
    private String mentorComments;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "reviewed_by")
    private Long reviewedBy; // Mentor ID
    
    @Builder.Default
    @Column(name = "is_late", nullable = false)
    private Boolean isLate = false;
    
    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        if (isLate == null) {
            isLate = false;
        }
    }
}
