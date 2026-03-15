package com.imes.assignment.entity;

public enum AssignmentStatus {
    DRAFT,
    PUBLISHED,
    ACCEPTED,
    REJECTED,
    IN_PROGRESS,
    SUBMITTED,
    REVISION_REQUESTED,
    APPROVED,
    COMPLETED,
    CANCELLED,

    // Legacy status kept for backward compatibility with existing data
    OPEN
}
