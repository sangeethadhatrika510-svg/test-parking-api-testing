# Parking Microservices Architecture

This document explains the Parking Platform architecture based on the developer services in `D:\Project\Parking`.

## 1. Platform Architecture

```mermaid
flowchart LR
    Client["Client<br/>Postman / Web / Rest Assured"]
    Gateway["API Gateway<br/>Port 8080"]
    Auth["Auth Service<br/>Port 8082"]
    API["API Service<br/>Port 8081"]
    Location["Location Service<br/>Port 8083"]
    Booking["Booking Service<br/>Port 8084"]
    Payment["Payment Service<br/>Port 8085"]

    AuthDB[("Auth Database")]
    LocationDB[("Location Database")]
    BookingDB[("Booking Database")]
    PaymentDB[("Payment Database")]

    Client --> Gateway

    Gateway -->|/auth/**| Auth
    Gateway -->|/api/**| API
    Gateway -->|Location routes| Location
    Gateway -->|/bookings/** and /sessions/**| Booking
    Gateway -->|Payment routes| Payment

    Auth --> AuthDB
    Location --> LocationDB
    Booking --> BookingDB
    Payment --> PaymentDB

    Auth -.->|Issues JWT| Client
    Client -.->|Bearer JWT| Gateway
```

The gateway is the preferred API entry point. It validates JWTs, adds or propagates correlation IDs, applies rate limiting, and routes requests to the correct service.

## 2. Authentication Flow

```mermaid
sequenceDiagram
    autonumber

    participant User
    participant Gateway
    participant Auth as Auth Service
    participant DB as Auth Database
    participant Service as Protected Service

    User->>Gateway: POST /auth/register
    Gateway->>Auth: Forward registration
    Auth->>DB: Insert user and roles
    DB-->>Auth: User created
    Auth-->>User: 201 Created

    User->>Gateway: POST /auth/login
    Gateway->>Auth: Forward credentials
    Auth->>DB: Find user and verify password
    DB-->>Auth: User and roles
    Auth-->>User: JWT access token

    User->>Gateway: Protected request with Bearer JWT
    Gateway->>Gateway: Validate JWT
    Gateway->>Service: Forward authenticated request
    Service->>Service: Check required role
    Service-->>User: API response
```

There are two authorization levels:

- The gateway confirms that the JWT is valid.
- The target service checks whether the user has the required role.

## 3. Parking Domain Structure

```mermaid
erDiagram
    USER ||--o{ BOOKING : creates
    USER ||--o{ PARKING_SESSION : starts

    PARKING_ZONE ||--o{ PARKING_LOCATION : contains
    PARKING_LOCATION ||--o{ PARKING_SPACE : contains
    PARKING_LOCATION ||--o{ CAMERA_DEVICE : operates

    BOOKING }o--|| PARKING_LOCATION : references
    BOOKING }o--o| PARKING_SPACE : reserves
    BOOKING ||--o| PARKING_SESSION : starts

    PARKING_SESSION }o--|| PARKING_LOCATION : occurs_at
    PARKING_SESSION }o--o| PARKING_SPACE : occupies

    USER ||--o{ PAYMENT_TRANSACTION : makes
    PAYMENT_TRANSACTION }o--o| BOOKING : pays_for
    PAYMENT_TRANSACTION }o--o| PARKING_SESSION : pays_for
    MOCK_CARD ||--o{ PAYMENT_TRANSACTION : funds
```

A booking and a parking session are different:

- A **booking** reserves parking for a future period.
- A **parking session** represents actual parking activity.
- A session can start without a booking, from a confirmed booking, or from a mock camera plate event.

## 4. Complete Parking Flow

```mermaid
sequenceDiagram
    autonumber

    participant QA as User / QA Test
    participant Auth
    participant Location
    participant Booking
    participant Payment
    participant DB as PostgreSQL

    QA->>Auth: Register and login
    Auth-->>QA: JWT

    QA->>Location: POST /zones
    Location->>DB: Insert zone

    QA->>Location: POST /parking-locations
    Location->>DB: Insert location

    QA->>Location: POST /parking-locations/{id}/spaces
    Location->>DB: Insert available space

    QA->>Booking: POST /bookings
    Booking->>DB: Insert REQUESTED booking

    QA->>Booking: POST /bookings/{id}/confirm
    Booking->>DB: Update to CONFIRMED

    QA->>Booking: POST /sessions/start-with-booking/{id}
    Booking->>DB: Insert ACTIVE session

    QA->>Payment: POST /test-cards
    Payment->>DB: Insert mock card

    QA->>Payment: POST /payments/intent
    Payment->>DB: Insert CREATED payment

    QA->>Payment: POST /payments/{id}/confirm
    Payment->>DB: Debit card and mark SUCCEEDED

    QA->>Booking: POST /sessions/{id}/end
    Booking->>DB: Mark session ENDED
```

## 5. Parking Session Paths

```mermaid
flowchart TD
    Start{"How does parking begin?"}

    AdHoc["POST /sessions/start"]
    Booking["POST /sessions/start-with-booking/{bookingId}"]
    Camera["POST /sessions/start-by-plate"]

    ValidateBooking{"Booking is CONFIRMED?"}
    Reject["400 Bad Request"]
    Active["Create ACTIVE session"]

    Search["GET /sessions/active<br/>or<br/>GET /sessions/by-vehicle/{plate}"]
    End["POST /sessions/{sessionId}/end"]
    Ended["Session status = ENDED<br/>endedAt is recorded"]

    Start -->|No booking| AdHoc
    Start -->|Confirmed booking| Booking
    Start -->|Camera plate event| Camera

    Booking --> ValidateBooking
    ValidateBooking -->|No| Reject
    ValidateBooking -->|Yes| Active

    AdHoc --> Active
    Camera --> Active

    Active --> Search
    Active --> End
    End --> Ended
```

The booking service provides these parking-session APIs:

- `POST /sessions/start`
- `POST /sessions/start-with-booking/{bookingId}`
- `POST /sessions/start-by-plate`
- `POST /sessions/{sessionId}/end`
- `GET /sessions/active`
- `GET /sessions/by-vehicle/{registrationNumber}`

## 6. Automated Testing Architecture

```mermaid
flowchart LR
    Feature["Cucumber Feature Files<br/>Gherkin scenarios and tags"]
    Runner["TestNG Cucumber Runner"]
    Steps["Step Definitions"]
    APIClient["Rest Assured API Client"]
    DBClient["JDBC DB Client"]
    Tunnel["SSH Tunnel<br/>Local port 15432"]
    EC2["EC2 Instance"]
    RDS[("Private RDS PostgreSQL")]
    Services["Parking APIs"]
    Reports["Allure + Cucumber HTML<br/>+ Surefire XML"]
    Actions["GitHub Actions"]

    Actions --> Runner
    Runner --> Feature
    Feature --> Steps
    Steps --> APIClient
    Steps --> DBClient

    APIClient --> Services
    DBClient --> Tunnel
    Tunnel --> EC2
    EC2 --> RDS

    Runner --> Reports
```

The automated test execution path is:

1. Cucumber selects scenarios using tags such as `@smoke`, `@regression`, and `@db`.
2. TestNG runs the selected Cucumber scenarios.
3. Step definitions call the APIs through Rest Assured.
4. Database verification steps connect through JDBC.
5. For private RDS access, Maven opens an SSH tunnel through EC2.
6. Allure, Cucumber HTML, and Surefire reports are generated.
7. GitHub Actions runs the test suites and stores or publishes the reports.

## 7. Service Responsibilities

| Service | Port | Main responsibility |
| --- | ---: | --- |
| Gateway | 8080 | Routing, JWT validation, correlation IDs, rate limiting, and fallback responses |
| API | 8081 | Public status and secured example APIs |
| Auth | 8082 | Registration, login, roles, and JWT generation |
| Location | 8083 | Zones, locations, spaces, availability, cameras, and plate events |
| Booking | 8084 | Future bookings and active parking sessions |
| Payment | 8085 | Mock cards, payment rules, payment confirmation, declines, and refunds |

## 8. Important QA Boundaries

The following boundaries should be covered by automated tests:

- Public endpoint success without a JWT.
- `401 Unauthorized` when a protected endpoint is called without a valid JWT.
- `403 Forbidden` when the user has a valid JWT but an insufficient role.
- Successful requests for every allowed role.
- Validation failures for missing, blank, malformed, past-date, and negative values.
- `404 Not Found` behavior for unknown resource IDs.
- Booking and session state transitions.
- Payment success, decline, and refund behavior.
- API response verification against inserted or updated database records.
- Gateway routing, correlation ID, fallback, and rate-limit behavior.
