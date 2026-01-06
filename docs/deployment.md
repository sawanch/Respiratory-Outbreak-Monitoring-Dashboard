# Deployment Guide

## AWS EC2 Deployment

### 1. Set Environment Variables

SSH into your EC2 instance and set these environment variables:

```bash
# Add to ~/.bashrc or /etc/environment for persistence
export SPRING_PROFILES_ACTIVE=prod
export DB_PASSWORD="your-mysql-password"
export MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/covid_analytics?retryWrites=true&w=majority"
```

### 2. Build the Application

```bash
cd covid-tracker-api
mvn clean package -DskipTests
```

### 3. Run the Application

```bash
# Option A: Direct Java
java -jar target/covid-tracker-api.jar

# Option B: With nohup (background process)
nohup java -jar target/covid-tracker-api.jar > app.log 2>&1 &

# Option C: Using systemd service (recommended)
sudo systemctl start covid-tracker-api
```

## Local Development

```bash
# No environment variables needed!
# Uses application-local.properties automatically
mvn spring-boot:run
```

## Environment Profiles

| Profile | File | Use Case | Committed to Git? |
|---------|------|----------|-------------------|
| **Default** | `application.properties` | Common config for all environments | ✅ Yes |
| **Local** | `application-local.properties` | Local development (overrides with secrets) | ❌ No (.gitignore) |
| **Production** | `application-prod.properties` | AWS deployment (uses env vars) | ✅ Yes (safe) |

## Switching Between Environments

**Local Development:**
- Set in `application.properties`: `spring.profiles.active=local`
- Or leave it as default

**Production Deployment:**
- Set environment variable: `export SPRING_PROFILES_ACTIVE=prod`
- Spring Boot will use `application-prod.properties`

## Security Checklist

- ✅ `application-local.properties` is in `.gitignore`
- ✅ `application-prod.properties` has NO passwords (uses `${ENV_VAR}`)
- ✅ Environment variables are set on AWS EC2
- ✅ Passwords are never committed to Git

