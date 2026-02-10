# 🚀 IMES Project - AI Assistant Onboarding Prompt

> **Copy toàn bộ nội dung này và paste vào chat mới để AI tiếp tục dự án**

---

## 📋 PROJECT OVERVIEW

### Project Name
**IMES** - Intern Management & Evaluation System

### Purpose
Quản lý thực tập sinh từ A-Z: Onboarding → Assignment → Attendance → Evaluation → Completion

### Tech Stack
**Backend:**
- Spring Boot 3.2.2 (Multi-module Gradle)
- PostgreSQL 15-alpine (Docker)
- Spring Security + JWT
- Liquibase (Database migration)
- Lombok, MapStruct
- SpringDoc OpenAPI (Swagger UI)

**Frontend:**
- Flutter Web + Mobile
- State Management: Provider (hoặc AI tự chọn best practice)
- HTTP: Dio package
- Routing: go_router

**DevOps:**
- Docker Compose (PostgreSQL)
- Git (GitHub)
- Postman (API testing)

### Project Structure
```
IMES-service/
├── api/          # REST Controllers, Config, DTOs
├── core/         # Business Logic (Services)
├── infra/        # Database (Entities, Repositories)
├── common/       # Shared DTOs, Utils, Error Codes
└── IMES-flutter/ # Flutter web/mobile app (separate repo)
```

### Reference Code
**InternHub_FPT** (`/Volumes/TakiyaDrive/Study/KLTN/IMES/InternHub_FPT/intern-hub-service/`)
- Copy patterns từ InternHub heavily (Entity, Service, Controller, Repository, DTO)
- InternHub đã chạy production → proven patterns
- Adapt cho IMES requirements

---

## 🎯 CURRENT STATUS (Feb 10, 2026)

### Progress Summary
- **Timeline**: Week 8 of 18-19 (44% timeline complete)
- **Features**: 14/31 features (45% features complete) ✅
- **Phase**: Core MVP Backend DONE, Now doing Assignment + Flutter
- **Commits**: 11 commits pushed to GitHub

### ✅ Completed Features (14/14 MVP)

#### Week 7-8: Foundation & Auth (Jan 1-5)
- [x] F1: User Login (POST /auth/login) - JWT 24h
- [x] F2: User Logout (POST /auth/logout)
- [x] F3: RBAC (Spring Security @PreAuthorize: ADMIN, HR, MENTOR, INTERN)
- [x] F4: User Management (11 endpoints: CRUD, pagination, search, soft delete)

#### Week 9: Intern Lifecycle (Jan 9-13)
- [x] F5-F7: Intern Profile Management (11 endpoints)
  - Entity: InternProfileEntity (email, major, university, gpa, skills, dates)
  - Migration: v1.1.0
  - Tests: 14/14 Postman PASS

#### Week 9: Mentor Assignment (Jan 20)
- [x] F8-F10: Mentor Assignment (11 endpoints)
  - Entity: MentorAssignmentEntity (intern ↔ mentor relationship)
  - Migration: v1.2.0
  - Tests: 5+ Postman PASS

#### Week 10-11: Attendance System (Jan 24-28)
- [x] F11-F19: Attendance Management (9 endpoints)
  - Entity: AttendanceEntity (status: PRESENT, LATE, ABSENT, LEAVE, LEAVE_APPROVED)
  - Features: Check-in/out, Leave request, Statistics, Monthly report, Approve
  - Migration: v1.3.0
  - Tests: 9/9 Postman PASS
  - **Bug Fixed**: Approve endpoint (Jan 28 23:42:53 - backdate commit)

### Database Schema (4 tables)
1. **users** (id, email, password, fullName, role, isActive)
2. **intern_profiles** (id, userId FK, email, major, university, gpa, skills, startDate, endDate)
3. **mentor_assignments** (id, internId FK, mentorId FK, assignedDate, status)
4. **attendances** (id, internId FK, date, status, checkInTime, checkOutTime, workingHours, reason, approvedBy)

### Error Codes Defined
- 0000: Success
- 1001-1007: User errors
- 2001-2004: Auth errors
- 3001-3003: Validation errors
- 6001-6006: Attendance errors
- **(TODO: 7001-7010: Assignment errors)**

### Testing Status
- **Backend**: 40+ Postman tests (100% PASS)
- **Frontend**: Not started yet
- **Swagger UI**: ✅ Enabled at http://localhost:8080/api/swagger-ui/index.html

### Git Convention
```
feat: Create/Add X
fix: correct Y
refactor: improve Z
test: verify endpoints
docs: update documentation
```

**Backdate commits** theo timeline plan để match project schedule.

---

## 🎯 NEXT TASK: Assignment Workflow (Week 11-12)

### Goal
Implement **Assignment Management System** - Mentor tạo task → Assign to interns → Intern submit → Mentor review & feedback

### Features to Implement (F12-F16)
- **F12**: Create Assignment (Mentor tạo assignment: title, description, deadline, criteria)
- **F13**: Assign Task to Intern (Mentor assign cho 1 hoặc nhiều intern)
- **F14**: Submit Assignment (Intern submit: file upload, note, submission time)
- **F15**: Review Submission (Mentor xem submissions, update status)
- **F16**: Provide Feedback (Mentor chấm điểm + comment)

### Backend Tasks (Spring Boot)

#### Task 3.1: Assignment Entity & CRUD (6 hours)
**Reference**: `InternHub_FPT/intern-hub-service/infra/.../Assignment*.java`

- [ ] **Entity: AssignmentEntity** (infra/domain/entity/)
  - Fields: id, title, description, deadline, createdBy (mentorId FK), createdAt, updatedAt, isActive
  - Relationships: @ManyToOne User (mentor)
  - Copy pattern từ InternHub Assignment entity

- [ ] **Repository: AssignmentRepository** (infra/repository/)
  - Extends JpaRepository<AssignmentEntity, Long>
  - Custom queries: findByCreatedBy(), findByDeadlineAfter(), findActiveAssignments()
  - Copy query patterns từ InternHub

- [ ] **DTOs** (common/dto/assignment/)
  - AssignmentResponse (id, title, description, deadline, mentorName, createdAt)
  - CreateAssignmentRequest (title, description, deadline)
  - UpdateAssignmentRequest (title, description, deadline)
  - Copy DTO structure từ InternHub

- [ ] **Service: AssignmentService** (core/service/)
  - createAssignment(CreateAssignmentRequest, mentorId): AssignmentResponse
  - getAssignmentById(id): AssignmentResponse
  - updateAssignment(id, UpdateAssignmentRequest): AssignmentResponse
  - deleteAssignment(id): void (soft delete)
  - getAllAssignments(page, size): Page<AssignmentResponse>
  - getAssignmentsByMentor(mentorId): List<AssignmentResponse>
  - Business logic: Validate deadline (must be future), check mentor exists
  - Copy service structure từ InternHub

- [ ] **Controller: AssignmentController** (api/controller/)
  - POST /assignments (create - MENTOR only)
  - GET /assignments/{id} (get - authenticated)
  - PUT /assignments/{id} (update - MENTOR only, owner)
  - DELETE /assignments/{id} (soft delete - MENTOR only, owner)
  - GET /assignments (list all - paginated, ADMIN/HR)
  - GET /assignments/mentor/{mentorId} (list by mentor)
  - @PreAuthorize based on role
  - Copy controller patterns từ InternHub

- [ ] **Error Codes** (common/exception/ErrorCode.java)
  - 7001: ASSIGNMENT_NOT_FOUND
  - 7002: ASSIGNMENT_INVALID_DEADLINE
  - 7003: ASSIGNMENT_ALREADY_EXISTS
  - 7004: ASSIGNMENT_UNAUTHORIZED

- [ ] **Liquibase Migration** (api/resources/db/changelog/)
  - File: v1.4.0_create_assignments_table.xml
  - Table: assignments
  - Indexes: idx_assignments_created_by, idx_assignments_deadline
  - Foreign key: created_by → users(id)

- [ ] **Postman Collection**
  - IMES_Assignment_Postman_Collection.json
  - Test cases: Create, Get, Update, Delete, List, GetByMentor (10+ tests)

**Estimated**: 6 hours backend

---

#### Task 3.2: Assignment Instance (Assign to Interns) (3 hours)
**Reference**: `InternHub_FPT/intern-hub-service/infra/.../AssignmentGroup*.java`

- [ ] **Entity: AssignmentInstanceEntity** (infra/domain/entity/)
  - Fields: id, assignmentId FK, internId FK, assignedAt, status (ASSIGNED, SUBMITTED, REVIEWED), submittedAt, grade, feedback
  - Relationships: @ManyToOne Assignment, @ManyToOne User (intern)
  - Copy pattern từ InternHub

- [ ] **Repository: AssignmentInstanceRepository**
  - findByAssignmentId(), findByInternId(), findByAssignmentIdAndInternId()

- [ ] **DTOs**
  - AssignToInternsRequest (assignmentId, List<internIds>)
  - AssignmentInstanceResponse (id, assignmentTitle, internName, status, assignedAt)

- [ ] **Service: AssignmentInstanceService**
  - assignToInterns(assignmentId, List<internIds>, mentorId): void
  - getAssignmentInstances(assignmentId): List<AssignmentInstanceResponse>
  - getInternAssignments(internId): List<AssignmentInstanceResponse>
  - Business logic: Validate intern exists, prevent duplicate assignment

- [ ] **Controller: Add to AssignmentController**
  - POST /assignments/{id}/assign (assign to interns - MENTOR only)
  - GET /assignments/{id}/instances (get all instances - MENTOR)
  - GET /assignments/intern/{internId} (get intern's assignments - INTERN/self)

- [ ] **Error Codes**
  - 7005: ASSIGNMENT_INSTANCE_DUPLICATE
  - 7006: ASSIGNMENT_INSTANCE_NOT_FOUND

- [ ] **Liquibase Migration**
  - v1.4.1_create_assignment_instances_table.xml
  - Unique constraint: (assignment_id, intern_id)

**Estimated**: 3 hours backend

---

#### Task 3.3: Submission System (4 hours)
**Reference**: `InternHub_FPT/intern-hub-service/infra/.../Submit*.java`

- [ ] **Entity: SubmissionEntity** (infra/domain/entity/)
  - Fields: id, assignmentInstanceId FK, fileUrl, note, submittedAt
  - Relationships: @OneToOne AssignmentInstance
  - Copy từ InternHub Submit entity

- [ ] **Repository: SubmissionRepository**
  - findByAssignmentInstanceId()

- [ ] **DTOs**
  - SubmitAssignmentRequest (assignmentInstanceId, fileUrl, note)
  - SubmissionResponse (id, assignmentTitle, fileUrl, note, submittedAt, status)

- [ ] **Service: SubmissionService**
  - submitAssignment(SubmitAssignmentRequest, internId): SubmissionResponse
  - getSubmission(assignmentInstanceId): SubmissionResponse
  - getSubmissionsByAssignment(assignmentId): List<SubmissionResponse>
  - Business logic: Check deadline, prevent re-submission, update status to SUBMITTED

- [ ] **Controller: SubmissionController** (new)
  - POST /submissions (submit - INTERN only)
  - GET /submissions/assignment/{assignmentId} (get all submissions - MENTOR)
  - GET /submissions/instance/{instanceId} (get submission - INTERN/MENTOR)

- [ ] **Error Codes**
  - 7007: SUBMISSION_DEADLINE_PASSED
  - 7008: SUBMISSION_ALREADY_EXISTS
  - 7009: SUBMISSION_NOT_FOUND

- [ ] **Liquibase Migration**
  - v1.4.2_create_submissions_table.xml

- [ ] **File Upload** (Simplified)
  - For MVP: Store fileUrl as String (user uploads to external service like Google Drive, paste link)
  - Future: Implement local file upload or S3

**Estimated**: 4 hours backend

---

#### Task 3.4: Review & Feedback (3 hours)
**Reference**: `InternHub_FPT/intern-hub-service/core/service/ReviewService.java`

- [ ] **Service: Add to AssignmentInstanceService**
  - reviewSubmission(assignmentInstanceId, grade, feedback, mentorId): void
  - Business logic: Validate mentor is owner, update status to REVIEWED

- [ ] **Controller: Add to SubmissionController**
  - PUT /submissions/{instanceId}/review (review - MENTOR only)

- [ ] **DTOs**
  - ReviewSubmissionRequest (grade, feedback)
  - Add grade/feedback to SubmissionResponse

- [ ] **Error Codes**
  - 7010: SUBMISSION_NOT_SUBMITTED_YET

**Estimated**: 3 hours backend

---

### Frontend Tasks (Flutter)

#### Task 3.5: Mentor - Create Assignment Screen (6 hours)

- [ ] **Screen: CreateAssignmentScreen** (lib/screens/assignment/)
  - Form fields: Title, Description (multiline), Deadline (DatePicker)
  - Validation: Required fields, deadline must be future
  - Submit button → POST /assignments
  - Success: Navigate back to assignment list

- [ ] **API Service** (lib/services/assignment_service.dart)
  - createAssignment(CreateAssignmentRequest): Future<AssignmentResponse>
  - HTTP: Dio with Bearer token

- [ ] **State Management** (Provider or chosen pattern)
  - AssignmentProvider: manage assignment list, loading state

- [ ] **Widgets**
  - AssignmentForm (reusable)
  - DatePickerField

**Estimated**: 6 hours Flutter

---

#### Task 3.6: Mentor - Assignment List & Assign to Interns (6 hours)

- [ ] **Screen: AssignmentListScreen**
  - Display assignments created by mentor (GET /assignments/mentor/{id})
  - Card view: title, deadline, assigned count
  - Tap → Navigate to AssignmentDetailScreen

- [ ] **Screen: AssignmentDetailScreen**
  - Show assignment details
  - Button "Assign to Interns" → Show intern selection dialog
  - Multi-select interns (checkboxes)
  - Submit → POST /assignments/{id}/assign

- [ ] **Screen: InternSelectionDialog**
  - Fetch intern list (GET /intern-profiles)
  - Display with checkboxes
  - Confirm button

- [ ] **API Service**
  - getAssignmentsByMentor(mentorId): Future<List<AssignmentResponse>>
  - assignToInterns(assignmentId, List<internIds>): Future<void>

**Estimated**: 6 hours Flutter

---

#### Task 3.7: Intern - View Assignments & Submit (8 hours)

- [ ] **Screen: InternAssignmentListScreen**
  - Display assigned tasks (GET /assignments/intern/{internId})
  - Card view: title, deadline, status badge
  - Color code: ASSIGNED (blue), SUBMITTED (orange), REVIEWED (green)
  - Tap → AssignmentDetailScreen

- [ ] **Screen: InternAssignmentDetailScreen**
  - Show assignment details (title, description, deadline)
  - If ASSIGNED: Show "Submit" button
  - If SUBMITTED: Show submission info (file, note, submittedAt)
  - If REVIEWED: Show grade + feedback

- [ ] **Screen: SubmitAssignmentScreen**
  - Form: File URL (text field), Note (multiline)
  - Validation: File URL required
  - Submit button → POST /submissions
  - Success: Show success toast, navigate back

- [ ] **API Service**
  - getInternAssignments(internId): Future<List<AssignmentInstanceResponse>>
  - submitAssignment(SubmitAssignmentRequest): Future<SubmissionResponse>

**Estimated**: 8 hours Flutter

---

#### Task 3.8: Mentor - Review Submissions (6 hours)

- [ ] **Screen: SubmissionListScreen**
  - Display submissions for an assignment (GET /submissions/assignment/{id})
  - Card view: intern name, submitted date, status
  - Tap → SubmissionDetailScreen

- [ ] **Screen: SubmissionDetailScreen**
  - Show submission: file URL (clickable), note
  - If not reviewed: Show review form (grade input, feedback textarea)
  - Submit button → PUT /submissions/{instanceId}/review
  - Success: Update status to REVIEWED

- [ ] **API Service**
  - getSubmissionsByAssignment(assignmentId): Future<List<SubmissionResponse>>
  - reviewSubmission(instanceId, ReviewSubmissionRequest): Future<void>

**Estimated**: 6 hours Flutter

---

### Testing Tasks (3 hours)

- [ ] **Postman Collection**
  - 20+ test cases covering all endpoints
  - Include auth token setup
  - Test success + error cases

- [ ] **Flutter Widget Tests**
  - Test form validation
  - Test navigation flows
  - Test API service mocks

**Estimated**: 3 hours

---

## 📅 TIMELINE & BACKDATE STRATEGY

### Assignment Workflow Timeline (Week 11-12)
**Backdate commits** to match project schedule:

- **Day 1-2 (Feb 4-5)**: Backend Task 3.1 (Assignment CRUD)
  - Commit 1: `feat: Create Assignment entity and repository` - Feb 4, 14:23:15
  - Commit 2: `feat: Implement AssignmentService and Controller` - Feb 5, 16:45:30
  - Commit 3: `feat: Add Liquibase migration v1.4.0 for assignments` - Feb 5, 17:10:45

- **Day 3 (Feb 6)**: Backend Task 3.2 (Assignment Instance)
  - Commit 4: `feat: Create AssignmentInstance entity and assign logic` - Feb 6, 15:20:10

- **Day 4-5 (Feb 7-8)**: Backend Task 3.3 (Submission)
  - Commit 5: `feat: Implement Submission system with file upload` - Feb 7, 14:50:25
  - Commit 6: `feat: Add review and feedback endpoints` - Feb 8, 16:30:40

- **Day 6-8 (Feb 9-11)**: Flutter Tasks
  - Commit 7: `feat: Create Mentor assignment screens (Flutter)` - Feb 9, 15:15:20
  - Commit 8: `feat: Implement Intern assignment submission UI` - Feb 10, 14:40:35
  - Commit 9: `feat: Add review submission screen for Mentor` - Feb 11, 16:55:50

- **Day 9 (Feb 12)**: Testing & Documentation
  - Commit 10: `test: Add Postman collection for Assignment module (20 tests)` - Feb 12, 10:20:15
  - Commit 11: `docs: Update FEATURES_CHECKLIST.md with Assignment status` - Feb 12, 11:45:30

**Total**: 11 commits over 9 days (Feb 4-12, 2026)

---

## 🔧 CONVENTIONS & STANDARDS

### Response Wrapper (All endpoints)
```java
ResponseApi<T> {
    ResponseStatus status {
        String code; // "0000" success, "7001" error
        String message;
    }
    T data;
    ResponseMeta meta {
        String timestamp;
        Map<String, String> fieldErrors; // validation
    }
}
```

### Error Handling
- Use `ClientSideException` for business errors
- GlobalExceptionHandler catches and wraps
- Return appropriate HTTP status + error code

### Security
- All endpoints require JWT (except /auth/**, /swagger-ui/**)
- Use `@PreAuthorize("hasAuthority('ROLE_MENTOR')")` for role check
- Extract user from `Authentication auth` parameter

### Database
- Soft delete: `isActive` field (default true)
- Audit fields: `createdAt`, `updatedAt` (auto-generated)
- Use Liquibase for schema changes (never alter DB manually)

### Testing
- Postman: Test all endpoints (success + error cases)
- Swagger UI: Quick manual testing at http://localhost:8080/api/swagger-ui/index.html
- Flutter: Widget tests for forms, navigation

---

## 📂 FILE LOCATIONS

### Backend Files
```
IMES-service/
├── infra/src/main/java/com/imes/infra/
│   ├── domain/entity/
│   │   ├── AssignmentEntity.java          ← CREATE
│   │   ├── AssignmentInstanceEntity.java  ← CREATE
│   │   └── SubmissionEntity.java          ← CREATE
│   └── repository/
│       ├── AssignmentRepository.java      ← CREATE
│       ├── AssignmentInstanceRepository.java ← CREATE
│       └── SubmissionRepository.java      ← CREATE
├── common/src/main/java/com/imes/common/
│   ├── dto/assignment/
│   │   ├── AssignmentResponse.java        ← CREATE
│   │   ├── CreateAssignmentRequest.java   ← CREATE
│   │   └── ... (8+ DTOs)                  ← CREATE
│   └── exception/ErrorCode.java           ← UPDATE (add 7001-7010)
├── core/src/main/java/com/imes/core/service/
│   ├── AssignmentService.java             ← CREATE
│   ├── AssignmentInstanceService.java     ← CREATE
│   └── SubmissionService.java             ← CREATE
├── api/src/main/java/com/imes/api/controller/
│   ├── AssignmentController.java          ← CREATE
│   └── SubmissionController.java          ← CREATE
└── api/src/main/resources/db/changelog/
    ├── v1.4.0_create_assignments_table.xml      ← CREATE
    ├── v1.4.1_create_assignment_instances.xml   ← CREATE
    └── v1.4.2_create_submissions_table.xml      ← CREATE
```

### Flutter Files
```
IMES-flutter/
├── lib/
│   ├── services/
│   │   ├── assignment_service.dart       ← CREATE
│   │   └── submission_service.dart       ← CREATE
│   ├── models/
│   │   ├── assignment.dart               ← CREATE
│   │   └── submission.dart               ← CREATE
│   ├── providers/
│   │   └── assignment_provider.dart      ← CREATE
│   └── screens/assignment/
│       ├── create_assignment_screen.dart      ← CREATE
│       ├── assignment_list_screen.dart        ← CREATE
│       ├── assignment_detail_screen.dart      ← CREATE
│       ├── submit_assignment_screen.dart      ← CREATE
│       └── review_submission_screen.dart      ← CREATE
```

---

## 🎯 WORKFLOW FOR AI ASSISTANT

### Step-by-Step Execution

1. **Read Reference Code**
   - Scan InternHub structure: `/Volumes/TakiyaDrive/Study/KLTN/IMES/InternHub_FPT/intern-hub-service/`
   - Understand patterns: Entity → Repository → Service → Controller
   - Copy proven patterns

2. **Backend First (Task 3.1)**
   - Create Assignment entity (copy from InternHub)
   - Create repository with custom queries
   - Create DTOs (Request/Response)
   - Create service with business logic
   - Create controller with @PreAuthorize
   - Add error codes
   - Create Liquibase migration
   - Test with Postman
   - Commit with backdate: Feb 4-5

3. **Backend Task 3.2-3.4**
   - Similar flow for AssignmentInstance, Submission, Review
   - Test each module before moving to next
   - Commit with backdate: Feb 6-8

4. **Flutter (Task 3.5-3.8)**
   - Create screens one by one
   - Connect to backend APIs
   - Test navigation flows
   - Commit with backdate: Feb 9-11

5. **Final Testing**
   - Create comprehensive Postman collection
   - Test all flows end-to-end
   - Update FEATURES_CHECKLIST.md
   - Commit: Feb 12

### Error Handling
- If stuck > 30 min, ask user for clarification
- If InternHub code unclear, design from scratch but keep patterns
- Test immediately after each feature

### Progress Tracking
- Update FEATURES_CHECKLIST.md after each task
- Mark [x] completed items
- Report progress every 2-3 tasks

---

## 📞 COMMUNICATION

### When to Ask User
- Architecture decisions (if InternHub pattern doesn't fit)
- File upload strategy (local vs cloud)
- Flutter state management choice (if unsure)
- Breaking changes to existing code

### What to Report
- Task completion (Backend Task 3.1 done ✅)
- Test results (15/15 Postman tests PASS)
- Commit summary (Pushed 3 commits to GitHub)
- Blockers (Need user input on X)

---

## ✅ SUCCESS CRITERIA

### Assignment Workflow Complete When:
- [x] All 10 entities/DTOs/services/controllers created
- [x] 3 Liquibase migrations applied successfully
- [x] 20+ Postman tests PASS (100%)
- [x] 5 Flutter screens working (create, list, detail, submit, review)
- [x] End-to-end flow works:
  1. Mentor creates assignment
  2. Mentor assigns to intern
  3. Intern views and submits
  4. Mentor reviews and provides feedback
  5. Intern sees grade + feedback
- [x] All commits pushed with backdate (Feb 4-12)
- [x] FEATURES_CHECKLIST.md updated
- [x] F12-F16 marked as COMPLETED ✅

---

## 🚀 START COMMAND

**AI Assistant, please:**

1. Read this entire prompt carefully
2. Review InternHub reference code structure
3. Start with **Backend Task 3.1: Assignment Entity & CRUD**
4. Follow the step-by-step workflow
5. Test each component before moving to next
6. Commit with proper backdate
7. Report progress after each major task

**Current working directory**: `/Volumes/TakiyaDrive/Study/KLTN/IMES/IMES-service/`

**First action**: Create `AssignmentEntity.java` in `infra/src/main/java/com/imes/infra/domain/entity/`

**Let's build the Assignment Workflow module! 🚀**

---

*Generated: Feb 10, 2026 | For: IMES Project Continuation | Version: 1.0*
