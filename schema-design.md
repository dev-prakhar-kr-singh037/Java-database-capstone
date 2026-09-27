# Database Schema Design Documentation

## 1. Overview & Polyglot Architecture

This application (`com.smartclinicsystem.demo`) uses a **polyglot persistence architecture** combining a relational database (**MySQL**) and a document store (**MongoDB**):

* **MySQL Database**: Stores primary operational domain entities (**Admin**, **Patient**, **Doctor**, **Appointment**). It enforces relational constraints, primary/foreign key mappings, and consistency across core entity interactions.
* **MongoDB Database**: Stores clinical **Prescription** documents inside the `prescription` collection. Using MongoDB allows flexible storage of prescription notes and metadata associated with patients, doctors, and appointments without requiring SQL migrations.

---

## 2. Relational Schema Design (MySQL)

### Entity Relationship Diagram (ASCII Layout)

```
       +-----------------------+              +-----------------------+
       |        DOCTOR         |              |        PATIENT        |
       +-----------------------+              +-----------------------+
       | doctorid (PK)         |              | patientid (PK)        |
       | firstname             |              | firstname             |
       | lastname              |              | lastname              |
       | specialisation        |              | dob                   |
       | contact               |              | gender                |
       | email                 |              | phoneno               |
       | createdat             |              | email                 |
       +-----------------------+              | createdat             |
           ^               ^                  | address               |
           |               |                  +-----------------------+
           | 1             | 1                    ^               ^
           |               |                      |               |
           |               +---------------+      | 1             |
           | 1                             |      |               |
           |                               v      v 1             |
+----------------------+              +----------------------+    |
|        ADMIN         |              |     APPOINTMENT      |    |
+----------------------+              +----------------------+    |
| adminid (PK)         |              | appointment_id (PK)  |    |
| username             |              | doctor_id (FK) ------+    |
| password             |              | patient_id (FK) ----------+
| firstname            |              | appointment_time     |
| lastname             |              | status               |
| email                |              +----------------------+
| contact              |                         |
| createdat            |                         v (Logical Reference)
| doctor_id (FK)       |             [ MongoDB: prescription Collection ]
| patient_id (FK)      |
+----------------------+
```

---

### DDL Implementation Script (`schema.sql`)

```sql
-- Disable foreign key checks for table creation
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. DOCTORS TABLE
CREATE TABLE doctors (
    doctorid BIGINT AUTO_INCREMENT PRIMARY KEY,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    specialisation VARCHAR(150) NOT NULL,
    contact INT NOT NULL,
    email VARCHAR(150),
    createdat DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. PATIENTS TABLE
CREATE TABLE patients (
    patientid BIGINT AUTO_INCREMENT PRIMARY KEY,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    dob VARCHAR(50) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    phoneno VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    createdat DATE,
    address VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. ADMINS TABLE
CREATE TABLE admins (
    adminid BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    contact VARCHAR(20) NOT NULL,
    createdat DATE,
    doctor_id BIGINT DEFAULT NULL,
    patient_id BIGINT DEFAULT NULL,
    
    CONSTRAINT fk_admin_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctors(doctorid) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_admin_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(patientid) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. APPOINTMENTS TABLE
CREATE TABLE appointments (
    appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    appointment_time DATE NOT NULL,
    status INT NOT NULL, -- Status mapping: 0 = Scheduled, 1 = Completed, 2 = Cancelled
    
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctors(doctorid) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(patientid) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for Query Optimization
CREATE INDEX idx_appointment_doctor ON appointments(doctor_id);
CREATE INDEX idx_appointment_patient ON appointments(patient_id);
CREATE INDEX idx_admin_doctor ON admins(doctor_id);
CREATE INDEX idx_admin_patient ON admins(patient_id);
```

---

## 3. MongoDB Document Schema (NoSQL)

### Collection Name: `prescription`

Prescription records are stored as JSON documents in MongoDB. Cross-database linkages to MySQL are maintained via string representations of `patient_id`, `doctor_id`, and `appointment_id`.

#### Sample MongoDB Document

```json
{
  "_id": {
    "$oid": "660d2b4f9a1b2c3d4e5f6a7b"
  },
  "prescription_id": "660d2b4f9a1b2c3d4e5f6a7b",
  "patient_id": "1",
  "doctor_id": "1",
  "appointment_id": "1",
  "issued_date": "2026-09-28",
  "notes": "Patient requires 500mg Amoxicillin twice daily for 5 days. Rest recommended.",
  "created_at": "2026-09-28"
}
```

#### MongoDB Collection Indexes

```javascript
// Indexing cross-database reference keys for quick lookups
db.prescription.createIndex({ "patient_id": 1 });
db.prescription.createIndex({ "doctor_id": 1 });
db.prescription.createIndex({ "appointment_id": 1 });
```

---

## 4. Entity Mappings & Field Specifications

### 4.1 JPA Entities (MySQL)

#### Entity: `Admin` (`admins` table)
| Field | Type / Annotation | DB Column | Description |
| :--- | :--- | :--- | :--- |
| `adminid` | `Long` / `@Id` | `adminid` | Primary Key |
| `username` | `String` | `username` | Login handle |
| `password` | `String` | `password` | Password string |
| `firstname` | `String` | `firstname` | First name |
| `lastname` | `String` | `lastname` | Last name |
| `email` | `String` | `email` | Contact email |
| `contact` | `String` | `contact` | Contact number |
| `createdAt` | `LocalDate` | `createdat` | Creation timestamp |
| `doctor` | `Doctor` / `@ManyToOne(fetch = LAZY)` | `doctor_id` | Refers to `Doctor.doctorid` |
| `patient` | `Patient` / `@ManyToOne(fetch = LAZY)` | `patient_id` | Refers to `Patient.patientid` |

#### Entity: `Patient` (`patients` table)
| Field | Type / Annotation | DB Column | Description |
| :--- | :--- | :--- | :--- |
| `patientid` | `Long` / `@Id` | `patientid` | Primary Key |
| `firstname` | `String` | `firstname` | Patient's first name |
| `lastname` | `String` | `lastname` | Patient's last name |
| `dob` | `String` | `dob` | Date of birth |
| `gender` | `String` | `gender` | Gender |
| `phoneno` | `String` | `phoneno` | Phone contact |
| `email` | `String` | `email` | Email address |
| `createdat` | `LocalDate` | `createdat` | Account creation date |
| `address` | `String` / `@NotNull` | `address` | Residential address |
| `appointments` | `List<Appointment>` / `@OneToMany(mappedBy="patient")` | N/A | List of patient appointments |

#### Entity: `Doctor` (`doctors` table)
| Field | Type / Annotation | DB Column | Description |
| :--- | :--- | :--- | :--- |
| `doctorid` | `Long` / `@Id` | `doctorid` | Primary Key |
| `firstname` | `String` | `firstname` | Doctor's first name |
| `lastname` | `String` | `lastname` | Doctor's last name |
| `specialisation`| `String` | `specialisation`| Medical specialty |
| `contact` | `int` | `contact` | Doctor phone contact |
| `email` | `String` | `email` | Doctor email address |
| `createdat` | `LocalDate` | `createdat` | Registration date |
| `appointments` | `List<Appointment>` / `@OneToMany(mappedBy="doctor")` | N/A | List of assigned appointments |

#### Entity: `Appointment` (`appointments` table)
| Field | Type / Annotation | DB Column | Description |
| :--- | :--- | :--- | :--- |
| `appointment_id`| `Long` / `@Id` | `appointment_id` | Primary Key |
| `doctor` | `Doctor` / `@ManyToOne` | `doctor_id` | FK referencing Doctor |
| `patient` | `Patient` / `@ManyToOne` | `patient_id` | FK referencing Patient |
| `appointmentTime`| `LocalDate` / `@FutureOrPresent` | `appointment_time` | Scheduled date |
| `status` | `Integer` / `@NotNull` | `status` | `0` = Scheduled, `1` = Completed, `2` = Cancelled |

---

### 4.2 Spring Data MongoDB Document

#### Document Class: `Prescription` (`prescription` collection)
| Field | Java Type | MongoDB Field | Annotations / Constraints |
| :--- | :--- | :--- | :--- |
| `prescription_id` | `String` | `prescription_id` | `@Id`, `@Field("prescription_id")` |
| `patientId` | `String` | `patient_id` | `@NotNull`, `@Field("patient_id")` |
| `doctorId` | `String` | `doctor_id` | `@NotNull`, `@Field("doctor_id")` |
| `appointmentId` | `String` | `appointment_id` | `@NotNull`, `@Field("appointment_id")` |
| `issuedDate` | `LocalDate` | `issued_date` | `@NotNull`, `@Field("issued_date")` |
| `notes` | `String` | `notes` | `@NotNull(message = "Prescription notes are required")`, `@Field("notes")` |
| `createdAt` | `LocalDate` | `created_at` | `@Field("created_at")` |