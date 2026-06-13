Hisaab - Shared Expense & Settlement Management System
A full stack expense splitting platform (Spring Boot + vanilla JS) that lets groups track shared expenses, automatically calculates running balances, and computes minimum transaction settlement plans like a self built Splitwise.
Features

JWT Authentication -> secure registration/login with BCrypt password hashing

Group & Expense Management -> create groups, add members by email, log shared expenses

My Groups & My Balances -> personalized views of all groups a user belongs to, and what they owe/are owed across all groups

Transactional Integrity -> expense creation, share calculation, balance updates, and audit logging happen atomically (@Transactional)

Settlement Engine -> greedy min-heap algorithm computes minimum number of payments needed to settle all debts (O(n log n))

Optimistic Locking -> @Version field on balance records prevents race conditions during concurrent updates, validated via concurrent-thread tests

Recurring Expenses -> Spring Scheduler auto-creates monthly bills (rent, etc.)

Audit Logging -> tracks all expense changes with before/after JSON snapshots

Caching -> cache-aside pattern for group summaries (in-memory, Redis-ready)

Practical, whole rupee splitting equal splits round to whole rupees with remainder adjustment, since Indian currency has no usable decimal denominations

Tech Stack

Backend: Java 17, Spring Boot 4, Spring Security, Spring Data JPA, Hibernate
Database: MySQL
Auth: JWT (jjwt)
Frontend: HTML, CSS, vanilla JavaScript (fetch API)
Testing: JUnit 5, concurrent-thread tests for optimistic locking

Architecture Overview
Controller Layer  - REST endpoints (auth, users, groups, expenses, settlements)
Service Layer     - business logic, @Transactional expense pipeline, settlement algorithm
Repository Layer  - Spring Data JPA interfaces
Entity Layer      - User, Group, GroupMember, Expense, ExpenseShare, Balance,
                     RecurringExpense, ActivityLog
Key Design Decisions

Running balance table (Balance) with net settlement logic instead of recomputing balances from all expense shares on every read, balances are updated incrementally and netted against reverse debts (if A owes B and B newly owes A, the smaller amount cancels out).
Greedy settlement algorithm - uses a max-heap (creditors) and min-heap (debtors) to repeatedly match the largest balances first, minimizing total transactions. This is a heuristic, not provably optimal for all cases (the general "minimum cash flow" problem is NP-hard), but performs well in practice.
Whole-rupee splitting equal splits are floored to whole rupees, and the remainder (₹0–2) is assigned to the last participant, so every share is a physically payable amount and the total always matches exactly.
Generic audit log stores old/new entity states as JSON strings, allowing one table to audit any entity type without per entity audit tables.
Optimistic locking via @Version concurrent updates to the same balance row are detected and rejected (ObjectOptimisticLockingFailureException) rather than silently overwritten.

API Endpoints
MethodEndpointDescriptionPOST/api/users/registerRegister a new userPOST/api/auth/loginLogin, returns JWTPOST/api/groupsCreate a group (creator auto added as member)GET/api/groups/my-groups/{userId}List all groups a user belongs toPOST/api/groups/{groupId}/members/{userId}Add member to group by user IDPOST/api/groups/{groupId}/members/by-emailAdd member to group by emailPOST/api/expensesAdd expense (splits among participants)GET/api/groups/{groupId}/settlementsGet minimum settlement plan for a groupGET/api/groups/{groupId}/summaryGet cached group summary (total + settlements)GET/api/users/{userId}/balancesGet all balances (owed/owes) for a user across all groupsPOST/api/recurring-expensesCreate a recurring expense (e.g., monthly rent)GET/api/activity-logs/{entityType}/{entityId}View audit log for an entity
Running Locally
Prerequisites

Java 17+
MySQL 8+
Maven

Setup

Create the database:

sqlCREATE DATABASE hisaab;

Set the DB password as an environment variable (do not hardcode in application.properties):

DB_PASSWORD=your_mysql_password
In IntelliJ: Run → Edit Configurations → Environment Variables.

Run the application:

bashmvn spring-boot:run

Open browser: http://localhost:8080

Testing
Run the concurrency test:
bashmvn test -Dtest=ConcurrencyTest
This uses CountDownLatch to force two threads to read and update the same Balance row simultaneously exactly one should succeed, and one should fail with ObjectOptimisticLockingFailureException, with the row's version incrementing by exactly 1.
What I'd Improve Next

Move JWT secret to an environment variable (currently regenerated on each app restart)
Switch from in-memory cache to Redis for distributed caching
Add custom split ratios (currently equal-split only)
Migrate to Flyway for schema versioning instead of ddl-auto=update
Add pagination for activity logs and expense lists
Replace sessionStorage JWT storage with httpOnly cookies for better XSS protection
Deploy to Render/Railway with a hosted MySQL instance for a live demo link

Author
Shatakshi
