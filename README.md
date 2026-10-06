# CampusSphere

**Student Resource Exchange & Collaboration Platform**
A Java Programming course project — a centralized, verified-student platform for buying/selling academic resources, offering freelance help, sharing senior guidance, and reporting lost & found items.

**Status: feature-complete.** All planned modules are implemented, integrated, and tested.

---

## Features

### Authentication & Profile
- College-email-restricted registration, session-based login/logout (Spring Security)
- BCrypt password hashing
- Editable user profile: name, department, year, profile picture, skills, about-me bio, contact details

### Marketplace
- Post, browse, search, and filter Buy & Sell listings (books, electronics, calculators, academic materials)
- Item condition, price, contact info, image upload, and status tracking (Available / Reserved / Sold)
- Owner-only edit/delete, enforced in the service layer

### Freelance Hub
- Post and discover freelance services (record writing, PPT/poster/resume design, coding help, video editing, and more)
- Fixed or "starting from" pricing, sample-work image, availability status (Available / Busy / Not Accepting)

### Senior Guidance Hub
- Share and browse guidance on internships, placements, subjects, hackathons, careers, certifications, and higher studies
- Optional year/department targeting metadata, visibility control (Published / Hidden)

### Lost & Found Portal
- Report lost or found items with category, location, date, and photo
- Status tracking (Open / Claimed / Closed) with transparent browsing of resolved posts

### Notification Center
- In-app notification feed with unread-count badge shown on every page
- Notifies content owners when an administrator moderates one of their posts

### Global Search
- Single search bar across all four content modules at once, with newest/oldest sorting

### Admin Dashboard
- Platform-wide statistics (users, listings, services, guidance posts, lost & found posts)
- View all registered users
- Tabbed cross-module content moderation with one-click removal (owner is notified automatically)

### Professional UI
- Left sidebar navigation (collapses to an off-canvas drawer on mobile) with a slim top bar and notification bell
- Consistent Bootstrap 5 design system shared across every module via CSS custom properties
- Responsive grid layout throughout

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot, Spring MVC |
| Security | Spring Security (session-based auth, BCrypt, method security) |
| Data Access | Spring Data JPA (Hibernate) |
| Database | MySQL |
| Build | Maven |
| Frontend | Thymeleaf, HTML5, CSS3, Bootstrap 5, vanilla JavaScript |
| Testing | JUnit 5, Mockito |

---

## Installation

### Prerequisites
- Java 17 (JDK)
- Maven 3.9+
- MySQL 8.x running locally

### Setup

1. Create a MySQL user/password with access to create databases, or ensure `campussphere_db` can be created automatically.
2. Open `src/main/resources/application.properties` and update:
   - `spring.datasource.username`
   - `spring.datasource.password`
   - `campussphere.security.allowed-email-domain` (set to your institution's actual email domain)
3. From the project root, run:
   ```
   mvn spring-boot:run
   ```
4. Visit `http://localhost:8080`.
5. Register a normal student account through the UI. There is no admin self-registration by design (matching the project's original scope: admins are appointed, not self-registered) — to test the Admin Dashboard, register a user normally, then manually update that row's `role` column to `ADMIN` in MySQL:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE email = 'your.email@yourcollege.edu';
   ```
   Log out and back in for the role change to take effect (Spring Security reads authorities at login).

---

## Routes Reference

| Feature | Route |
|---|---|
| Landing page | `GET /` |
| Registration page / API | `GET /register` · `POST /api/auth/register` |
| Login / Logout | `POST /login` · `POST /logout` |
| Dashboard (stats + recent activity) | `GET /dashboard` |
| View / Edit Profile | `GET /profile` · `GET`,`POST /profile/edit` |
| Notification Center | `GET /notifications` · `POST /notifications/{id}/read` |
| Global Search | `GET /search?keyword=&sort=` |
| Marketplace | `GET /marketplace?category=&keyword=` · `/marketplace/create` · `/marketplace/{id}` · `/marketplace/{id}/edit` · `/marketplace/{id}/delete` · `/marketplace/my-listings` |
| Freelance Hub | `GET /freelance?category=&keyword=` · `/freelance/create` · `/freelance/{id}` · `/freelance/{id}/edit` · `/freelance/{id}/delete` · `/freelance/my-services` |
| Senior Guidance | `GET /guidance?category=&keyword=` · `/guidance/create` · `/guidance/{id}` · `/guidance/{id}/edit` · `/guidance/{id}/delete` · `/guidance/my-guidance` |
| Lost & Found | `GET /lostfound?postType=&category=&status=&keyword=` · `/lostfound/create` · `/lostfound/{id}` · `/lostfound/{id}/edit` · `/lostfound/{id}/delete` · `/lostfound/my-posts` |
| Admin Dashboard | `GET /admin` (ROLE_ADMIN only) |
| Admin - Users | `GET /admin/users` |
| Admin - Moderate Content | `GET /admin/content?module=` · `POST /admin/content/{module}/{id}/delete` |
| Uploaded files | `GET /uploads/{marketplace\|freelance\|guidance\|lostfound\|profile}/{filename}` |

---

## Project Structure

```
com.campussphere
├── config/            → SecurityConfig, WebMvcConfig
├── common/
│   ├── dto/             → ApiResponse
│   ├── exception/         → shared exceptions + GlobalExceptionHandler
│   ├── service/            → FileStorageService (shared across every module)
│   └── util/               → college email validation
├── auth/
│   ├── entity/          → User, Role
│   ├── repository/        → UserRepository
│   ├── dto/                 → UserRegisterDTO, UserProfileDTO, ProfileUpdateDTO
│   ├── service/               → UserService, CustomUserDetailsService
│   └── controller/              → AuthController, PageController, ProfileController
├── marketplace/       → entity, repository, dto, service (MarketplaceListingService), controller
├── freelance/         → entity, repository, dto, service (FreelanceServiceManager), controller
├── guidance/          → entity, repository, dto, service (GuidanceServiceManager), controller
├── lostfound/         → entity, repository, dto, service (LostFoundServiceManager), controller
├── notification/      → entity, repository, dto, service, controller (+ GlobalNotificationAttributeAdvice)
├── admin/             → dto, service (AdminService), controller
└── search/            → dto, controller (composes the four module services, no new query logic)
```

Every content module (`marketplace`, `freelance`, `guidance`, `lostfound`) follows the identical internal shape: `entity` (JPA entity + enums), `repository` (Spring Data JPA), `dto` (Create/Update/Response), `service` (business logic + ownership enforcement), `controller` (MVC, server-rendered). This consistency is deliberate — once you understand one module's structure, you understand all four.

---

## Architecture Notes

- **Layered MVC, strictly followed:** Controller → Service → Repository, with DTOs at every controller boundary. No entity is ever serialized directly to a template.
- **Ownership enforcement lives in the service layer**, not the controller or the UI — every module's edit/delete throws `UnauthorizedActionException` for a non-owner, verified by unit tests, not just hidden behind a UI button.
- **Shared infrastructure, not duplicated per module:** `FileStorageService` (image/file uploads), the exception hierarchy, and `ApiResponse` are all defined once in `common` and reused by every content module.
- **Admin moderation reuses each module's own service**, rather than a generic/abstracted "content" layer — `AdminService` composes `MarketplaceListingService`, `FreelanceServiceManager`, `GuidanceServiceManager`, and `LostFoundServiceManager` directly, keeping each module the single source of truth for its own data.
- **Naming note:** `FreelanceServiceManager`, `GuidanceServiceManager`, and `LostFoundServiceManager` use "Manager" rather than the stricter `[Entity]Service` convention (e.g. `MarketplaceListingService`) specifically to avoid an ambiguous `FreelanceServiceService`-style name next to Spring's own `@Service` stereotype — documented in each file's Javadoc.

---

## Testing

Unit tests (JUnit 5 + Mockito, no database required) cover the ownership-enforcement rule in every content module, plus registration, profile updates, notifications, and admin moderation:

```
mvn test
```

---

## Screenshots

*(Add screenshots here before final submission/demo — recommended: landing page, dashboard with sidebar, a module browse page, a create-listing form, the admin dashboard, and the mobile sidebar drawer.)*

| Page | Screenshot |
|---|---|
| Landing Page | _add screenshot_ |
| Dashboard | _add screenshot_ |
| Marketplace Browse | _add screenshot_ |
| Create Listing Form | _add screenshot_ |
| Admin Dashboard | _add screenshot_ |
| Mobile View | _add screenshot_ |

---

## Future Enhancements

Deliberately out of scope for this submission, in rough priority order:

- **Interaction/messaging system** — structured buyer–seller requests and in-app chat (would also unlock richer notification triggers beyond admin moderation)
- **Ratings & Reviews**, tied to a completed interaction
- **Notes Sharing, Project Collaboration, Internship Board, Event Announcements** (per the original project blueprint's Future Scope)
- **Per-module advanced sort controls** (price, popularity) beyond Global Search's newest/oldest
- **Email/SMS notification delivery**, in addition to the current in-app notification center
- **Swagger/OpenAPI documentation** for a future REST API layer, if a mobile client is ever built
- **Payment gateway integration** for Marketplace transactions

---

## Development Journal (Phase History)

*The sections below document what was built and verified in each development phase, including real bugs caught during the process. Kept for transparency and as a viva/demo reference — the summary above reflects the finished, current state of the project.*

### Phase 1 — Foundation
Registration, login/logout, college-email validation, BCrypt hashing, base UI shell. Reviewed and fixed after initial build: a CSRF misconfiguration that silently rejected every registration attempt with `403 Forbidden`, two dead files (`UserLoginDTO`, `InvalidRequestException`), a duplicate default-role assignment, and unused `xmlns:sec` declarations.

### Phase 2 — Buy & Sell Marketplace
First content module. Established the shared `FileStorageService` pattern (local image storage outside the classpath, served via `/uploads/**`) and the ownership-enforcement pattern every later module follows.

### Phase 3 — Freelance Hub
Second content module, built to the identical shape as Marketplace. Introduced the `Manager` naming convention for business-logic classes whose entity name would otherwise force an ambiguous `XxxServiceService` name.

### Phase 4 — Senior Guidance Hub
Third content module. Introduced optional targeting metadata (relevant year/department) and a simpler two-state visibility toggle, appropriate since guidance posts have no transactional lifecycle to track.

### Phase 5 — Lost & Found Portal
Fourth and final content module. Deliberately different browsing behavior from the other three: defaults to showing OPEN posts, but lets a status be explicitly requested so a student can check whether an item was already claimed — genuinely useful here in a way it isn't for a sold marketplace item. One real bug caught during development: an early draft used the inert HTML5 `<template>` tag to wrap a Thymeleaf loop, which would have made a set of radio buttons never render; fixed with Thymeleaf's `th:block`.

### Final Phase — Professionalization
Added the User Profile module, Notification Center, Admin Dashboard, Global Search, dashboard statistics/recent-activity, and the sidebar-based UI revamp — all layered on top of the four existing content modules without altering their core behavior. Two real issues caught and fixed during this phase: an inline `<script>` inside a Thymeleaf fragment that would never have actually been included on any page (fragments only extract the tagged element itself, not its siblings), and a pre-existing unused import in `CustomUserDetailsService.java` left over since Phase 1. Every module service gained an `adminDelete`/`getAllForAdmin` pair to support moderation, without changing any existing method's signature or behavior.
