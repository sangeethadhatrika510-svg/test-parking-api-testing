# Parking Microservices Architecture

This document describes the architecture implemented by the service code under `D:\Project\Parking`.

## Review Conclusion

The `parking-api-service` is deployed and routed by the gateway, but it is not part of the current parking business workflow. It provides only:

- `GET /status` and `GET /api/status`
- `GET /api/secure/sample` and `GET /api/secure/hello`

These endpoints demonstrate status and JWT-protected access. Auth, location, booking, session, and payment operations do not call this service. The diagrams therefore show it as an optional demonstration service rather than a core parking service.

The code also shows these important architectural facts:

- The client or automated test orchestrates the workflow by calling each service.
- The domain services do not make HTTP calls to one another.
- Auth, Location, Booking, and Payment use the same PostgreSQL `parking_db` database.
- Each domain service owns a different group of tables inside that shared database.
- IDs such as `locationId`, `bookingId`, and `sessionId` are passed between APIs, but the services do not currently verify them by calling one another.

## 1. Actual Runtime Architecture

```mermaid
flowchart LR
    Client["Client<br/>Application / Postman / Rest Assured"]
    Gateway["Parking Gateway<br/>Port 8080"]

    subgraph Core["Core parking services"]
        Auth["Auth Service<br/>Port 8082"]
        Location["Location Service<br/>Port 8083"]
        Booking["Booking Service<br/>Port 8084"]
        Payment["Payment Service<br/>Port 8085"]
    end

    API["API Service<br/>Port 8081<br/>Secured sample only"]
    DB[("PostgreSQL / RDS<br/>parking_db")]

    Client -->|Preferred entry point| Gateway
    Gateway --> Auth
    Gateway --> Location
    Gateway --> Booking
    Gateway --> Payment

    Gateway -.->|Optional /api demonstration routes| API

    Auth -->|users and user_roles| DB
    Location -->|zones, locations, spaces, cameras| DB
    Booking -->|bookings and sessions| DB
    Payment -->|cards, rules, transactions| DB

    classDef optional fill:#f4f4f4,stroke:#777,stroke-dasharray:5 5,color:#333;
    class API optional;
```

The API service is still a runnable service and gateway route. The dashed connection means it is not used by the core parking journey.

## 2. Gateway Routing

```mermaid
flowchart TB
    Request["Incoming request"] --> Gateway["Gateway :8080"]

    Gateway --> Filters["Global filters"]
    Filters --> Correlation["Create or propagate<br/>X-Correlation-Id"]
    Filters --> Headers["Add gateway and<br/>original-path headers"]
    Filters --> Limit["In-memory rate limit<br/>120 requests/minute by default"]
    Filters --> JWT["Validate Bearer JWT<br/>for protected routes"]

    JWT --> Router{"Route by path"}
    Router -->|/auth/**| Auth["Auth :8082"]
    Router -->|/zones/**<br/>/parking-locations/**<br/>/spaces/**<br/>/cameras/**| Location["Location :8083"]
    Router -->|/bookings/**<br/>/sessions/**| Booking["Booking :8084"]
    Router -->|/test-cards/**<br/>/payment-rules/**<br/>/payments/**| Payment["Payment :8085"]
    Router -.->|/api/**| API["API :8081<br/>sample endpoints"]

    Location --> Resilience["GET retry and circuit breaker"]
    Booking --> Resilience
    Payment --> Resilience
    API -.-> Resilience
    Auth --> Circuit["Circuit breaker"]

    Resilience --> Response["Response to client"]
    Circuit --> Response
```

The gateway retries only `GET` requests for selected upstream failure statuses. Circuit-breaker fallback responses are produced when a downstream service is unavailable.

## 3. Authentication and Authorization

```mermaid
sequenceDiagram
    autonumber

    actor User
    participant Gateway
    participant Auth as Auth Service
    participant DB as PostgreSQL
    participant Domain as Location / Booking / Payment

    User->>Gateway: POST /auth/register
    Gateway->>Auth: Route public request
    Auth->>DB: Insert users and user_roles
    DB-->>Auth: User stored
    Auth-->>User: 201 user response

    User->>Gateway: POST /auth/login
    Gateway->>Auth: Route public request
    Auth->>DB: Read user and roles
    Auth->>Auth: Verify encoded password
    Auth->>Auth: Sign JWT using shared secret
    Auth-->>User: Bearer access token

    User->>Gateway: Protected request + JWT
    Gateway->>Gateway: Validate signature and expiry
    Gateway->>Domain: Forward JWT request
    Domain->>Domain: Validate JWT again
    Domain->>Domain: Apply method role rule
    Domain-->>User: 2xx, 401, or 403 response
```

The services share the JWT signing secret through environment configuration. They do not call Auth to validate every request; each protected service validates the token locally.

## 4. Shared Database and Table Ownership

```mermaid
flowchart TB
    DB[("parking_db<br/>PostgreSQL locally or RDS on AWS")]

    Auth["Auth Service"] --> AuthTables["users<br/>user_roles"]
    Location["Location Service"] --> LocationTables["parking_zones<br/>parking_locations<br/>parking_spaces<br/>camera_devices"]
    Booking["Booking Service"] --> BookingTables["parking_bookings<br/>parking_sessions"]
    Payment["Payment Service"] --> PaymentTables["mock_cards<br/>payment_rules<br/>payment_transactions"]

    AuthTables --> DB
    LocationTables --> DB
    BookingTables --> DB
    PaymentTables --> DB

    API["API Service"] -.-> NoDB["No datasource or domain tables"]
```

This is a shared-database architecture, not a separate database per microservice. The service code separates table responsibilities, but all four stateful services connect to the same configured database.

## 5. Current Business Workflow

```mermaid
sequenceDiagram
    autonumber

    actor Client
    participant Gateway
    participant Auth
    participant Location
    participant Booking
    participant Payment
    participant DB as Shared parking_db

    Client->>Gateway: Register and login
    Gateway->>Auth: /auth/**
    Auth->>DB: Store/read user
    Auth-->>Client: JWT

    Client->>Gateway: Create zone
    Gateway->>Location: POST /zones
    Location->>DB: Insert parking_zones

    Client->>Gateway: Create location
    Gateway->>Location: POST /parking-locations
    Location->>DB: Insert parking_locations

    Client->>Gateway: Create parking space
    Gateway->>Location: POST /parking-locations/{id}/spaces
    Location->>DB: Insert parking_spaces

    Client->>Gateway: Create booking using locationId and spaceId
    Gateway->>Booking: POST /bookings
    Booking->>DB: Insert REQUESTED booking

    Client->>Gateway: Confirm booking
    Gateway->>Booking: POST /bookings/{id}/confirm
    Booking->>DB: Set booking CONFIRMED

    Client->>Gateway: Start session from booking
    Gateway->>Booking: POST /sessions/start-with-booking/{id}
    Booking->>DB: Insert ACTIVE session

    Client->>Gateway: Create payment intent
    Gateway->>Payment: POST /payments/intent
    Payment->>DB: Insert CREATED transaction

    Client->>Gateway: Confirm payment
    Gateway->>Payment: POST /payments/{id}/confirm
    Payment->>DB: Update transaction and card balance

    Client->>Gateway: End session
    Gateway->>Booking: POST /sessions/{id}/end
    Booking->>DB: Set session ENDED
```

Notice that Location does not call Booking, and Booking does not call Payment. The client carries resource IDs from one API response into later requests.

## 6. Logical Cross-Service References

```mermaid
flowchart LR
    Zone["Zone ID"] --> Location["Parking Location"]
    Location -->|locationId| Space["Parking Space"]

    Location -->|locationId| Booking["Booking"]
    Space -->|optional spaceId| Booking

    Booking -->|bookingId| Session["Parking Session"]
    Location -->|locationId| Session
    Space -->|optional spaceId| Session

    Booking -.->|optional bookingId| Transaction["Payment Transaction"]
    Session -.->|optional sessionId| Transaction
    Card["Mock Card"] -->|cardId| Transaction
```

These are logical references passed as numeric IDs. Except for relationships inside an individual service's table group, the current migrations do not define database foreign keys between the Location, Booking, and Payment table groups.

## 7. Parking Session Lifecycle

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: POST /sessions/start
    [*] --> ACTIVE: POST /sessions/start-by-plate
    [*] --> BookingCheck: POST /sessions/start-with-booking/{id}

    BookingCheck --> ACTIVE: Booking is CONFIRMED
    BookingCheck --> Rejected: Booking is not CONFIRMED

    ACTIVE --> ENDED: POST /sessions/{sessionId}/end
    Rejected --> [*]
    ENDED --> [*]
```

Session query endpoints are:

- `GET /sessions/active`
- `GET /sessions/by-vehicle/{registrationNumber}`

The domain enum also contains `CANCELLED`, but the current controller does not expose a session-cancellation endpoint.

## 8. Location and Camera Flow

```mermaid
flowchart TD
    Zone["Create parking zone"] --> Location["Create parking location"]
    Location --> Space["Create one or more spaces"]
    Space --> Status["Update space status"]
    Status --> Availability["Calculate location or zone availability"]

    Location --> Camera["Register mock camera"]
    Camera --> Upload["Upload multipart image"]
    Upload --> Plate["Return random mock registration plate"]
    Plate --> Session["Client calls<br/>POST /sessions/start-by-plate"]
```

The camera endpoint does not call the Booking service. It returns a mock plate to the client, which must then start the session in a separate request.

## 9. Payment Decision Flow

```mermaid
flowchart TD
    Intent["Create payment intent<br/>status CREATED"] --> Confirm["Confirm payment"]
    Confirm --> CardState{"Card state"}

    CardState -->|EXPIRED| Expired["DECLINED<br/>CARD_EXPIRED"]
    CardState -->|BLOCKED| Blocked["DECLINED<br/>CARD_BLOCKED"]
    CardState -->|ACTIVE| Rules{"Active payment rules"}

    Rules -->|DECLINE_ALWAYS| RuleDecline["DECLINED_BY_ACTIVE_RULE"]
    Rules -->|Amount above threshold| Threshold["AMOUNT_ABOVE_RULE_THRESHOLD"]
    Rules -->|No matching decline| Balance{"Enough balance?"}

    Balance -->|No| Insufficient["DECLINED<br/>INSUFFICIENT_BALANCE"]
    Balance -->|Yes| Success["Debit card<br/>status SUCCEEDED"]
    Success --> Refund["Optional refund"]
    Refund --> Refunded["Restore balance<br/>status REFUNDED"]
```

The Payment service uses mock cards and rules intended for deterministic QA testing. It does not integrate with a real payment provider.

## 10. Automated Testing Architecture

```mermaid
flowchart LR
    Trigger["Developer / GitHub Actions"] --> Maven["Maven"]
    Maven --> TestNG["TestNG Cucumber runner"]
    TestNG --> Features["Gherkin features selected by tags"]
    Features --> Steps["Step definitions"]

    Steps --> RestAssured["Rest Assured"]
    RestAssured --> Gateway["Gateway or direct service URL"]

    Steps --> JDBC["JDBC DB checks"]
    JDBC --> Tunnel["Optional SSH tunnel<br/>localhost:15432"]
    Tunnel --> EC2["EC2 bastion / test runner"]
    EC2 --> RDS[("Private RDS parking_db")]

    TestNG --> Cucumber["Cucumber HTML / JSON"]
    TestNG --> Surefire["Surefire XML"]
    TestNG --> AllureResults["Allure results"]
    AllureResults --> Allure["Allure HTML report"]
```

For local database verification against private RDS, `localhost:15432` is the local end of the SSH tunnel. Traffic is forwarded through EC2 to RDS; PostgreSQL is not running on the tester's computer.

## 11. Service Classification

| Service | Port | Classification | Current responsibility |
| --- | ---: | --- | --- |
| Gateway | 8080 | Core infrastructure | Routing, JWT enforcement, headers, rate limiting, retries, circuit breakers, and fallback |
| Auth | 8082 | Core business support | User registration, roles, login, and JWT generation |
| Location | 8083 | Core domain | Zones, locations, spaces, availability, cameras, and mock plate events |
| Booking | 8084 | Core domain | Future bookings and actual parking sessions |
| Payment | 8085 | Core domain | Mock cards, test rules, payment intents, confirmation, decline, and refund |
| API | 8081 | Optional demonstration | Status and secured sample endpoints; not used by the parking workflow |

## 12. Important QA Implications

- Test the API service independently as a JWT/security sample, but do not include it as a required step in the parking end-to-end flow.
- Test the gateway and each downstream service because JWT validation occurs at both layers.
- Verify records in the shared `parking_db`, using the table group owned by the service under test.
- Test invalid cross-service IDs because the services do not currently call one another to verify every reference.
- Test cross-user access to bookings, sessions, and payments because ID-based operations require careful ownership controls.
- Test repeated confirm, cancel, end-session, payment-confirm, and refund requests because state transitions are handled inside individual controllers.
- For camera tests, assert plate format and response source rather than expecting the uploaded image to be genuinely recognized.
