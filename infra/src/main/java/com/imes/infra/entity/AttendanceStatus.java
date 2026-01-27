package com.imes.infra.entity;

/**
 * Attendance Status Enum
 * 
 * Business Rules:
 * - PRESENT: Check-in before 9:00 AM
 * - LATE: Check-in after 9:00 AM
 * - ABSENT: No check-in record (marked by mentor/HR)
 * - LEAVE: Pre-approved leave request
 * - HALF_DAY: Worked less than 4 hours
 */
public enum AttendanceStatus {
    PRESENT,    // On-time attendance
    LATE,       // Late check-in (after 9:00 AM)
    ABSENT,     // Did not check in
    LEAVE,      // Approved leave
    HALF_DAY    // Worked < 4 hours
}
