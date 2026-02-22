package com.imes.common.dto.department;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDepartmentRequest {
    
    @Size(min = 3, max = 100, message = "Department name must be between 3 and 100 characters")
    private String name;
    
    @Pattern(regexp = "^[A-Z0-9_]{2,50}$", 
             message = "Code must be 2-50 characters, uppercase letters, numbers, and underscores only")
    private String code;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Long managerId;
}
