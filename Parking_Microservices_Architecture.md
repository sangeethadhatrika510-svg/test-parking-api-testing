# Parking Services Architecture

This document explains the implemented Parking Platform microservices, their responsibilities, data ownership, security model, and business workflows. It is intended to help the testing team understand what must be tested independently and across services.

## 1. Microservices Overview

```mermaid
flowchart LR
    Client["Client Application<br/>or Automated Test"]

    API["API Service<br/>http://localhost:8081<br/>Status and secured sample"]
    Auth["Auth Service<br/>http://localhost:8082<br/>Register, login, JWT"]
    Location["Location Service<br/>http://localhost:8083<br/>Parking inventory"]
    Booking["Booking Service<br/>http://localhost:8084<br/>Bookings and sessions"]
    Payment["Payment Service<br/>http://localhost:8085<br/>Mock payment processing"]

    DB[("PostgreSQL<br/>parking_db")]

    Client -->|apiBaseUrl| API
    Client -->|authBaseUrl| Auth
    Client -->|locationBaseUrl| Location
    Client -->|bookingBaseUrl| Booking
    Client -->|paymentBaseUrl| Payment

    Auth --> DB
    Location --> DB
    Booking --> DB
    Payment --> DB

    classDef sample fill:#f4f4f4,stroke:#777,stroke-dasharray:5 5,color:#333;
    class API sample;
```

The platform contains **five directly accessible microservices**. Auth, Location, Booking, and Payment form the core parking workflow. The API service is a small independent service used to demonstrate public and JWT-protected endpoints.

## 2. Authentication and Direct JWT Use

```mermaid
sequenceDiagram
    autonumber

    actor Client
    participant Auth as Auth Service :8082
    participant AuthDB as PostgreSQL
    participant API as API Service :8081
    participant Domain as Location / Booking / Payment

    Client->>Auth: POST /auth/register
    Auth->>AuthDB: Insert users and user_roles
    AuthDB-->>Auth: User stored
    Auth-->>Client: 201 user response

    Client->>Auth: POST /auth/login
    Auth->>AuthDB: Read user and roles
    Auth->>Auth: Verify password and sign JWT
    Auth-->>Client: Bearer access token

    Client->>API: GET /api/secure/hello + JWT
    API->>API: Validate JWT locally
    API-->>Client: Authorized sample response

    Client->>Domain: Protected request + JWT
    Domain->>Domain: Validate JWT and required roles locally
    Domain-->>Client: API response
```

The client sends the token directly to protected APIs using `Authorization: Bearer <accessToken>`.

The protected services validate the JWT themselves using the configured shared secret. They do not call Auth for every request.

## 3. API Service Scope

```mermaid
flowchart LR
    Client["Client"] --> Status["GET :8081/status"]
    Status --> PublicResponse["Public service status"]

    Client --> Secure["GET :8081/api/secure/hello<br/>Bearer JWT required"]
    Secure --> Validation{"JWT valid?"}
    Validation -->|Yes| Authorized["Return username and<br/>authorization message"]
    Validation -->|No| Unauthorized["401 Unauthorized"]

    Secure -.-> ParkingFlow["No location, booking,<br/>session or payment operations"]
```

The controller provides `GET /status`, `GET /api/status`, `GET /api/secure/hello`, and `GET /api/secure/sample`.

## 4. Shared Database and Table Ownership

```mermaid
flowchart TB
    Auth["Auth Service :8082"] --> AuthTables["users<br/>user_roles"]
    Location["Location Service :8083"] --> LocationTables["parking_zones<br/>parking_locations<br/>parking_spaces<br/>camera_devices"]
    Booking["Booking Service :8084"] --> BookingTables["parking_bookings<br/>parking_sessions"]
    Payment["Payment Service :8085"] --> PaymentTables["mock_cards<br/>payment_rules<br/>payment_transactions"]

    AuthTables --> DB[("Shared PostgreSQL database<br/>parking_db")]
    LocationTables --> DB
    BookingTables --> DB
    PaymentTables --> DB

    API["API Service :8081"] -.-> NoTables["No datasource and<br/>no domain tables"]
```

The four stateful services use the same configured PostgreSQL database. They own different groups of tables inside that database.

## 5. Location Service Flow

```mermaid
flowchart TD
    Client["Client + JWT"] --> Zone["POST :8083/zones"]
    Zone --> ZoneId["Save zoneId"]

    ZoneId --> Location["POST :8083/parking-locations"]
    Location --> LocationId["Save locationId"]

    LocationId --> Space["POST :8083/parking-locations/{locationId}/spaces"]
    Space --> SpaceId["Save spaceId"]

    SpaceId --> Update["PATCH :8083/spaces/{spaceId}/status"]
    Update --> LocationAvailability["GET location availability"]
    Update --> ZoneAvailability["GET zone availability"]

    LocationId --> Camera["POST location camera"]
    Camera --> CameraId["Save cameraId"]
    CameraId --> Scan["POST multipart plate event"]
    Scan --> Plate["Receive random mock<br/>registration number"]
```

The availability endpoints calculate totals from the location and space records stored by the Location service.

## 6. Booking and Parking Session Flow

```mermaid
flowchart TD
    Client["Client + JWT"] --> CreateBooking["POST :8084/bookings"]
    CreateBooking --> Requested["Booking status REQUESTED<br/>Save bookingId"]

    Requested --> Confirm["POST /bookings/{bookingId}/confirm"]
    Confirm --> Confirmed["Booking status CONFIRMED"]
    Requested --> Cancel["POST /bookings/{bookingId}/cancel"]
    Cancel --> Cancelled["Booking status CANCELLED"]

    Confirmed --> StartWithBooking["POST /sessions/start-with-booking/{bookingId}"]
    Client --> AdHoc["POST /sessions/start"]
    Client --> ByPlate["POST /sessions/start-by-plate"]

    StartWithBooking --> Active["Session status ACTIVE<br/>Save sessionId"]
    AdHoc --> Active
    ByPlate --> Active

    Active --> ActiveQuery["GET /sessions/active"]
    Active --> VehicleQuery["GET /sessions/by-vehicle/{registrationNumber}"]
    Active --> End["POST /sessions/{sessionId}/end"]
    End --> Ended["Session status ENDED"]
```

The client passes `locationId` and optional `spaceId` into booking or session payloads. The Booking service does not call the Location service to obtain them.

## 7. Payment Service Flow

```mermaid
flowchart TD
    Client["Client + JWT"] --> Card["POST :8085/test-cards"]
    Card --> CardId["Card status ACTIVE<br/>Save cardId"]

    CardId --> Balance["PATCH card balance"]
    CardId --> Expire["POST expire card"]
    CardId --> Block["POST block card"]

    Client --> Rule["POST /payment-rules"]
    Rule --> RuleList["GET /payment-rules"]

    CardId --> Intent["POST /payments/intent"]
    Intent --> Created["Payment status CREATED<br/>Save paymentId"]
    Created --> Confirm["POST /payments/{paymentId}/confirm"]

    Confirm --> Decision{"Card, rules and balance valid?"}
    Decision -->|Yes| Success["SUCCEEDED<br/>Debit mock-card balance"]
    Decision -->|No| Declined["DECLINED<br/>Store failure reason"]

    Success --> Refund["POST /payments/{paymentId}/refund"]
    Refund --> Refunded["REFUNDED<br/>Restore mock-card balance"]

    Success --> Queries["GET all, mine or by paymentId"]
    Declined --> Queries
    Refunded --> Queries
```

The payment implementation is a mock system for QA. It does not communicate with a real payment provider.

## 8. End-to-End Parking Business Journey

```mermaid
sequenceDiagram
    autonumber

    actor Client
    participant Auth as Auth :8082
    participant Location as Location :8083
    participant Booking as Booking :8084
    participant Payment as Payment :8085
    participant DB as Shared parking_db

    Client->>Auth: Register and login
    Auth->>DB: Store/read user
    Auth-->>Client: JWT

    Client->>Location: Create zone
    Location->>DB: Insert parking_zones
    Location-->>Client: zoneId

    Client->>Location: Create location with zoneId
    Location->>DB: Insert parking_locations
    Location-->>Client: locationId

    Client->>Location: Create space with locationId
    Location->>DB: Insert parking_spaces
    Location-->>Client: spaceId

    Client->>Booking: Create booking with locationId and spaceId
    Booking->>DB: Insert REQUESTED booking
    Booking-->>Client: bookingId

    Client->>Booking: Confirm booking
    Booking->>DB: Set CONFIRMED

    Client->>Booking: Start session with bookingId
    Booking->>DB: Insert ACTIVE session
    Booking-->>Client: sessionId

    Client->>Payment: Create mock card
    Payment->>DB: Insert mock_cards
    Payment-->>Client: cardId

    Client->>Payment: Create intent with cardId and bookingId/sessionId
    Payment->>DB: Insert CREATED transaction
    Payment-->>Client: paymentId

    Client->>Payment: Confirm payment
    Payment->>DB: Update payment and card balance

    Client->>Booking: End session with sessionId
    Booking->>DB: Set ENDED
```

The client coordinates this journey. A response ID from one service becomes input to a later direct request to another service.

## 9. Logical Cross-Service References

```mermaid
erDiagram
    PARKING_ZONE ||--o{ PARKING_LOCATION : contains
    PARKING_LOCATION ||--o{ PARKING_SPACE : contains
    PARKING_LOCATION ||--o{ CAMERA_DEVICE : has

    USER ||--o{ BOOKING : creates
    PARKING_LOCATION ||--o{ BOOKING : locationId
    PARKING_SPACE ||--o{ BOOKING : optional_spaceId

    BOOKING ||--o| PARKING_SESSION : optional_bookingId
    PARKING_LOCATION ||--o{ PARKING_SESSION : locationId
    PARKING_SPACE ||--o{ PARKING_SESSION : optional_spaceId

    USER ||--o{ PAYMENT_TRANSACTION : creates
    MOCK_CARD ||--o{ PAYMENT_TRANSACTION : cardId
    BOOKING ||--o{ PAYMENT_TRANSACTION : optional_bookingId
    PARKING_SESSION ||--o{ PAYMENT_TRANSACTION : optional_sessionId
```

This diagram represents logical relationships visible in request payloads and entity fields. It does not imply that every relationship is enforced by a database foreign key.

## 10. Automated Testing Path

```mermaid
flowchart LR
    Trigger["Developer / GitHub Actions"] --> Maven["Maven"]
    Maven --> TestNG["TestNG Cucumber runner"]
    TestNG --> Features["Gherkin features selected by tags"]
    Features --> Steps["Step definitions"]

    Steps --> RestAssured["Rest Assured"]
    RestAssured --> API["API :8081"]
    RestAssured --> Auth["Auth :8082"]
    RestAssured --> Location["Location :8083"]
    RestAssured --> Booking["Booking :8084"]
    RestAssured --> Payment["Payment :8085"]

    Steps --> JDBC["JDBC verification"]
    JDBC --> Tunnel["Optional SSH tunnel<br/>localhost:15432"]
    Tunnel --> EC2["EC2 instance"]
    EC2 --> RDS[("Private RDS<br/>parking_db")]

    TestNG --> Reports["Cucumber HTML / JSON<br/>Surefire XML<br/>Allure results"]
```

Rest Assured tests communicate directly with the service base URLs. For AWS, use the configured EC2 service host with the appropriate service port.

## 11. Service Responsibilities

| Service | Direct port | Business role | Responsibility |
| --- | ---: | --- | --- |
| API | 8081 | Supporting sample | Public status and secured JWT demonstration endpoint |
| Auth | 8082 | Core security | Registration, login, roles, and JWT generation |
| Location | 8083 | Core parking inventory | Zones, locations, spaces, availability, cameras, and plate events |
| Booking | 8084 | Core parking operation | Bookings and parking sessions |
| Payment | 8085 | Core payment operation | Mock cards, rules, payment intent, confirmation, decline, and refund |

## 12. QA Implications

- Configure and test a separate base URL for each service.
- Obtain the JWT from Auth and send it directly to every protected service.
- Treat the API service as an independent security check, not a required parking workflow step.
- Capture resource IDs from responses and pass them into later service requests.
- Verify writes against the correct table group in the shared `parking_db`.
- Test invalid cross-service IDs because services do not call one another to validate every reference.
- Test `401`, `403`, validation, not-found, ownership, and state-transition behavior directly on each service.
