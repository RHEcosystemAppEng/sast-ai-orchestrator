# Database Migrations for SAST-AI-Orchestrator

## Current Setup

The SAST-AI-Orchestrator currently uses Hibernate's `drop-and-create` mode for database schema management:

```properties
# application.properties
quarkus.hibernate-orm.database.generation=drop-and-create
```

This means tables are automatically created from JPA entity annotations on application startup.

## Migration Files

This directory contains Flyway migration scripts for production deployment when switching from `drop-and-create` to proper database migrations.

### Available Migrations

- **V1_1_0__Add_OSH_Retry_Tables.sql**: Creates the OSH retry mechanism tables and indexes
  - `osh_uncollected_scan` table for retry queue management
  - Optimized indexes for retry queries and cleanup operations
  - Proper constraints and documentation

## Switching to Production Migrations

To use these migration scripts in production:

1. **Change application.properties**:
   ```properties
   # Replace drop-and-create with validate
   quarkus.hibernate-orm.database.generation=validate

   # Enable Flyway migrations
   quarkus.flyway.migrate-at-start=true
   quarkus.flyway.locations=classpath:db/migration
   ```

2. **Add Flyway dependency** to pom.xml:
   ```xml
   <dependency>
       <groupId>io.quarkus</groupId>
       <artifactId>quarkus-flyway</artifactId>
   </dependency>
   ```

3. **Run initial migration**:
   ```bash
   # First time setup - let Flyway baseline existing database
   ./mvnw quarkus:dev -Dquarkus.flyway.baseline-on-migrate=true
   ```

## Migration Naming Convention

Flyway migrations follow the pattern: `V{major}_{minor}_{patch}__{Description}.sql`

- **V1_1_0**: OSH retry mechanism (current)
- **V1_2_0**: Future enhancements
- **V2_0_0**: Major version changes

## Index Strategy

The OSH retry tables use strategic indexing:

1. **Primary Index**: `osh_scan_id` (unique constraint, scan lookups)
2. **Retry Query Index**: `(last_attempt_at, attempt_count, failure_reason)` - optimizes main retry selection
3. **Cleanup Index**: `created_at` - optimizes retention policy cleanup
4. **Monitoring Indexes**: `package_name`, `failure_reason`, `attempt_count` - for analysis

## Performance Notes

- The retry eligibility query uses `FOR UPDATE SKIP LOCKED` for concurrent scheduler safety
- Composite indexes support complex WHERE clauses efficiently
- Regular cleanup prevents unbounded table growth

## Development vs Production

- **Development**: Continue using `drop-and-create` for rapid iteration
- **Production**: Use Flyway migrations for controlled schema evolution
- **Testing**: Both approaches supported via configuration profiles