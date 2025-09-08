# Integration Tests

Integration tests for the SAST-AI Orchestrator REST API using Quarkus Test Framework with TestContainers.

## Test Structure

```
src/test/java/com/redhat/sast/api/
├── AbstractIntegrationTest.java     # Base test class with PostgreSQL TestContainer
├── config/
│   └── TestProfile.java             # Test configuration and mock service setup
├── mock/
│   ├── MockKubernetesResourceManager.java
│   ├── MockPlatformService.java
│   └── MockTektonClient.java        # Mock implementations for external services
├── testdata/
│   ├── JobBatchTestDataBuilder.java
│   └── JobTestDataBuilder.java      # Test data builders
└── v1/resource/
    ├── JobBatchResourceIT.java     # Job batch endpoint integration tests
    └── JobResourceIT.java          # Job endpoint integration tests
```

## Running Tests

### Prerequisites
- **Docker** must be running (TestContainers automatically creates PostgreSQL containers)
- **No manual database setup required** - TestContainers handles everything automatically

### Standard approach (works in most cases)
```bash
./mvnw test -Dtest="*IT"
```

### Alternative: Complete build and test pipeline
```bash
# Option 1 (Complete build and test)
./mvnw clean verify -DskipITs=false

# Option 2 (for faster development iterations)
./mvnw clean package -DskipTests
./mvnw failsafe:integration-test failsafe:verify -DskipITs=false
```

### Run specific test class
```bash
./mvnw test -Dtest=JobResourceIT
./mvnw test -Dtest=JobBatchResourceIT
```

### Run with debug logging
```bash
./mvnw test -Dtest="*IT" -Dquarkus.log.level=DEBUG
```

### macOS Users
If you encounter TestContainers connection issues on macOS, set this environment variable:
```bash
export TESTCONTAINERS_RYUK_DISABLED=true
./mvnw test -Dtest="*IT"
```

**Why this happens**: The Ryuk container (TestContainers' self-cleaning mechanism) sometimes doesn't work properly on macOS, causing connection refused errors.

## Test Configuration

- **Database**: PostgreSQL 15 TestContainer with automatic schema creation
- **Mocks**: Kubernetes, Tekton, and Platform services are mocked to avoid external dependencies
- **Profile**: Uses `test` profile with configuration overrides in `TestProfile.java`
- **Data**: Fresh database created for each test class using `drop-and-create` strategy

## Test Coverage

### Job Resource Tests (`JobResourceIT`)
- Create single jobs
- Retrieve jobs by ID and with filtering
- Handle pagination
- Cancel jobs
- Validate input fields
- Handle known false positives

### Job Batch Resource Tests (`JobBatchResourceIT`)
- Submit job batches
- Retrieve batches by ID
- Cancel job batches
- Validate batch submissions
- Handle Google Sheets integration
- Create individual jobs within batches

## Test Data

Test data builders provide pre-configured test objects:
- `JobTestDataBuilder` - Creates job creation DTOs with various package NVRs
- `JobBatchTestDataBuilder` - Creates batch submission DTOs with multiple jobs

Example test packages include: `tree-pkg`, `units`, `xz`, `zlib-ng`, `userspace-rcu`, `usbutils`