# Transport Company - Architecture & Design Explanation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture Decisions](#architecture-decisions)
3. [Layer-by-Layer Explanation](#layer-by-layer-explanation)
4. [Database Design](#database-design)
5. [Business Rules Implementation](#business-rules-implementation)
6. [Technology Choices](#technology-choices)
7. [Design Patterns](#design-patterns)
8. [Data Flow](#data-flow)

---

## System Overview

### What This System Does
This is a **console-based management system** for a transport company that handles:
- Multiple transport companies with their fleets and employees
- Client management (individuals and corporate clients)
- Vehicle fleet management (buses, trucks, tankers, vans)
- Employee management with role-based access and driver qualifications
- Transport operations (passenger and cargo)
- Payment tracking
- Reporting and analytics
- Data import/export via CSV

### Why Console Application?
- **Direct Database Control**: JDBC gives precise control over SQL and transactions
- **No Web Overhead**: Faster development, no HTTP layer complexity
- **Scriptable**: Easy to automate with shell scripts
- **Lightweight**: Minimal resource usage
- **Professional CLI**: Using PicoCLI provides enterprise-grade command interface

---

## Architecture Decisions

### 1. Clean Layered Architecture

```
┌─────────────────────────────────────────┐
│         CLI Layer (User Interface)       │  ← Commands, Input/Output
├─────────────────────────────────────────┤
│      Service Layer (Business Logic)      │  ← Validation, Transactions
├─────────────────────────────────────────┤
│    DAO Layer (Data Access)               │  ← SQL, JDBC Operations
├─────────────────────────────────────────┤
│         Domain Layer (Models)            │  ← Pure Java Objects
├─────────────────────────────────────────┤
│      Database (MySQL)                    │  ← Persistent Storage
└─────────────────────────────────────────┘
```

**Why This Architecture?**
- **Separation of Concerns**: Each layer has ONE responsibility
- **Testability**: Can test business logic without database
- **Maintainability**: Changes in UI don't affect business rules
- **Scalability**: Easy to add web interface later (reuse Service layer)

### 2. JDBC Instead of JPA/Hibernate

**Why JDBC?**
- **Performance**: Direct SQL control, no ORM overhead
- **Simplicity**: For CLI apps, ORM is overkill
- **Transparency**: See exact SQL queries being executed
- **Learning**: Better understanding of database operations
- **Connection Pooling**: HikariCP provides enterprise-level pooling

**Trade-offs Accepted**:
- More boilerplate code (but organized in DAOs)
- Manual mapping (but predictable and debuggable)

### 3. Single Table Inheritance for Vehicles

Instead of separate tables for Bus, Truck, Tanker, we use:

```sql
CREATE TABLE vehicle (
    type VARCHAR(20) NOT NULL,  -- BUS, TRUCK, TANKER, VAN
    seat_count INT,             -- NULL for non-buses
    max_load_kg INT,            -- NULL for buses/tankers
    volume_liters INT           -- NULL for buses/trucks
)
```

**Why?**
- **Simplicity**: One table, one DAO
- **Flexibility**: Easy to add new vehicle types
- **Queries**: Simple joins (no UNION needed)
- **Polymorphism**: Handled in application layer via validators

**Alternative Considered**: Table per Type
- Would require separate tables: `bus`, `truck`, `tanker`
- More complex queries with UNION
- More DAOs to maintain

### 4. Enum-Based Qualification System

```java
enum DriverQualification {
    PASSENGERS_12_PLUS,  // For buses with >12 passengers
    HAZMAT,              // For hazardous materials
    OVERSIZED            // For oversized cargo
}
```

**Why Enums?**
- **Type Safety**: Compiler prevents invalid values
- **Performance**: No database lookup needed
- **Immutability**: Values cannot change at runtime
- **Self-Documenting**: Code is the documentation

**Many-to-Many Relationship**:
```sql
CREATE TABLE employee_qualification (
    employee_id BIGINT,
    qualification VARCHAR(50)
)
```
One driver can have multiple qualifications.

---

## Layer-by-Layer Explanation

### Domain Layer (`domain/`)

**Purpose**: Pure business entities with NO logic

```java
public class Transport {
    private Long id;
    private Long companyId;
    private String origin;
    private String destination;
    private OffsetDateTime departAt;
    private OffsetDateTime arriveAt;
    private CargoKind cargoKind;        // GOODS or PASSENGERS
    private Integer cargoWeightKg;       // Only for GOODS
    private Integer passengerCount;      // Only for PASSENGERS
    private BigDecimal price;
    private Long driverId;
    private Long vehicleId;
    private PaymentStatus paymentStatus; // PAID or UNPAID
}
```

**Why POJOs?**
- Framework-agnostic (can move to Spring later)
- Easy to serialize (CSV, JSON)
- Testable without dependencies

**Key Design Choices**:
- `OffsetDateTime` instead of `Date`: Timezone-aware, immutable
- `BigDecimal` for money: Precise decimal arithmetic
- Nullable fields: `cargoWeightKg` XOR `passengerCount` based on `cargoKind`

### DAO Layer (`dao/`)

**Purpose**: Encapsulate ALL database operations

```java
public interface TransportDao {
    Transport create(Transport transport);
    Transport update(Transport transport);
    Optional<Transport> findById(Long id);
    List<Transport> list(String destinationFilter, String sort);
    void updatePaymentStatus(Long id, PaymentStatus status);
    void delete(Long id);
}
```

**Why Interfaces?**
- **Testability**: Can mock DAOs in service tests
- **Flexibility**: Easy to swap JDBC → JPA later
- **Contracts**: Clear API between layers

**Implementation Pattern** (`JdbcTransportDao`):

```java
public Transport create(Transport transport) {
    String sql = "INSERT INTO transport(...) VALUES (?, ?, ...)";
    try (Connection con = getConnection(); 
         PreparedStatement ps = con.prepareStatement(sql, RETURN_GENERATED_KEYS)) {
        // Fill parameters
        ps.executeUpdate();
        // Get generated ID
        return transport;
    } catch (SQLException e) {
        throw new DatabaseException("Failed to create transport", e);
    }
}
```

**Key Patterns**:
- **Try-with-resources**: Auto-closes connections (prevents leaks)
- **PreparedStatement**: SQL injection prevention
- **Custom Exceptions**: Translate SQLException → DatabaseException
- **ResultSet Mappers**: Centralized mapping logic

**BaseDao Pattern**:
```java
public abstract class BaseDao {
    protected final DataSource dataSource;
    
    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // From HikariCP pool
    }
}
```
All DAOs extend this → DRY principle.

### Service Layer (`service/`)

**Purpose**: Business logic + transaction coordination

```java
public class TransportService {
    private final TransportDao transportDao;
    private final TransportValidator validator;

    public Transport create(Transport transport) {
        validator.validate(transport);  // ← Business rules
        return transportDao.create(transport);
    }
    
    public void setPaymentStatus(Long id, PaymentStatus status) {
        // Could add: check if already paid, notify accounting, etc.
        transportDao.updatePaymentStatus(id, status);
    }
}
```

**Why Services?**
- **Single Responsibility**: DAO = data, Service = business rules
- **Transaction Boundaries**: Services define transaction scope
- **Reusability**: CLI and future Web API share same services
- **Orchestration**: Coordinate multiple DAOs for complex operations

**Example Complex Operation**:
```java
public Transport create(Transport transport) {
    // 1. Validate business rules
    validator.validate(transport);  
    // ↑ Checks: dates, vehicle compatibility, driver qualifications
    
    // 2. Database operation
    return transportDao.create(transport);
    
    // Future: Add notifications, logging, analytics
}
```

### Validation Layer (`validation/`)

**Purpose**: Enforce ALL business rules BEFORE database

```java
public class TransportValidator {
    private final VehicleDao vehicleDao;  // ← Needs DB for vehicle type
    private final EmployeeDao employeeDao; // ← Needs DB for qualifications

    public void validate(Transport transport) {
        ValidationResult result = new ValidationResult();
        
        // Rule 1: Arrival after Departure
        if (transport.getArriveAt().isBefore(transport.getDepartAt())) {
            result.addError("arriveAt", "Arrival must be after departure");
        }
        
        // Rule 2: Cargo Constraints
        if (transport.getCargoKind() == GOODS) {
            if (transport.getCargoWeightKg() == null || transport.getCargoWeightKg() <= 0) {
                result.addError("cargoWeightKg", "Weight required for goods");
            }
            if (transport.getPassengerCount() != null) {
                result.addError("passengerCount", "Must be null for goods");
            }
        }
        
        // Rule 3: Vehicle Compatibility (requires DB lookup)
        vehicleDao.findById(transport.getVehicleId()).ifPresent(vehicle -> {
            if (transport.getCargoKind() == PASSENGERS && vehicle.getType() != BUS) {
                result.addError("vehicleId", "Only buses transport passengers");
            }
        });
        
        // Rule 4: Driver Qualifications
        employeeDao.findById(transport.getDriverId()).ifPresent(driver -> {
            if (transport.getPassengerCount() > 12) {
                if (!driver.getQualifications().contains(PASSENGERS_12_PLUS)) {
                    result.addError("driverId", "Driver needs PASSENGERS_12_PLUS");
                }
            }
        });
        
        if (!result.isValid()) {
            throw new ValidationException("Validation failed", result.getFieldErrors());
        }
    }
}
```

**Why Separate Validation Layer?**
- **Reusability**: Same rules for CLI, REST API, batch imports
- **Testability**: Mock DAOs, test business rules in isolation
- **Clarity**: Business rules are VISIBLE and DOCUMENTED in code
- **Error Reporting**: Collect ALL errors, not just first one

**ValidationResult Pattern**:
```java
public class ValidationResult {
    private Map<String, String> fieldErrors = new HashMap<>();
    
    public void addError(String field, String message) {
        fieldErrors.put(field, message);
    }
    
    public boolean isValid() {
        return fieldErrors.isEmpty();
    }
}
```
Accumulates all validation errors → better UX.

### CLI Layer (`cli/`)

**Purpose**: User interface via commands

**PicoCLI Architecture**:
```java
@Command(name = "transport", subcommands = {
    CompanyCommand.class,
    ClientCommand.class,
    EmployeeCommand.class,
    VehicleCommand.class,
    TransportSubCommand.class,
    PaymentCommand.class,
    ExportCommand.class,
    ImportCommand.class,
    ReportCommand.class
})
public class TransportCommand implements Callable<Integer> {
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }
}
```

**Why PicoCLI?**
- **Auto Help**: Generates help text automatically
- **Type Conversion**: Parses strings → dates, enums, numbers
- **Validation**: Built-in validation for required/optional
- **Subcommands**: Hierarchical command structure
- **Professional**: Industry-standard CLI framework

**Command Example**:
```java
@Command(name = "company")
public class CompanyCommand {
    @Command(name = "add")
    public void add(@Parameters(index = "0") String name,
                    @Parameters(index = "1") String address) {
        TransportCompany company = new TransportCompany(null, name, address, null);
        CompanyService service = new CompanyService(new JdbcTransportCompanyDao(dataSource));
        company = service.create(company);
        System.out.println("Company created with ID: " + company.getId());
    }
    
    @Command(name = "list")
    public void list(@Option(names = "--sort") String sort) {
        List<TransportCompany> companies = service.list(sort);
        companies.forEach(System.out::println);
    }
}
```

**Dependency Injection Pattern** (Manual):
```java
DataSource ds = DataSourceFactory.getDataSource();
TransportCompanyDao dao = new JdbcTransportCompanyDao(ds);
CompanyService service = new CompanyService(dao);
```
Could migrate to Spring DI later without changing service code.

### Configuration Layer (`config/`)

**1. ConfigLoader** - Properties Management
```java
public class ConfigLoader {
    private static Properties properties;
    
    public static Properties load() {
        if (properties == null) {
            properties = new Properties();
            properties.load(new FileInputStream("application.properties"));
        }
        return properties;
    }
}
```
**Singleton Pattern**: Load config once, reuse everywhere.

**2. DataSourceFactory** - Connection Pooling
```java
public class DataSourceFactory {
    private static HikariDataSource dataSource;
    
    public static DataSource getDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.user"));
            config.setPassword(properties.getProperty("db.password"));
            config.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }
}
```

**Why HikariCP?**
- **Performance**: Fastest connection pool in Java
- **Reliability**: Battle-tested in production
- **Monitoring**: Built-in metrics
- **Configuration**: Simple and powerful

**3. FlywayMigrator** - Database Versioning
```java
public class FlywayMigrator {
    public static void migrate() {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .schemas("transport_company")
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();
        flyway.migrate();
    }
}
```

**Why Flyway?**
- **Versioning**: Database schema as code
- **Repeatability**: Same migrations in dev/test/prod
- **Safety**: Checksum verification prevents manual changes
- **Team Collaboration**: Migrations in Git

---

## Database Design

### ERD Overview

```
┌─────────────────────┐
│ transport_company   │
│──────────────────── │
│ id (PK)            │◄─────┐
│ name               │      │
│ address            │      │
│ created_at         │      │
└─────────────────────┘      │
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
         ▼                   ▼                   ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ client          │  │ employee        │  │ vehicle         │
│──────────────── │  │──────────────── │  │──────────────── │
│ id (PK)         │  │ id (PK)         │  │ id (PK)         │
│ company_id (FK) │  │ company_id (FK) │  │ company_id (FK) │
│ name            │  │ full_name       │  │ reg_number      │
│ phone           │  │ salary          │  │ type (ENUM)     │
│ email           │  │ role (ENUM)     │  │ seat_count      │
│ type (ENUM)     │  │ created_at      │  │ max_load_kg     │
│ created_at      │  └─────────────────┘  │ volume_liters   │
└─────────────────┘           │            │ created_at      │
         │                    │            └─────────────────┘
         │                    │                     │
         │                    ▼                     │
         │         ┌─────────────────────┐         │
         │         │ employee_qual (M:N) │         │
         │         │──────────────────── │         │
         │         │ employee_id (FK)    │         │
         │         │ qualification (ENUM)│         │
         │         └─────────────────────┘         │
         │                                          │
         └───────────┐                 ┌───────────┘
                     ▼                 ▼
              ┌─────────────────────────────┐
              │ transport                   │
              │──────────────────────────── │
              │ id (PK)                     │
              │ company_id (FK)             │
              │ client_id (FK)              │
              │ driver_id (FK → employee)   │
              │ vehicle_id (FK → vehicle)   │
              │ origin                      │
              │ destination                 │
              │ depart_at                   │
              │ arrive_at                   │
              │ cargo_kind (ENUM)           │
              │ cargo_weight_kg             │
              │ passenger_count             │
              │ price                       │
              │ payment_status (ENUM)       │
              │ created_at                  │
              └─────────────────────────────┘
```

### Key Design Decisions

**1. Timestamps**
```sql
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```
**Why?**
- Audit trail: know when records were created
- Debugging: correlate events
- Analytics: time-based reports

**2. Indexes**
```sql
CREATE INDEX idx_company_name ON transport_company(name);
CREATE INDEX idx_employee_salary ON employee(salary);
CREATE INDEX idx_transport_destination ON transport(destination);
CREATE INDEX idx_transport_company_depart ON transport(company_id, depart_at);
```

**Why These Indexes?**
- `company_name`: Fast company searches/sorts
- `employee_salary`: Fast salary sorting for reports
- `transport_destination`: Frequent filter in list queries
- `(company_id, depart_at)`: Composite for date-range queries per company

**3. Foreign Keys with Constraints**
```sql
FOREIGN KEY (company_id) REFERENCES transport_company(id) ON DELETE CASCADE
```

**Why CASCADE?**
- Deleting a company automatically deletes:
  - All its employees
  - All its vehicles
  - All its transports
- Data integrity: no orphaned records

**Alternative**: Use `ON DELETE RESTRICT` if you want to prevent deletion of companies with data.

**4. ENUM Storage**
```sql
role VARCHAR(50) -- Stores 'DRIVER', 'DISPATCHER', 'ADMIN'
```
Instead of separate lookup tables.

**Why?**
- **Performance**: No joins needed
- **Simplicity**: Fewer tables
- **Type Safety**: Java enums enforce values

**When NOT to use**: If values change frequently (then use lookup table).

---

## Business Rules Implementation

### Rule 1: Date Validation
```java
if (transport.getArriveAt().isBefore(transport.getDepartAt())) {
    result.addError("arriveAt", "Arrival must be after departure");
}
```
**Why Important**: Prevents logical impossibilities.

### Rule 2: Cargo Type Constraints
```java
if (cargoKind == GOODS) {
    require(cargoWeightKg > 0);
    require(passengerCount == null);
} else if (cargoKind == PASSENGERS) {
    require(passengerCount > 0);
    require(cargoWeightKg == null);
}
```
**Why Exclusive**: A transport is EITHER goods OR passengers, never both.

### Rule 3: Vehicle-Cargo Compatibility
```java
if (cargoKind == PASSENGERS && vehicle.getType() != BUS) {
    error("Only buses transport passengers");
}
if (cargoKind == GOODS && vehicle.getType() == BUS) {
    error("Buses cannot transport goods");
}
```
**Real-world Constraint**: Physical vehicle capabilities.

### Rule 4: Capacity Checks
```java
if (vehicle.getType() == BUS) {
    if (passengerCount > vehicle.getSeatCount()) {
        error("Exceeds bus capacity");
    }
}
if (vehicle.getType() == TRUCK) {
    if (cargoWeightKg > vehicle.getMaxLoadKg()) {
        error("Exceeds truck capacity");
    }
}
```
**Safety**: Prevent overloading.

### Rule 5: Driver Qualifications
```java
if (passengerCount > 12) {
    if (!driver.getQualifications().contains(PASSENGERS_12_PLUS)) {
        error("Driver needs PASSENGERS_12_PLUS qualification");
    }
}
```
**Legal Requirement**: Special license for large passenger vehicles.

---

## Technology Choices

### Java 21 LTS
**Why?**
- **Latest LTS**: Long-term support until 2029
- **Performance**: G1GC improvements, virtual threads (future)
- **Language Features**: Records, pattern matching, text blocks
- **Security**: Latest security patches

**Key Features Used**:
- **Records** (`ImportResult`): Immutable data carriers
- **Text Blocks**: Multi-line SQL queries (future enhancement)
- **var**: Type inference for cleaner code

### MySQL 8.x
**Why?**
- **Maturity**: 25+ years of development
- **Performance**: InnoDB engine with ACID compliance
- **Features**: JSON columns, CTEs, window functions
- **Tooling**: Excellent admin tools (MySQL Workbench)
- **Cost**: Free and open-source

**Alternatives Considered**:
- **PostgreSQL**: More features but heavier
- **H2**: Good for testing but MySQL for production

### HikariCP
**Why?**
- **Speed**: Zero-overhead connection pool
- **Reliability**: Production-proven
- **Metrics**: Built-in monitoring
- **Small**: Tiny footprint (~130KB)

**Configuration**:
```properties
maximumPoolSize=10        # Max 10 concurrent connections
connectionTimeout=10000   # 10 seconds to get connection
```

### Flyway
**Why?**
- **Simplicity**: SQL-based migrations
- **Reliability**: Checksum verification
- **Rollback**: Version control for database
- **Team**: Multiple developers can work together

**Migration Naming**:
```
V1__init.sql         # Initial schema
V2__indexes.sql      # Performance indexes
V3__add_column.sql   # Future changes
```

### PicoCLI
**Why?**
- **Professional**: Enterprise-grade CLI framework
- **Auto Help**: Generates usage documentation
- **Type Safety**: Compile-time checked
- **Extensible**: Easy to add subcommands

**Alternative Considered**: Apache Commons CLI (more verbose).

### SLF4J + Logback
**Why?**
- **Standard**: Industry-standard logging facade
- **Performance**: Async appenders, conditional logging
- **Flexibility**: Change log levels without recompile
- **Features**: Rolling files, filters, patterns

**Configuration** (`logback.xml`):
```xml
<appender name="FILE" class="RollingFileAppender">
    <file>logs/transport.log</file>
    <rollingPolicy class="TimeBasedRollingPolicy">
        <fileNamePattern>logs/transport.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
</appender>
```

---

## Design Patterns

### 1. DAO Pattern
**Purpose**: Separate data access from business logic.
```java
interface EmployeeDao { /* CRUD */ }
class JdbcEmployeeDao implements EmployeeDao { /* SQL */ }
```

### 2. Service Layer Pattern
**Purpose**: Encapsulate business logic.
```java
class EmployeeService {
    private EmployeeDao dao;
    private EmployeeValidator validator;
}
```

### 3. Singleton Pattern
**Where**: `ConfigLoader`, `DataSourceFactory`
**Why**: One shared instance across application.

### 4. Factory Pattern
**Where**: `DataSourceFactory.create()`
**Why**: Centralize object creation logic.

### 5. Strategy Pattern (Implicit)
**Where**: Different validators for different entities
**Why**: Pluggable validation strategies.

### 6. Template Method (BaseDao)
**Where**: `getConnection()` in BaseDao
**Why**: Reuse common DAO logic.

### 7. Builder Pattern (Potential)
**Where**: Could use for complex domain objects
**Why**: Fluent object construction.

---

## Data Flow

### Example: Creating a Transport

```
1. CLI Layer
   ↓
   TransportSubCommand.add()
   │
   ├─ Parse CLI arguments → Transport object
   │
   └─ Call service.create(transport)

2. Service Layer
   ↓
   TransportService.create()
   │
   ├─ TransportValidator.validate()
   │  │
   │  ├─ Check dates (arrive >= depart)
   │  ├─ Check cargo constraints
   │  ├─ vehicleDao.findById() → verify vehicle type
   │  ├─ employeeDao.findById() → verify driver qualifications
   │  └─ Throw ValidationException if errors
   │
   └─ transportDao.create(transport)

3. DAO Layer
   ↓
   JdbcTransportDao.create()
   │
   ├─ Get connection from HikariCP pool
   ├─ Prepare SQL: INSERT INTO transport...
   ├─ Fill PreparedStatement parameters
   ├─ Execute INSERT
   ├─ Retrieve generated ID
   ├─ Close connection (back to pool)
   └─ Return Transport with ID

4. Database
   ↓
   MySQL executes:
   │
   ├─ Validate foreign keys (company, client, driver, vehicle exist)
   ├─ Insert row
   ├─ Generate auto-increment ID
   └─ Commit transaction

5. Response Flow (upward)
   ↓
   Transport object (with ID)
   │
   ├─ DAO returns to Service
   ├─ Service returns to CLI
   └─ CLI prints: "Transport created with ID: 123"
```

### Transaction Boundaries

**Current**: Single operations = single transactions
```java
public Transport create(Transport transport) {
    validator.validate(transport);
    return dao.create(transport); // Auto-commit
}
```

**Future Enhancement**: Multi-operation transactions
```java
@Transactional
public void createWithNotification(Transport transport) {
    validator.validate(transport);
    dao.create(transport);
    notificationService.notifyClient(transport.getClientId());
    // All-or-nothing: both succeed or both rollback
}
```

---

## Error Handling Strategy

### Exception Hierarchy

```
RuntimeException
    │
    ├─ ValidationException      (Business rule violations)
    │
    ├─ BusinessRuleException    (Complex business logic errors)
    │
    ├─ NotFoundException        (Entity not found)
    │
    └─ DatabaseException        (SQL/connection errors)
```

**Why Custom Exceptions?**
- **Clarity**: Know error type immediately
- **Handling**: Different recovery strategies per type
- **Logging**: Different log levels per type

### Error Propagation

```
Database (SQLException)
    ↓
DAO catches → throw DatabaseException
    ↓
Service catches → log + rethrow OR handle
    ↓
CLI catches → print user-friendly message
```

**Example**:
```java
try {
    service.create(transport);
} catch (ValidationException e) {
    System.err.println("Validation errors:");
    e.getFieldErrors().forEach((field, msg) -> 
        System.err.println("  " + field + ": " + msg)
    );
} catch (DatabaseException e) {
    System.err.println("Database error: " + e.getMessage());
    log.error("DB error", e); // Full stack trace to log
}
```

---

## Testing Strategy

### Unit Tests
**What**: Validators, Business Logic
**How**: Mock DAOs using Mockito
**Example**:
```java
@ExtendWith(MockitoExtension.class)
class TransportValidatorTest {
    @Mock VehicleDao vehicleDao;
    @Mock EmployeeDao employeeDao;
    
    @Test
    void rejectsArrivalBeforeDeparture() {
        // Test with mocked DAOs
    }
}
```

### Integration Tests
**What**: DAO operations with real database
**How**: Testcontainers (Docker MySQL)
**Example**:
```java
@Testcontainers
class TransportDaoIntegrationTest {
    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0");
    
    @Test
    void createsAndReadsTransport() {
        // Test with real database
    }
}
```

**Why Testcontainers?**
- **Isolation**: Fresh database per test
- **Reproducibility**: Same environment for all developers
- **CI/CD**: Works in automated pipelines

---

## Future Enhancements

### 1. REST API Layer
Add Spring Boot on top of existing services:
```
┌─────────────────┐
│ REST Controller │  ← New
├─────────────────┤
│ Service Layer   │  ← Reuse
├─────────────────┤
│ DAO Layer       │  ← Reuse
└─────────────────┘
```

### 2. Event-Driven Architecture
```java
public Transport create(Transport transport) {
    validator.validate(transport);
    Transport created = dao.create(transport);
    eventPublisher.publish(new TransportCreatedEvent(created));
    return created;
}
```

### 3. Caching Layer
```java
@Cacheable("vehicles")
public Vehicle findById(Long id) {
    return dao.findById(id);
}
```

### 4. Microservices Split
- **Fleet Service**: Vehicles + Drivers
- **Transport Service**: Transports + Payments
- **Reporting Service**: Analytics

### 5. Advanced Reporting
- Dashboard with graphs
- PDF/Excel export
- Scheduled reports via email

---

## Conclusion

This architecture provides:
- ✅ **Clean separation** of concerns
- ✅ **Testability** at every layer
- ✅ **Maintainability** through clear structure
- ✅ **Scalability** for future growth
- ✅ **Professional** enterprise-grade patterns
- ✅ **Performance** through connection pooling and indexes
- ✅ **Reliability** through validation and transactions

The system is production-ready while remaining simple enough to understand and extend.
