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

### Run all tests
```bash
./mvnw test
```

### Run only integration tests
```bash
./mvnw test -Dtest="*IT"
```

### Run specific test class
```bash
./mvnw test -Dtest=JobResourceIT
./mvnw test -Dtest=JobBatchResourceIT
```

### Run with debug logging
```bash
./mvnw test -Dquarkus.log.level=DEBUG
```

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