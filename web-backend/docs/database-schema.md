# Database Schema Documentation

## Overview
The Visitor Management System (VMS) uses a PostgreSQL database with the following main entities:
- Visitor
- Visit
- Employee
- OTP

## Tables

### visitor
Stores information about visitors to the premises.

| Column | Type | Description | Constraints |
|--------|------|-------------|-------------|
| id | BIGINT | Unique identifier | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR | Full name of visitor | NOT NULL |
| phone_number | VARCHAR | Contact phone number | NOT NULL |
| email | VARCHAR | Email address | NOT NULL, UNIQUE |
| address | VARCHAR | Physical address | |
| picture_path | VARCHAR | Path to visitor's profile picture | |
| created_at | TIMESTAMP | Record creation timestamp | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| employee_id | BIGINT | Reference to host employee | FOREIGN KEY |

### visiting_info
Tracks visitor check-ins and visit details.

| Column | Type | Description | Constraints |
|--------|------|-------------|-------------|
| id | BIGINT | Unique identifier | PRIMARY KEY, AUTO_INCREMENT |
| visitor_id | BIGINT | Reference to visitor | FOREIGN KEY, NOT NULL |
| host | VARCHAR | Host employee name | NOT NULL |
| otp | VARCHAR | One-time password | |
| is_approved | BOOLEAN | Visit approval status | DEFAULT FALSE |
| visit_date | TIMESTAMP | Date and time of visit | NOT NULL |

### employee
Stores information about employees who can host visitors.

| Column | Type | Description | Constraints |
|--------|------|-------------|-------------|
| id | BIGINT | Unique identifier | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR | Full name of employee | NOT NULL |
| email | VARCHAR | Email address | NOT NULL, UNIQUE |

### otp
Manages one-time passwords for visitor verification.

| Column | Type | Description | Constraints |
|--------|------|-------------|-------------|
| id | BIGINT | Unique identifier | PRIMARY KEY, AUTO_INCREMENT |
| email | VARCHAR | Email address | NOT NULL |
| otp | VARCHAR | One-time password | NOT NULL |
| expiration_time | TIMESTAMP | OTP expiration time | NOT NULL |
| created_at | TIMESTAMP | Record creation timestamp | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

## Relationships

1. **Visitor to Employee (Many-to-One)**
   - A visitor can be associated with one employee (host)
   - An employee can host multiple visitors
   - Foreign key: `visitor.employee_id` references `employee.id`

2. **Visit to Visitor (One-to-One)**
   - Each visit record is associated with exactly one visitor
   - A visitor can have multiple visit records
   - Foreign key: `visiting_info.visitor_id` references `visitor.id`

## Indexes

1. **visitor**
   - Primary key index on `id`
   - Unique index on `email`
   - Index on `employee_id` (foreign key)

2. **visiting_info**
   - Primary key index on `id`
   - Index on `visitor_id` (foreign key)
   - Index on `visit_date` (for date range queries)

3. **employee**
   - Primary key index on `id`
   - Unique index on `email`

4. **otp**
   - Primary key index on `id`
   - Index on `email` and `created_at` (for OTP lookup)

## Data Types and Constraints

- **Timestamps**: All timestamp fields use the `TIMESTAMP` type without timezone
- **Strings**: VARCHAR fields have appropriate length limits based on the data
- **Foreign Keys**: All foreign keys are indexed for better query performance
- **Unique Constraints**: Email addresses are unique across visitors and employees
- **NOT NULL**: Critical fields are marked as NOT NULL to ensure data integrity

## Security Considerations

1. **Data Encryption**
   - Sensitive data like OTPs are stored in encrypted form
   - AES encryption is used for secure data storage

2. **Access Control**
   - Database access is restricted to application users only
   - No direct database access is allowed for end users

3. **Data Retention**
   - OTP records are automatically expired after 10 minutes
   - Visit records are retained for audit purposes

## Backup and Recovery

1. **Backup Strategy**
   - Daily full database backups
   - Transaction log backups every hour
   - Backup retention period: 30 days

2. **Recovery Procedures**
   - Point-in-time recovery supported
   - Automated recovery testing monthly
   - Documented recovery procedures for different scenarios 