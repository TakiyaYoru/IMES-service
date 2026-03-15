# IMES - Tài Liệu Tính Năng Hệ Thống

**Phiên bản:** 1.0.0  
**Ngày cập nhật:** 26/02/2026  
**Trạng thái:** Production Ready

---

## 📋 Mục Lục

1. [Xác Thực & Phân Quyền](#1-xác-thực--phân-quyền)
2. [Quản Lý Người Dùng](#2-quản-lý-người-dùng)
3. [Quản Lý Intern](#3-quản-lý-intern)
4. [Quản Lý Phòng Ban](#4-quản-lý-phòng-ban)
5. [Quản Lý Bài Tập](#5-quản-lý-bài-tập)
6. [Quản Lý Chấm Công](#6-quản-lý-chấm-công)
7. [Phân Công Mentor](#7-phân-công-mentor)

---

## 1. Xác Thực & Phân Quyền

### 1.1. Đăng Nhập (Login)

#### Mô tả chi tiết
Cho phép người dùng đăng nhập vào hệ thống bằng email và mật khẩu. Hệ thống sẽ xác thực thông tin và trả về JWT token để sử dụng cho các request tiếp theo.

#### Vai trò
- Tất cả người dùng (HR, Mentor, Intern)

#### Luồng xử lý
```
[Client] → POST /auth/login
  ↓
[Auth Service] Kiểm tra email/password
  ↓
[Database] Truy vấn bảng users
  ↓
[Auth Service] Tạo JWT token
  ↓
[Client] ← Response: {token, email, fullName, role}
```

#### API Endpoint
- **URL:** `POST /auth/login`
- **Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **Response (Success):**
```json
{
  "status": {
    "code": "0000",
    "message": "Success"
  },
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "MENTOR"
  }
}
```

#### Validation Rules
- Email: Required, phải đúng format email
- Password: Required, tối thiểu 6 ký tự
- Account phải active (isActive = true)

#### Error Codes
- `1001`: Email không tồn tại
- `1002`: Mật khẩu không đúng
- `1003`: Tài khoản bị khóa

---

### 1.2. Đăng Xuất (Logout)

#### Mô tả chi tiết
Cho phép người dùng đăng xuất khỏi hệ thống. JWT token sẽ bị vô hiệu hóa (nếu có blacklist).

#### Vai trò
- Tất cả người dùng đã đăng nhập

#### Luồng xử lý
```
[Client] → POST /auth/logout
  ↓
[Auth Service] Xóa token khỏi session
  ↓
[Client] ← Response: Success
```

#### API Endpoint
- **URL:** `POST /auth/logout`
- **Headers:** `Authorization: Bearer {token}`
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Logout successful"
  }
}
```

---

## 2. Quản Lý Người Dùng

### 2.1. Tạo Người Dùng Mới (Create User)

#### Mô tả chi tiết
HR có thể tạo tài khoản mới cho các vai trò: HR, Mentor, hoặc Intern. Mật khẩu sẽ được mã hóa BCrypt trước khi lưu vào database.

#### Vai trò
- **HR only**

#### Luồng xử lý
```
[HR Client] → POST /users
  ↓
[Gateway] → Route to User Service
  ↓
[User Service] Validate data
  ↓
[User Service] Hash password (BCrypt)
  ↓
[Database] Insert into users table
  ↓
[User Service] Return user info
  ↓
[HR Client] ← Response: Created user
```

#### API Endpoint
- **URL:** `POST /users`
- **Headers:** 
  - `Authorization: Bearer {token}`
  - `X-User-Role: HR`
- **Body:**
```json
{
  "name": "John Mentor",
  "email": "john@imes.com",
  "password": "secure123",
  "role": "MENTOR"
}
```
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Success"
  },
  "data": {
    "id": 5,
    "name": "John Mentor",
    "email": "john@imes.com",
    "role": "MENTOR",
    "isActive": true,
    "createdAt": "2026-02-26T10:30:00"
  }
}
```

#### Validation Rules
- Name: Required, 2-100 ký tự
- Email: Required, unique, format hợp lệ
- Password: Required, minimum 6 ký tự
- Role: Required, phải là HR, MENTOR, hoặc INTERN

#### Business Rules
- Email không được trùng trong hệ thống
- Chỉ HR mới có quyền tạo user
- Mặc định tài khoản được tạo sẽ active (isActive = true)

---

### 2.2. Xem Danh Sách Người Dùng (List Users)

#### Mô tả chi tiết
Lấy danh sách tất cả người dùng trong hệ thống với phân trang. Hỗ trợ filter theo role và search theo name/email.

#### Vai trò
- **HR only**

#### Luồng xử lý
```
[HR Client] → GET /users?page=0&size=20&role=MENTOR
  ↓
[Gateway] → User Service
  ↓
[User Service] Query database với pagination
  ↓
[Database] SELECT * FROM users WHERE...
  ↓
[User Service] Format response
  ↓
[HR Client] ← Response: Paginated list
```

#### API Endpoint
- **URL:** `GET /users`
- **Query Parameters:**
  - `page`: số trang (default: 0)
  - `size`: số records per page (default: 20)
  - `role`: filter theo role (optional)
  - `search`: tìm kiếm theo name/email (optional)
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Success"
  },
  "data": {
    "content": [
      {
        "id": 1,
        "name": "HR Manager",
        "email": "hr@imes.com",
        "role": "HR",
        "isActive": true
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

---

### 2.3. Xem Chi Tiết Người Dùng (Get User by ID)

#### Mô tả chi tiết
Lấy thông tin chi tiết của 1 người dùng theo ID.

#### Vai trò
- **HR, hoặc chính user đó**

#### API Endpoint
- **URL:** `GET /users/{id}`
- **Response:** User object với đầy đủ thông tin

---

### 2.4. Cập Nhật Người Dùng (Update User)

#### Mô tả chi tiết
Cập nhật thông tin người dùng (name, email, role, isActive). Không cho phép update password qua endpoint này.

#### Vai trò
- **HR only**

#### API Endpoint
- **URL:** `PUT /users/{id}`
- **Body:**
```json
{
  "name": "Updated Name",
  "email": "new-email@imes.com",
  "role": "MENTOR",
  "isActive": true
}
```

#### Business Rules
- Không thể update password qua endpoint này
- Email mới phải unique
- Phải có quyền HR

---

### 2.5. Xóa Người Dùng (Delete User)

#### Mô tả chi tiết
Xóa người dùng khỏi hệ thống (soft delete hoặc hard delete tùy config).

#### Vai trò
- **HR only**

#### API Endpoint
- **URL:** `DELETE /users/{id}`

#### Business Rules
- Không thể xóa chính mình
- Recommend: Soft delete (set isActive = false) thay vì hard delete

---

## 3. Quản Lý Intern

### 3.1. Thêm Intern Mới (Create Intern)

#### Mô tả chi tiết
HR tạo hồ sơ intern mới với thông tin cá nhân đầy đủ. Khác với User, Intern có thêm các thông tin như phone, address, school, major, startDate, endDate.

#### Vai trò
- **HR only**

#### Luồng xử lý
```
[HR Client] → POST /interns
  ↓
[Gateway] → Intern Service
  ↓
[Intern Service] Validate data
  ↓
[Database] INSERT INTO interns
  ↓
[Intern Service] Return intern profile
  ↓
[HR Client] ← Response: Created intern
```

#### API Endpoint
- **URL:** `POST /interns`
- **Body:**
```json
{
  "fullName": "Nguyen Van A",
  "email": "intern1@imes.com",
  "phone": "0901234567",
  "address": "123 Nguyen Hue, HCMC",
  "school": "University of Technology",
  "major": "Computer Science",
  "startDate": "2026-03-01",
  "endDate": "2026-06-30"
}
```
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Success"
  },
  "data": {
    "id": 10,
    "fullName": "Nguyen Van A",
    "email": "intern1@imes.com",
    "phone": "0901234567",
    "address": "123 Nguyen Hue, HCMC",
    "school": "University of Technology",
    "major": "Computer Science",
    "startDate": "2026-03-01",
    "endDate": "2026-06-30",
    "status": "ACTIVE",
    "createdAt": "2026-02-26T11:00:00"
  }
}
```

#### Validation Rules
- Full Name: Required, 2-100 ký tự
- Email: Required, unique, format hợp lệ
- Phone: Required, format số điện thoại VN
- Start Date: Required, phải là ngày trong tương lai hoặc hôm nay
- End Date: Required, phải sau startDate

#### Business Rules
- Email intern không được trùng
- Thời gian thực tập phải hợp lệ (startDate < endDate)
- Mặc định status = ACTIVE

---

### 3.2. Xem Danh Sách Intern (List Interns)

#### Mô tả chi tiết
Xem danh sách tất cả intern với pagination, filter, search.

#### Vai trò
- **HR, Mentor**

#### API Endpoint
- **URL:** `GET /interns`
- **Query Parameters:**
  - `page`: số trang
  - `size`: số records
  - `status`: ACTIVE, COMPLETED, TERMINATED
  - `search`: tìm theo tên/email

---

### 3.3. Xem Chi Tiết Intern (Get Intern by ID)

#### API Endpoint
- **URL:** `GET /interns/{id}`

---

### 3.4. Cập Nhật Intern (Update Intern)

#### Vai trò
- **HR only**

#### API Endpoint
- **URL:** `PUT /interns/{id}`

---

### 3.5. Xóa/Kết Thúc Intern (Terminate Intern)

#### Mô tả chi tiết
Đánh dấu intern đã kết thúc thực tập (set status = COMPLETED hoặc TERMINATED).

#### API Endpoint
- **URL:** `PATCH /interns/{id}/terminate`

---

## 4. Quản Lý Phòng Ban

### 4.1. Tạo Phòng Ban (Create Department)

#### Mô tả chi tiết
HR tạo phòng ban mới trong công ty.

#### Vai trò
- **HR only**

#### API Endpoint
- **URL:** `POST /departments`
- **Body:**
```json
{
  "name": "Engineering",
  "description": "Software Development Team",
  "managerId": 2
}
```

---

### 4.2. Danh Sách Phòng Ban (List Departments)

#### API Endpoint
- **URL:** `GET /departments`

---

### 4.3. Cập Nhật Phòng Ban (Update Department)

#### API Endpoint
- **URL:** `PUT /departments/{id}`

---

### 4.4. Xóa Phòng Ban (Delete Department)

#### API Endpoint
- **URL:** `DELETE /departments/{id}`

#### Business Rules
- Không thể xóa phòng ban còn có intern hoặc mentor

---

## 5. Quản Lý Bài Tập

### 5.1. Tạo Bài Tập (Create Assignment)

#### Mô tả chi tiết
Mentor tạo bài tập mới cho các intern. Bài tập bao gồm tiêu đề, mô tả, deadline, và status ban đầu là OPEN.

#### Vai trò
- **Mentor only**

#### Luồng xử lý
```
[Mentor Client] → POST /assignments
  ↓
[Gateway] → Assignment Service
  ↓
[Assignment Service] Validate data
  ↓
[Assignment Service] Get mentorId from X-User-Id header
  ↓
[Database] INSERT INTO assignments
  ↓
[Assignment Service] Return assignment
  ↓
[Mentor Client] ← Response: Created assignment
```

#### API Endpoint
- **URL:** `POST /assignments`
- **Headers:**
  - `Authorization: Bearer {token}`
  - `X-User-Id: {mentorId}`
- **Body:**
```json
{
  "title": "Build REST API with Spring Boot",
  "description": "Create a REST API for managing books using Spring Boot, Spring Data JPA, and PostgreSQL",
  "deadline": "2026-03-15"
}
```
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Success"
  },
  "data": {
    "id": 1,
    "title": "Build REST API with Spring Boot",
    "description": "Create a REST API for managing books...",
    "deadline": "2026-03-15",
    "mentorId": 2,
    "status": "OPEN",
    "submissionCount": 0,
    "createdAt": "2026-02-26T14:00:00",
    "updatedAt": "2026-02-26T14:00:00"
  }
}
```

#### Validation Rules
- Title: Required, 5-255 ký tự
- Description: Optional, max 5000 ký tự
- Deadline: Required, phải là ngày trong tương lai
- MentorId: Tự động lấy từ user đang login

#### Business Rules
- Chỉ Mentor mới được tạo assignment
- Status mặc định là OPEN
- Assignment sẽ tự động gán cho tất cả intern trong hệ thống (hoặc theo mentor assignment)

---

### 5.2. Xem Danh Sách Bài Tập (List Assignments)

#### Mô tả chi tiết
Xem danh sách bài tập theo vai trò:
- **Mentor:** Xem tất cả bài tập của mình tạo ra
- **Intern:** Xem bài tập được giao

#### Vai trò
- **Mentor, Intern**

#### API Endpoint
- **URL:** `GET /assignments/my-assignments`
- **Query Parameters:**
  - `page`: số trang (default: 0)
  - `size`: số records (default: 10)
- **Headers:**
  - `X-User-Id`: ID của user đang login
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Success"
  },
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Build REST API",
        "description": "...",
        "deadline": "2026-03-15",
        "mentorId": 2,
        "status": "OPEN",
        "submissionCount": 3,
        "createdAt": "2026-02-26T14:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3
  }
}
```

#### Logic
- **Nếu user là Mentor:** Lấy các assignment có mentorId = userId
- **Nếu user là Intern:** Lấy các assignment từ mentor được phân công

---

### 5.3. Xem Chi Tiết Bài Tập (Get Assignment by ID)

#### API Endpoint
- **URL:** `GET /assignments/{id}`
- **Response:** Chi tiết assignment + số lượng submission

---

### 5.4. Nộp Bài Tập (Submit Assignment)

#### Mô tả chi tiết
Intern nộp bài làm cho assignment. Khi nộp lần đầu, status của assignment tự động chuyển từ OPEN → SUBMITTED.

#### Vai trò
- **Intern only**

#### Luồng xử lý
```
[Intern Client] → POST /assignments/{id}/submit
  ↓
[Gateway] → Assignment Service
  ↓
[Assignment Service] Validate intern chưa nộp bài này
  ↓
[Database] INSERT INTO submissions
  ↓
[Assignment Service] Check: Nếu submission đầu tiên
  ↓
[Database] UPDATE assignments SET status = 'SUBMITTED'
  ↓
[Assignment Service] Increment submissionCount
  ↓
[Intern Client] ← Response: Submission created
```

#### API Endpoint
- **URL:** `POST /assignments/{id}/submit`
- **Headers:**
  - `X-User-Id: {internId}`
- **Body:**
```json
{
  "content": "I have completed the REST API with CRUD operations for books. Implemented Spring Security with JWT authentication. Database: PostgreSQL with Spring Data JPA.",
  "attachmentUrl": "https://github.com/intern1/book-api"
}
```
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Success"
  },
  "data": {
    "id": 5,
    "assignmentId": 1,
    "internId": 4,
    "content": "I have completed the REST API...",
    "attachmentUrl": "https://github.com/intern1/book-api",
    "submittedAt": "2026-03-10T16:30:00"
  }
}
```

#### Validation Rules
- Content: Required, minimum 10 ký tự
- AttachmentUrl: Optional, phải là URL hợp lệ
- Intern chỉ được nộp 1 lần cho mỗi assignment

#### Business Rules
- Mỗi intern chỉ submit 1 lần cho 1 assignment (UNIQUE constraint)
- Khi có submission đầu tiên, status assignment → SUBMITTED
- SubmissionCount tự động tăng
- Không thể nộp sau deadline (có thể warning nhưng vẫn cho nộp)

#### Error Codes
- `5001`: Intern đã nộp bài này rồi (duplicate submission)
- `5002`: Assignment không tồn tại
- `5003`: Content quá ngắn

---

### 5.5. Xem Danh Sách Bài Nộp (View Submissions)

#### Mô tả chi tiết
Mentor xem tất cả bài nộp của các intern cho 1 assignment cụ thể.

#### Vai trò
- **Mentor only**

#### API Endpoint
- **URL:** `GET /assignments/{id}/submissions`
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Success"
  },
  "data": [
    {
      "id": 5,
      "assignmentId": 1,
      "internId": 4,
      "internName": "Nguyen Van A",
      "content": "Completed REST API...",
      "attachmentUrl": "https://github.com/intern1/book-api",
      "submittedAt": "2026-03-10T16:30:00"
    },
    {
      "id": 6,
      "assignmentId": 1,
      "internId": 5,
      "internName": "Tran Thi B",
      "content": "Finished with Docker integration...",
      "attachmentUrl": "https://github.com/intern2/book-api",
      "submittedAt": "2026-03-11T10:15:00"
    }
  ]
}
```

#### Business Rules
- Chỉ mentor tạo assignment mới xem được submissions
- Hiển thị theo thứ tự submit (submittedAt DESC)

---

### 5.6. Đánh Dấu Hoàn Thành (Mark as Completed)

#### Mô tả chi tiết
Mentor đánh dấu assignment đã hoàn thành sau khi review tất cả submissions.

#### Vai trò
- **Mentor only**

#### API Endpoint
- **URL:** `PUT /assignments/{id}/complete`
- **Response:** Assignment với status = COMPLETED

#### Business Rules
- Chỉ mentor tạo assignment mới có quyền
- Status chuyển từ SUBMITTED → COMPLETED

---

## 6. Quản Lý Chấm Công

### 6.1. Check-in (Clock In)

#### Mô tả chi tiết
Intern check-in khi đến công ty. Hệ thống ghi nhận thời gian check-in và lưu vào bảng attendances.

#### Vai trò
- **Intern only**

#### Luồng xử lý
```
[Intern Client] → POST /attendances/check-in
  ↓
[Gateway] → Attendance Service
  ↓
[Attendance Service] Get internId from header
  ↓
[Attendance Service] Check: Đã check-in hôm nay chưa?
  ↓
[Database] INSERT INTO attendances (intern_id, date, check_in_time)
  ↓
[Intern Client] ← Response: Check-in success
```

#### API Endpoint
- **URL:** `POST /attendances/check-in`
- **Headers:**
  - `X-User-Id: {internId}`
- **Body:** Empty `{}`
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Check-in successful"
  },
  "data": {
    "id": 50,
    "internId": 4,
    "date": "2026-02-26",
    "checkInTime": "08:30:00",
    "checkOutTime": null,
    "status": "PRESENT"
  }
}
```

#### Validation Rules
- Intern chỉ được check-in 1 lần mỗi ngày
- Không thể check-in cho ngày trong quá khứ

#### Business Rules
- Check-in time tự động = thời gian hiện tại
- Status mặc định = PRESENT
- Nếu check-in sau 9h sáng → status = LATE

#### Error Codes
- `6001`: Đã check-in rồi hôm nay
- `6002`: Không phải intern

---

### 6.2. Check-out (Clock Out)

#### Mô tả chi tiết
Intern check-out khi về. Hệ thống cập nhật checkOutTime cho record attendance của ngày hôm đó.

#### Vai trò
- **Intern only**

#### Luồng xử lý
```
[Intern Client] → POST /attendances/check-out
  ↓
[Gateway] → Attendance Service
  ↓
[Attendance Service] Find attendance record hôm nay
  ↓
[Database] UPDATE attendances SET check_out_time = NOW()
  ↓
[Attendance Service] Calculate working hours
  ↓
[Intern Client] ← Response: Check-out success
```

#### API Endpoint
- **URL:** `POST /attendances/check-out`
- **Headers:**
  - `X-User-Id: {internId}`
- **Response:**
```json
{
  "status": {
    "code": "0000",
    "message": "Check-out successful"
  },
  "data": {
    "id": 50,
    "internId": 4,
    "date": "2026-02-26",
    "checkInTime": "08:30:00",
    "checkOutTime": "17:45:00",
    "workingHours": 9.25,
    "status": "PRESENT"
  }
}
```

#### Business Rules
- Phải check-in trước mới check-out được
- Working hours = checkOutTime - checkInTime (giờ)

#### Error Codes
- `6003`: Chưa check-in hôm nay
- `6004`: Đã check-out rồi

---

### 6.3. Xem Lịch Sử Chấm Công (View Attendance History)

#### Mô tả chi tiết
Xem lịch sử chấm công của intern theo tháng/tuần.

#### Vai trò
- **Intern (xem của mình), HR/Mentor (xem của tất cả)**

#### API Endpoint
- **URL:** `GET /attendances`
- **Query Parameters:**
  - `internId`: filter theo intern (optional)
  - `startDate`: từ ngày
  - `endDate`: đến ngày
  - `page`, `size`: pagination

---

### 6.4. Báo Cáo Chấm Công (Attendance Report)

#### API Endpoint
- **URL:** `GET /attendances/report?month=2&year=2026`
- **Response:** Tổng hợp số ngày đi làm, đi muộn, nghỉ

---

## 7. Phân Công Mentor

### 7.1. Phân Công Mentor cho Intern

#### Mô tả chi tiết
HR phân công mentor chịu trách nhiệm hướng dẫn intern.

#### Vai trò
- **HR only**

#### API Endpoint
- **URL:** `POST /mentor-assignments`
- **Body:**
```json
{
  "mentorId": 2,
  "internId": 4,
  "startDate": "2026-03-01",
  "endDate": "2026-06-30"
}
```

#### Business Rules
- 1 intern có thể có nhiều mentor trong các giai đoạn khác nhau
- 1 mentor có thể hướng dẫn nhiều intern cùng lúc

---

### 7.2. Xem Danh Sách Intern của Mentor

#### API Endpoint
- **URL:** `GET /mentor-assignments/my-interns`
- **Headers:** `X-User-Id: {mentorId}`

---

### 7.3. Xem Mentor của Intern

#### API Endpoint
- **URL:** `GET /mentor-assignments/my-mentors`
- **Headers:** `X-User-Id: {internId}`

---

## 📊 Tổng Quan Hệ Thống

### Kiến Trúc Microservices

```
                    ┌─────────────────┐
                    │   API Gateway   │
                    │   Port: 8080    │
                    └────────┬────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
    ┌─────▼─────┐     ┌─────▼─────┐     ┌─────▼─────┐
    │   Auth    │     │   User    │     │  Intern   │
    │  Service  │     │  Service  │     │  Service  │
    │  :8081    │     │  :8082    │     │  :8083    │
    └───────────┘     └───────────┘     └───────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
    ┌─────▼─────┐     ┌─────▼─────┐     ┌─────▼─────┐
    │Attendance │     │Assignment │     │  Mentor   │
    │  Service  │     │  Service  │     │Assignment │
    │  :8084    │     │  :8085    │     │  Service  │
    └───────────┘     └───────────┘     └───────────┘
                             │
                    ┌────────▼────────┐
                    │   PostgreSQL    │
                    │   Port: 5433    │
                    └─────────────────┘
```

### Database Schema

#### users
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### interns
```sql
CREATE TABLE interns (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    school VARCHAR(200),
    major VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### assignments
```sql
CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    deadline DATE NOT NULL,
    mentor_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'OPEN',
    submission_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### submissions
```sql
CREATE TABLE submissions (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES assignments(id),
    intern_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    attachment_url VARCHAR(500),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(assignment_id, intern_id)
);
```

#### attendances
```sql
CREATE TABLE attendances (
    id BIGSERIAL PRIMARY KEY,
    intern_id BIGINT NOT NULL,
    date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    status VARCHAR(20) DEFAULT 'PRESENT',
    UNIQUE(intern_id, date)
);
```

---

## 🔒 Phân Quyền & Bảo Mật

### Matrix Phân Quyền

| Tính Năng | HR | Mentor | Intern |
|-----------|----|---------| -------|
| **User Management** | ✅ CRUD | ❌ | ❌ |
| **Intern Management** | ✅ CRUD | ✅ View | ❌ |
| **Department Management** | ✅ CRUD | ❌ | ❌ |
| **Create Assignment** | ❌ | ✅ | ❌ |
| **View Assignments** | ❌ | ✅ (own) | ✅ (assigned) |
| **Submit Assignment** | ❌ | ❌ | ✅ |
| **View Submissions** | ❌ | ✅ (own assignments) | ❌ |
| **Mark Completed** | ❌ | ✅ | ❌ |
| **Check-in/out** | ❌ | ❌ | ✅ |
| **View Attendance** | ✅ (all) | ✅ (assigned interns) | ✅ (own) |
| **Mentor Assignment** | ✅ | ❌ | ❌ |

### Security Headers
- `Authorization: Bearer {JWT_TOKEN}` - Xác thực
- `X-User-Id: {userId}` - Identify user
- `X-User-Role: {role}` - Phân quyền

---

## 📈 Performance & Scalability

### Pagination
Tất cả list endpoints đều hỗ trợ pagination:
- Default: page=0, size=20
- Max size: 100

### Caching Strategy
- JWT token: Cache 24h
- User info: Cache 1h
- Assignment list: No cache (real-time)

### Rate Limiting
- Login: 5 requests/minute/IP
- API calls: 100 requests/minute/user

---

## 🔔 Notifications (Future)

### Email Notifications
- Assignment mới được tạo
- Deadline sắp đến (1 ngày trước)
- Submission được mentor review
- Check-in muộn warning

### In-App Notifications
- Real-time với WebSocket
- Badge count chưa đọc

---

## 📱 Mobile App (Future)

### React Native
- iOS & Android support
- Push notifications
- Offline mode
- QR code check-in

---

## ✅ Testing

### Unit Tests
- Service layer: 80% coverage
- Repository layer: 90% coverage

### Integration Tests
- E2E flow cho các use case chính
- API contract testing

### Load Testing
- 1000 concurrent users
- Response time < 200ms (p95)

---

## 📝 Changelog

### Version 1.0.0 (26/02/2026)
- ✅ Authentication & Authorization
- ✅ User Management (HR)
- ✅ Intern Management
- ✅ Assignment Management
- ✅ Attendance Management
- ✅ Mentor Assignment

### Future Versions
- [ ] Evaluation & Feedback
- [ ] Notification System
- [ ] File Upload for attachments
- [ ] Chat between Mentor-Intern
- [ ] Calendar view cho assignments
- [ ] Dashboard analytics

---

## 🌐 API Base URLs

### Development
- Gateway: `http://localhost:8080`
- Auth: `http://localhost:8081`
- User: `http://localhost:8082`
- Intern: `http://localhost:8083`
- Attendance: `http://localhost:8084`
- Assignment: `http://localhost:8085`

### Production
- Gateway: `https://api.imes.com`

---

## 📞 Contact & Support

- **Email:** support@imes.com
- **Slack:** #imes-support
- **Documentation:** https://docs.imes.com

---

**© 2026 IMES - Intern Management & Evaluation System**
