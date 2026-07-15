# Secure Enterprise IAM Platform - Week 1

Foundational infrastructure for a secure IAM platform using Keycloak and PostgreSQL.

## Prerequisites
- Docker Desktop installed and running
- VS Code

## Getting Started

1. **Start the infrastructure:**
   ```bash
   docker compose up -d
   ```

2. **Verify status:**
   ```bash
   docker compose ps
   ```

3. **Check logs:**
   ```bash
   docker compose logs -f
   ```

4. **Access Keycloak Admin Console:**
   - URL: http://localhost:8080
   - Username: `admin` (from .env)
   - Password: `admin_password_123` (from .env)

5. **Stop the infrastructure:**
   ```bash
   docker compose down
   ```

## Infrastructure Components
- **PostgreSQL**: Stores Keycloak's identity data. Persistent via `postgres_data` volume.
- **Keycloak**: Open-source Identity and Access Management solution.

## Verification Checklist
- [ ] PostgreSQL container is healthy.
- [ ] Keycloak container is running and connected to Postgres.
- [ ] Admin console is reachable at port 8080.
