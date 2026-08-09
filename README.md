# 🏥 MediCure - Clinic Management System

A modern full-stack Clinic Management System built with **Spring Boot**, **React**, and **MySQL** to streamline clinic operations. The application provides secure role-based access for Admin, Doctor, Patient, and Receptionist, along with appointment scheduling, patient record management, digital prescriptions, billing, authentication, and an architecture ready for seamless healthcare features.

---

## 📖 Overview

The Clinic Management System is designed to simplify and digitize day-to-day clinic workflows by providing a centralized platform for managing appointments, patients, doctors, prescriptions, billing, and administrative operations.

The backend is fully developed using **Spring Boot**, while the frontend is built with **React**. Future releases will introduce AI-powered healthcare assistance using Spring AI and OpenAI APIs.

---

## ✨ Features

### 👤 Patient
- Secure Registration & Login
- Google OAuth 2.0 Login
- Forgot & Reset Password via Email
- Book Appointments
- View Appointment History
- View Medical Records
- View Prescriptions
- View Bills & Payment History
- Profile Management

### 👨‍⚕️ Doctor
- Doctor Dashboard with Analytics
- Manage Availability & Schedule
- View Assigned Appointments
- Access Patient Medical History
- Create & Manage Prescriptions
- Update Patient Records
- Profile Management

### 🧑‍💼 Admin
- Dashboard Analytics
- Manage Doctors
- Manage Patients
- Manage Receptionists
- Manage Appointments
- System Settings
- User Management

### 🏥 Receptionist
- Register Walk-in Patients
- Schedule Appointments
- Billing & Payment Management
- Manage Patient Records
- View Doctor Availability

### 📅 Appointment Management
- Slot-Based Appointment Booking
- Doctor Availability Scheduling
- Appointment Status Tracking (Scheduled → Confirmed → In Progress → Completed)
- Appointment History

### 💊 Prescription Management
- Digital Prescription Generation
- Patient Prescription History
- Doctor Notes & Instructions

### 🧾 Billing Management
- Create Bills per Appointment
- Record Payments (Cash, UPI, Card, Net Banking)
- View Outstanding Balances
- Paid & Unpaid Bill History

### 🔐 Authentication & Security
- JWT Authentication
- Google OAuth 2.0 Login
- Role-Based Access Control (RBAC)
- BCrypt Password Encryption
- Email-based Password Reset
- Secure REST APIs

---

## 🛠️ Tech Stack

### Backend
| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.2.0 |
| Spring Security | Included |
| Spring Data JPA | Included |
| Hibernate | Included |
| MySQL | 8.0+ |
| JWT (jjwt) | 0.11.5 |
| OAuth2 Client | Included |
| Spring Mail | Included |
| Thymeleaf | Included |
| Lombok | 1.18.34 |
| Maven | 3.9+ |

### Frontend
| Technology | Version |
|---|---|
| React | 18 |
| Vite | 5 |
| React Router | 6 |
| Axios | Latest |
| Tailwind CSS | 3 |
| Framer Motion | 10 |
| Recharts | Latest |
| Context API | Built-in |

### AI (Upcoming)
- Spring AI
- OpenAI API
- Medical Assistant Chatbot
- AI Doctor Recommendation
- Symptom Analysis
- AI-powered Clinical Assistance

---

## 📁 Repository Structure

```
Clinic-Management-System/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/clinic/management/
│   │       └── resources/
│   │           ├── application.yml
│   │           └── templates/
│   ├── pom.xml
│   ├── mvnw
│   └── .mvn/
│       └── jvm.config
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── README.md
└── .gitignore
```

---

## 🏗️ Backend Architecture

```
backend/src/main/java/com/clinic/management/
│
├── config/          → SecurityConfig, CORS
├── controller/      → REST controllers per role
├── dto/             → Request/Response objects
├── entity/          → JPA entities
├── enums/           → Role, UserStatus
├── repository/      → Spring Data JPA repositories
├── security/        → JWT, OAuth2, UserDetails
├── service/         → Business logic
├── exception/       → Global exception handling
└── util/            → Helper utilities
```

---

## 🎨 Frontend Architecture

```
frontend/src/
│
├── assets/          → Static assets
├── components/      → Reusable UI components
│   ├── common/      → Shared components (Button, Modal, etc.)
│   ├── layout/      → Navbar, Sidebar, DashboardLayout
│   └── auth/        → ProtectedRoute, RoleRoute
├── context/         → AuthContext, ThemeContext, etc.
├── hooks/           → Custom hooks (useApi, useTitle, etc.)
├── pages/           → Page components per role
│   ├── admin/
│   ├── doctor/
│   ├── patient/
│   ├── receptionist/
│   ├── auth/
│   └── errors/
├── routes/          → AppRoutes (lazy loaded)
├── services/        → API service files per role
└── utils/           → helpers, validation, constants
```

---

## ⚙️ Prerequisites

- **Java 21** (tested with Java 24 using `.mvn/jvm.config`)
- **Maven 3.9+**
- **MySQL 8.0+**
- **Node.js 18+**
- **npm**
- **Google Cloud Console account** (for OAuth2)
- **Gmail App Password** (for password reset emails)

---

## 🚀 Backend Setup

### 1. Clone the repository

```bash
git clone https://github.com/DevmalyaBhattacharjee/Clinic-Management-System.git
cd Clinic-Management-System/backend
```

### 2. Configure `application.yml`

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/clinic_db?createDatabaseIfNotExist=true
    username: your_mysql_username
    password: your_mysql_password

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET

  mail:
    username: your_gmail@gmail.com
    password: your_gmail_app_password

jwt:
  secret: your_256_bit_hex_secret

app:
  frontend:
    url: http://localhost:3000
  mail:
    from: "MediCure <your_gmail@gmail.com>"
```

### 3. Google Cloud Console Setup

1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Create a project → **APIs & Services** → **Credentials**
3. Create **OAuth 2.0 Client ID** (Web Application)
4. Add Authorized redirect URI:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
5. Add Authorized JavaScript origins:
   ```
   http://localhost:3000
   http://localhost:8080
   ```

### 4. Run the backend

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```powershell
mvn clean spring-boot:run
```

The backend runs at: `http://localhost:8080`

> **Note for Java 24 users:** The `.mvn/jvm.config` file is already configured with the required `--add-opens` flags for Lombok compatibility.

---

## 💻 Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

The frontend runs at: **`http://localhost:3000`**

---

## 🔑 REST API Modules

| Module | Base Endpoint |
|---|---|
| Authentication | `/api/auth` |
| Admin — Doctors | `/api/admin/doctors` |
| Admin — Patients | `/api/admin/patients` |
| Admin — Receptionists | `/api/admin/receptionists` |
| Admin — Appointments | `/api/admin/appointments` |
| Doctor — Appointments | `/api/doctor/appointments` |
| Doctor — Records | `/api/doctor/medical-records` |
| Doctor — Prescriptions | `/api/doctor/prescriptions` |
| Doctor — Availability | `/api/doctor/availability` |
| Patient — Appointments | `/api/patient/appointments` |
| Patient — Records | `/api/patient/medical-records` |
| Patient — Prescriptions | `/api/patient/prescriptions` |
| Patient — Bills | `/api/patient/bills` |
| Patient — Profile | `/api/patient/profile` |
| Receptionist — Patients | `/api/receptionist/patients` |
| Receptionist — Appointments | `/api/receptionist/appointments` |
| Receptionist — Bills | `/api/receptionist/bills` |
| Receptionist — Doctors | `/api/receptionist/doctors` |

---

## 🗃️ Database Modules

- Users
- Patients
- Doctors
- Receptionists
- Appointments
- Doctor Availability
- Medical Records
- Prescriptions
- Bills
- Password Reset Tokens

---

## 🔐 Security Features

- JWT Authentication (stateless REST APIs)
- Google OAuth 2.0 Login
- BCrypt Password Encryption
- Role-Based Authorization (RBAC)
- Secure Email-based Password Reset (30-minute token expiry)
- CORS Configuration
- Protected REST APIs
- Session management for OAuth2 state validation

---

## 🤖 Planned AI Features

- AI Medical Chatbot
- Symptom Checker
- AI Prescription Assistance
- Doctor Recommendation Engine
- Medical Report Summarization
- Voice-to-Text Prescription Generation

---

## 📈 Future Enhancements

- Telemedicine
- Online Payments
- SMS & Email Notifications
- Inventory Management
- Laboratory Module
- Multi-Clinic Support
- AI Analytics Dashboard
- PDF & Excel Report Generation

---

## 🧪 Testing

- REST API Testing using **Postman** (collection included)
- Authentication & Authorization Testing
- Role-Based Access Testing
- Frontend Integration Testing

---

## 📌 Current Project Status

### ✅ Completed
- Spring Boot Backend (103 REST endpoints)
- JWT Authentication
- Google OAuth 2.0 Login
- Password Reset via Email
- Role-Based Access Control
- Appointment Management
- Medical Records Management
- Prescription Management
- Billing & Payment Management
- React Frontend
- Responsive Dashboard UI (all 4 roles)
- Dark Mode
- Global Search (Ctrl+K)
- Notifications System
- Skeleton Loaders
- Page Animations (Framer Motion)
- Lazy Loading & Code Splitting
- Global Error Boundary
- SEO & Accessibility

### 🚧 In Progress
- Frontend–Backend Integration Testing
- Email Notifications
- AI Integration

---

## 🤝 Contributing

Contributions are welcome.

1. Fork the repository.
2. Create a feature branch.
   ```bash
   git checkout -b feature/your-feature
   ```
3. Commit your changes.
   ```bash
   git commit -m "Add new feature"
   ```
4. Push to your branch.
   ```bash
   git push origin feature/your-feature
   ```
5. Open a Pull Request.

---

## 👨‍💻 Authors

- **Debolina Roy**
- **Devmalya Bhattacharjee**

---

## 📄 License

This project is licensed under the **MIT License**.

---

## ⭐ Support

If you found this project useful, consider giving it a ⭐ on GitHub.
