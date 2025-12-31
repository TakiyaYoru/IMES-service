# Test Credentials

## Available Test Accounts

| Email | Password | Role | Full Name |
|-------|----------|------|-----------|
| admin@imes.com | admin123 | ADMIN | System Administrator |
| hr@imes.com | hr123 | HR | HR Manager |
| mentor@imes.com | mentor123 | MENTOR | Senior Mentor |
| intern@imes.com | intern123 | INTERN | Test Intern |

## API Endpoints

### Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@imes.com",
  "password": "admin123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "admin@imes.com",
  "fullName": "System Administrator",
  "role": "ADMIN"
}
```

### Test Protected Endpoint
```bash
GET http://localhost:8080/api/auth/me
Authorization: Bearer {token}
```

### Logout
```bash
POST http://localhost:8080/api/auth/logout
Authorization: Bearer {token}
```
