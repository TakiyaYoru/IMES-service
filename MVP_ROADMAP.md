# MVP Roadmap - Demo End-User

**Mục tiêu:** Làm tối thiểu để demo các tính năng cơ bản cho end-user  
**Thời gian:** 12-16 giờ (1.5 - 2 ngày)  
**Trạng thái hiện tại:** Core backend đã chạy ổn định (gateway + assignment + attendance analytics) → **Mục tiêu tiếp theo:** hardening + React integration hoàn chỉnh

---

## Chiến lược MVP

### ✅ Đã có (đã hoàn tất phần lõi backend)
- Authentication & Login
- User Management
- Department Management
- Intern Profiles
- Mentor Assignment Tracking
- Microservices Infrastructure

### 🎯 Cần làm tiếp theo

**Priority 1: Backend Hardening (CRITICAL)** - 4-8 giờ
- Regression test các luồng chính qua gateway
- Siết security policy theo role thực tế demo
- Chốt checklist endpoint cho React

**Priority 2: React Integration (IMES-webapp)** - 4-6 giờ  
- Kết nối đầy đủ qua gateway `http://localhost:8080`
- Verify các màn chính: Auth / Intern / Assignment / Attendance
- Fix mapping dữ liệu và error handling

**Priority 3: Documentation Sync** - 1-2 giờ
- Đồng bộ report/checklist theo trạng thái hiện tại
- Loại bỏ nội dung cũ không còn thuộc luồng chính

---

## Phase 1: API Gateway (4-6 giờ)

### Mục tiêu
Tạo 1 entry point duy nhất cho tất cả API calls từ frontend.

### Công việc tối thiểu

**1. Tạo module (30 phút)**
```bash
mkdir -p gateway-service/src/main/java/com/imes/gateway
mkdir -p gateway-service/src/main/resources
```

**2. Dependencies (copy từ module khác)**
- Spring Cloud Gateway
- Eureka Discovery Client
- Spring Boot Web

**3. Application Config (30 phút)**
```yaml
# gateway-service/src/main/resources/application.yml
server:
  port: 8080

spring:
  application:
    name: gateway-service
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/auth/**
        
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/users/**,/departments/**
        
        - id: intern-service
          uri: lb://INTERN-SERVICE
          predicates:
            - Path=/interns/**,/mentor-assignments/**
        
        - id: attendance-service
          uri: lb://ATTENDANCE-SERVICE
          predicates:
            - Path=/attendances/**
        
        - id: assignment-service
          uri: lb://ASSIGNMENT-SERVICE
          predicates:
            - Path=/assignments/**
      
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"

eureka:
  client:
    serviceUrl:
      defaultZone: http://eureka-server:8761/eureka/
```

**4. Main Class (15 phút)**
```java
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

**5. Build & Docker (1 giờ)**
- Build Gradle
- Tạo Dockerfile
- Thêm vào docker-compose.yml
- Test routes

**Kết quả:** Frontend gọi http://localhost:8080/auth/login thay vì http://localhost:8081/auth/login

---

## Phase 2: Assignment Service MVP (4-6 giờ)

### Mục tiêu
Demo workflow: Mentor tạo task → Intern submit → Done

### Entity tối thiểu

**1. Assignment Entity (30 phút)**
```java
@Entity
@Table(name = "assignments")
class Assignment {
    @Id
    @GeneratedValue
    private Long id;
    
    private String title;
    private String description;
    private LocalDate deadline;
    
    @ManyToOne
    private User mentor;  // Người giao
    
    @Enumerated(STRING)
    private AssignmentStatus status; // OPEN, SUBMITTED, COMPLETED
    
    private LocalDateTime createdAt;
}

enum AssignmentStatus {
    OPEN,       // Mới tạo
    SUBMITTED,  // Intern đã nộp
    COMPLETED   // Mentor đã xem
}
```

**2. Submission Entity (30 phút)**
```java
@Entity
@Table(name = "submissions")
class Submission {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Assignment assignment;
    
    @ManyToOne
    private User intern;
    
    private String content;        // Nội dung bài làm
    private String attachmentUrl;  // Link file (optional)
    private LocalDateTime submittedAt;
}
```

**3. Repository (15 phút)**
```java
interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    Page<Assignment> findByMentorId(Long mentorId, Pageable pageable);
}

interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByAssignmentIdAndInternId(Long assignmentId, Long internId);
}
```

**4. Service Layer (1.5 giờ)**
```java
@Service
class AssignmentService {
    // Create Assignment (Mentor only)
    public AssignmentResponse create(CreateAssignmentRequest request) {
        // 1. Validate mentor exists
        // 2. Create assignment
        // 3. Return response
    }
    
    // Get assignments by mentor
    public Page<AssignmentResponse> getByMentor(Long mentorId, int page, int size) {
        // Return paginated assignments
    }
    
    // Submit assignment (Intern only)
    public SubmissionResponse submit(Long assignmentId, SubmitRequest request) {
        // 1. Validate assignment exists
        // 2. Check not already submitted
        // 3. Create submission
        // 4. Update assignment status to SUBMITTED
        // 5. Return response
    }
    
    // Get submissions for assignment
    public List<SubmissionResponse> getSubmissions(Long assignmentId) {
        // Return all submissions for this assignment
    }
}
```

**5. Controller (1 giờ)**
```java
@RestController
@RequestMapping("/assignments")
class AssignmentController {
    
    @PostMapping
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseApi<AssignmentResponse> create(@RequestBody CreateAssignmentRequest request) {
        return ResponseApi.success(assignmentService.create(request));
    }
    
    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseApi<Page<AssignmentResponse>> getMyAssignments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        // Get from authenticated user
        Long mentorId = getCurrentUserId();
        return ResponseApi.success(assignmentService.getByMentor(mentorId, page, size));
    }
    
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('INTERN')")
    public ResponseApi<SubmissionResponse> submit(
        @PathVariable Long id,
        @RequestBody SubmitRequest request
    ) {
        return ResponseApi.success(assignmentService.submit(id, request));
    }
    
    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseApi<List<SubmissionResponse>> getSubmissions(@PathVariable Long id) {
        return ResponseApi.success(assignmentService.getSubmissions(id));
    }
}
```

**6. DTOs (30 phút)**
```java
record CreateAssignmentRequest(
    String title,
    String description,
    LocalDate deadline
) {}

record SubmitRequest(
    String content,
    String attachmentUrl  // optional
) {}

record AssignmentResponse(
    Long id,
    String title,
    String description,
    LocalDate deadline,
    AssignmentStatus status,
    String mentorName,
    int submissionCount
) {}

record SubmissionResponse(
    Long id,
    Long assignmentId,
    String internName,
    String content,
    String attachmentUrl,
    LocalDateTime submittedAt
) {}
```

**7. Build & Docker (1 giờ)**
- Thêm vào settings.gradle
- Build module
- Dockerfile
- docker-compose.yml

**Bỏ qua trong MVP:**
- ❌ Review/Feedback từ mentor
- ❌ Score/Rating
- ❌ Resubmit
- ❌ Assignment templates
- ❌ Batch assign to multiple interns
- ❌ Notifications

**Kết quả:** 
- Mentor có thể tạo assignment
- Intern có thể submit
- Mentor xem được submissions

---

## Phase 3: Frontend Integration (2-3 giờ)

### Mục tiêu
React webapp gọi được API qua Gateway và demo 1 workflow hoàn chỉnh.

### Công việc

**1. Update API Base URL (15 phút)**
```dart
// lib/config/api_config.dart
class ApiConfig {
  static const String baseUrl = 'http://localhost:8080';  // Gateway URL
  
  // Không cần biết port của từng service nữa
}
```

**2. Test Authentication Flow (30 phút)**
```dart
// Test login
final response = await http.post(
  Uri.parse('$baseUrl/auth/login'),
  body: json.encode({
    'email': 'mentor@imes.com',
    'password': 'mentor123',
  }),
);

// Lưu token
final token = json.decode(response.body)['data']['token'];
```

**3. Test Assignment Workflow (1 giờ)**

**Scenario: Mentor tạo task cho intern**
```dart
// 1. Mentor login
loginAsMentor();

// 2. Create assignment
POST /assignments
{
  "title": "Học Spring Boot",
  "description": "Hoàn thành tutorial Spring Boot",
  "deadline": "2026-03-15"
}

// 3. View created assignments
GET /assignments/my-assignments

// 4. Logout mentor
```

**Scenario: Intern submit assignment**
```dart
// 1. Intern login
loginAsIntern();

// 2. View assignments (get from somewhere - could be hardcoded for demo)
GET /assignments/1

// 3. Submit
POST /assignments/1/submit
{
  "content": "Đã hoàn thành bài tập...",
  "attachmentUrl": "https://drive.google.com/..."
}

// 4. Success message
```

**4. Fix Bugs (30-60 phút)**
- CORS issues
- Authentication header
- Response parsing

**Bỏ qua trong MVP:**
- ❌ UI đẹp
- ❌ Form validation phức tạp
- ❌ Loading states elaborate
- ❌ Error handling chi tiết
- ❌ File upload (dùng hardcoded URL)

**Kết quả:** 
Demo được workflow từ đầu đến cuối:
Login → Create Task → Submit → View Submission

---

## Demo Script End-User

### Chuẩn bị
1. Start all services: `docker-compose up -d`
2. Open React webapp (IMES-webapp)
3. Prepare 2 accounts: mentor@imes.com và intern1@imes.com

### Demo Flow (5-7 phút)

**Scene 1: Mentor tạo nhiệm vụ (2 phút)**
1. Login as mentor@imes.com
2. Navigate to "Assignments" screen
3. Click "Create Assignment"
4. Fill form:
   - Title: "Học Spring Boot Microservices"
   - Description: "Hoàn thành tutorial và tạo demo project"
   - Deadline: 15/03/2026
5. Click "Create"
6. Show assignment list (1 assignment created)
7. Logout

**Scene 2: Intern nhận và làm bài (2 phút)**
1. Login as intern1@imes.com
2. Navigate to "My Assignments" 
3. See "Học Spring Boot Microservices" (status: OPEN)
4. Click to view details
5. Click "Submit Assignment"
6. Fill:
   - Content: "Đã hoàn thành tutorial, code tại: https://github.com/..."
   - Attachment: Link Google Drive
7. Click "Submit"
8. Show success message
9. Status changed to SUBMITTED
10. Logout

**Scene 3: Mentor xem bài nộp (1-2 phút)**
1. Login back as mentor@imes.com
2. Navigate to "My Assignments"
3. See assignment status changed to SUBMITTED
4. Click to view submissions
5. See intern's submission with content
6. (Optional) Click "Mark as Completed"

**Key Message:**
> "Hệ thống demo workflow cơ bản: Mentor giao task → Intern làm và nộp → Mentor xem kết quả. Đây là nền tảng, các tính năng nâng cao sẽ bổ sung sau."

---

## Những gì BỎ QUA trong MVP

### ❌ Không làm (giành 40% còn lại)

1. **Evaluation System** (0%)
   - Mentor đánh giá
   - Self-evaluation
   - Aggregation

2. **Advanced Assignment Features** (0%)
   - Review & feedback
   - Score/rating
   - Resubmit
   - Templates

3. **Notification System** (0%)
   - Email notifications
   - Push notifications

4. **Advanced Attendance** (0%)
   - Leave approval workflow
   - Monthly reports
   - Statistics dashboard

5. **Learning Materials** (0%)
   - Upload/download materials
   - Progress tracking

6. **Advanced Gateway** (0%)
   - Rate limiting
   - Circuit breaker
   - Advanced security

7. **UI Polish** (0%)
   - Beautiful UI
   - Animations
   - Advanced UX

---

## Timeline Chi Tiết

### Day 1 (8 giờ)
- **Morning (4h):** API Gateway
  - 09:00-09:30: Setup module
  - 09:30-11:00: Config routes
  - 11:00-12:00: Build & test
  - 12:00-13:00: Docker integration

- **Afternoon (4h):** Assignment Service (Part 1)
  - 14:00-14:30: Entities
  - 14:30-15:00: Repositories
  - 15:00-16:30: Service layer
  - 16:30-18:00: Controller & DTOs

### Day 2 (6-8 giờ)
- **Morning (3-4h):** Assignment Service (Part 2)
  - 09:00-10:00: Build & test endpoints
  - 10:00-11:00: Docker integration
  - 11:00-12:00: Fix bugs
  - 12:00-13:00: Add sample data

- **Afternoon (3-4h):** Frontend Integration
  - 14:00-14:30: Update API config
  - 14:30-15:30: Test auth flow
  - 15:30-17:00: Test assignment workflow
  - 17:00-18:00: Fix bugs & polish

### Total: 14-16 giờ = 1.5-2 ngày

---

## Acceptance Criteria

### ✅ MVP Ready khi:

1. **Gateway Working**
   - [ ] Frontend gọi được http://localhost:8080/auth/login
   - [ ] CORS configured đúng
   - [ ] Routes đến tất cả services

2. **Assignment Service Working**
   - [ ] Mentor tạo được assignment
   - [ ] Assignment hiện trong list
   - [ ] Intern submit được bài
   - [ ] Mentor xem được submission

3. **End-to-End Demo**
   - [ ] Login as mentor → tạo task → logout
   - [ ] Login as intern → submit → logout  
   - [ ] Login as mentor → xem submission
   - [ ] Toàn bộ flow < 5 phút

4. **Docker Deployment**
   - [ ] 6/9 services running (5 current + assignment)
   - [ ] Gateway on port 8080
   - [ ] All healthy

---

## Risk Mitigation

### Nếu thiếu thời gian:

**Plan B: Bỏ qua Assignment Service**
- Demo với data có sẵn (hardcoded)
- Focus vào Gateway + Frontend
- Show architecture & infrastructure

**Plan C: API-only demo**
- Không dùng frontend
- Demo bằng Postman/curl
- Focus technical capabilities

---

## Success Metrics

### 60% Completion = MVP Ready

| Component | Current | Target | Priority |
|-----------|---------|--------|----------|
| Infrastructure | 60% | 60% | ✅ Keep |
| Gateway | 100% | 100% | ✅ Done |
| Assignment | 100% | 100% | ✅ Done |
| Auth | 75% | 80% | 🟡 Nice |
| Users | 60% | 60% | ✅ Keep |
| Interns | 60% | 60% | ✅ Keep |
| Attendance | 40% | 40% | ✅ Keep |
| Frontend (React) | 70% | 100% | 🟡 In progress |

**Overall: chuyển từ MVP build sang stabilization + integration**

---

## Next Steps After MVP

Once 60% MVP achieved, optionally add:

1. **Assignment Feedback** (+5%) - 2-3 hours
2. **Better UI** (+5%) - 3-4 hours
3. **Attendance Complete** (+5%) - 2-3 hours
4. **Evaluation System** (+15%) - 8-10 hours
5. **Notifications** (+5%) - 4-6 hours

Total to 80%: Additional 15-20 hours

---

## Summary

**Current:** 40% - Technical demo only  
**MVP Goal:** 60% - End-user demo ready  
**Effort:** 14-16 hours (1.5-2 days)  
**Focus:** Gateway + Basic Assignment + Frontend Integration  
**Result:** Demo full workflow Mentor → Intern

**Start with:** API Gateway (highest priority)
