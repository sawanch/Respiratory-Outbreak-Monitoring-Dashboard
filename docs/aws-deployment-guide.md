# AWS Deployment Guide - COVID-19 Tracker

Complete guide to deploy the COVID-19 Tracker application on AWS using a single EC2 instance with Nginx reverse proxy, MySQL, and MongoDB Atlas.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Prerequisites](#prerequisites)
3. [Step 1: Launch EC2 Instance](#step-1-launch-ec2-instance)
4. [Step 2: Allocate Elastic IP](#step-2-allocate-elastic-ip)
5. [Step 3: Install Prerequisites](#step-3-install-prerequisites)
6. [Step 4: Setup MySQL Database](#step-4-setup-mysql-database)
7. [Step 5: Setup MongoDB Atlas](#step-5-setup-mongodb-atlas)
8. [Step 6: Deploy Spring Boot Backend](#step-6-deploy-spring-boot-backend)
9. [Step 7: Deploy Frontend with Nginx](#step-7-deploy-frontend-with-nginx)
10. [Step 8: Configure Nginx Reverse Proxy](#step-8-configure-nginx-reverse-proxy)
11. [Step 9: Setup Auto-Start Services](#step-9-setup-auto-start-services)
12. [Step 10: Testing and Verification](#step-10-testing-and-verification)
13. [Troubleshooting](#troubleshooting)
14. [Cost Analysis](#cost-analysis)

---

## Architecture Overview

### System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Internet Users                         │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ HTTP/HTTPS (Port 80)
                     ▼
┌─────────────────────────────────────────────────────────┐
│              EC2 Instance (t2.micro)                    │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Nginx (Port 80)                                   │ │
│  │  ┌──────────────────┐  ┌──────────────────────┐    │ │
│  │  │ Static Files     │  │ Reverse Proxy        │    │ │
│  │  │ (Frontend)       │  │ → Spring Boot :8080  │    │ │
│  │  └──────────────────┘  └──────────────────────┘    │ │
│  └──────────────────┬─────────────────────────────────┘ │
│                     │                                   │
│  ┌──────────────────▼─────────────────────────────────┐ │
│  │  Spring Boot API (Port 8080)                       │ │
│  │  ├─ REST Endpoints (/api/*)                        │ │
│  │  └─ Systemd Service                                │ │
│  └──────┬──────────────────────┬──────────────────────┘ │
│         │                      │                          │
│         │ JDBC                 │ MongoDB Driver          │
│         ▼                      │                          │
│  ┌──────────────┐              │                          │
│  │ MySQL 8.0    │              │                          │
│  │ (Localhost)  │              │                          │
│  └──────────────┘              │                          │
└─────────────────────────────────┼──────────────────────────┘
                                  │
                                  │ HTTPS Connection
                                  ▼
┌─────────────────────────────────────────────────────────┐
│              MongoDB Atlas (Cloud)                      │
│              Analytics Data Storage                     │
└─────────────────────────────────────────────────────────┘
```

### Component Overview

**EC2 Instance**: A virtual server in AWS that hosts our entire application stack. Think of it as a remote computer you rent in the cloud that's always accessible via the internet.

**Elastic IP**: A permanent public IP address that never changes, even when you restart your EC2 instance. This ensures your application URL remains consistent and accessible.

**Nginx**: A web server that serves two critical functions:
- **Static File Server**: Delivers HTML, CSS, and JavaScript files to users' browsers
- **Reverse Proxy**: Routes API requests (`/api/*`) to the Spring Boot backend running on port 8080, while keeping the backend hidden from direct internet access

**MySQL Database**: A relational database running on the same EC2 instance that stores structured COVID-19 data (countries, confirmed cases, deaths). It uses SQL queries and provides ACID compliance for data integrity.

**MongoDB Atlas**: A cloud-hosted NoSQL database service that stores flexible analytics data (API metrics, response times, request tracking). It's managed by MongoDB and doesn't consume EC2 resources.

**Spring Boot API**: The backend application that processes requests, queries databases, and returns JSON responses. It runs as a systemd service for automatic startup and recovery.

### Why This Architecture?

- **Cost-Effective**: Uses AWS Free Tier (12 months) and MongoDB Atlas Free Tier (forever)
- **Simple Management**: Single EC2 instance is easy to manage and monitor
- **Professional**: Industry-standard reverse proxy pattern with Nginx
- **Secure**: Backend and database are not directly exposed to the internet
- **Scalable**: Can be extended with load balancers and additional EC2 instances
- **Hybrid Database**: Demonstrates polyglot persistence (SQL for structured data, NoSQL for analytics)

---

## Prerequisites

Before starting, ensure you have:

1. **AWS Account** with Free Tier eligibility
   - Sign up at https://aws.amazon.com
   - Credit card required (won't be charged within Free Tier limits)
   - Email verification completed

2. **Local Development Environment**:
   - Terminal (Mac/Linux) or PuTTY (Windows)
   - SSH client installed
   - Maven installed (`mvn --version`)
   - Git installed

3. **Application Ready**:
   - Spring Boot application builds successfully
   - JAR file can be generated (`mvn clean package`)
   - Frontend files ready for deployment
   - SQL scripts available

---

## Step 1: Launch EC2 Instance

### 1.1 Access EC2 Console

1. Log in to AWS Console: https://console.aws.amazon.com
2. Search for "EC2" in the top search bar
3. Click "EC2" to open the EC2 Dashboard

### 1.2 Launch Instance

1. Click **"Launch Instance"** button

2. **Name and Tags**:
   - Name: `covid-tracker-server`

3. **Application and OS Images (Amazon Machine Image)**:
   - Select: **Amazon Linux 2023** (Free tier eligible)
   - This is a pre-configured Linux operating system optimized for AWS

4. **Instance Type**:
   - Select: **t2.micro** (Free tier eligible)
   - Specifications: 1 vCPU, 1 GB RAM
   - Free for 750 hours/month (first 12 months)

5. **Key Pair (Login)**:
   - Click **"Create new key pair"**
   - Key pair name: `covid-tracker-key`
   - Key pair type: **RSA**
   - Private key file format: **.pem** (Mac/Linux) or **.ppk** (Windows)
   - Click **"Create key pair"**
   - **IMPORTANT**: The `.pem` file downloads automatically - save it securely! You'll need this to connect to your server.

6. **Network Settings**:
   - Click **"Edit"** to configure security groups
   - **Security Group Name**: `covid-tracker-sg`
   - **Description**: `Security group for COVID-19 Tracker`
   
   **Inbound Security Group Rules**:
   
   | Type | Port | Source | Description |
   |------|------|--------|-------------|
   | SSH | 22 | My IP | Allows you to connect via SSH |
   | HTTP | 80 | Anywhere (0.0.0.0/0) | Allows users to access the website |
   
   **Why these rules?**
   - SSH from "My IP" restricts server access to only your computer (security best practice)
   - HTTP from "Anywhere" allows anyone on the internet to access your application

7. **Configure Storage**:
   - Size: **20 GB**
   - Volume type: **gp3** (General Purpose SSD)
   - Free tier includes 30 GB, so this is free

8. **Launch Instance**:
   - Click **"Launch Instance"**
   - Wait 30-60 seconds for the instance to start

### 1.3 Verify Instance Status

1. Go to **EC2 Dashboard → Instances**
2. Find your instance: `covid-tracker-server`
3. **Instance State**: Should show "Running" (green circle)
4. **Status Checks**: Wait until "2/2 checks passed" (takes 2-3 minutes)
5. Note the **Public IPv4 address** (this is temporary - we'll fix this next)

---

## Step 2: Allocate Elastic IP

### 2.1 Why Elastic IP?

**The Problem**: When you stop and restart an EC2 instance, AWS assigns a new public IP address. This breaks bookmarks, configurations, and any references to your application.

**The Solution**: Elastic IP provides a permanent IP address that never changes, even when you restart your instance.

### 2.2 Allocate Elastic IP

1. In **EC2 Console**, left sidebar:
   - Click **"Elastic IPs"** (under Network & Security)

2. Click **"Allocate Elastic IP address"**

3. **Settings**:
   - Network Border Group: Default
   - Public IPv4 address pool: Amazon's pool

4. Click **"Allocate"**

5. **Success!** Note your Elastic IP address (e.g., `54.123.45.67`) - this is your **permanent** IP

### 2.3 Associate Elastic IP with EC2

1. **Select** the Elastic IP you just created

2. Click **"Actions" → "Associate Elastic IP address"**

3. **Settings**:
   - Resource type: **Instance**
   - Instance: Select `covid-tracker-server`
   - Private IP address: (auto-filled)

4. Click **"Associate"**

5. **Verify**: Go back to **EC2 → Instances** → Your instance now shows the Elastic IP as the Public IPv4 address

**Cost Note**: Elastic IP is FREE while attached to a running EC2 instance. If you stop the instance, you'll be charged ~$0.005/hour (~$3.60/month) until you start it again or release the IP.

---

## Step 3: Install Prerequisites

### 3.1 Connect to EC2 Instance

**On your local machine** (Mac/Linux):

```bash
# Navigate to where you saved the key file
cd ~/Downloads

# Set correct permissions (required for SSH)
chmod 400 covid-tracker-key.pem

# Connect to EC2 (replace YOUR_ELASTIC_IP with your actual Elastic IP)
ssh -i covid-tracker-key.pem ec2-user@YOUR_ELASTIC_IP

# Example:
# ssh -i covid-tracker-key.pem ec2-user@54.123.45.67
```

**First time connection**: Type `yes` when prompted about host authenticity.

**Windows users**: Use PuTTY (convert `.pem` to `.ppk` using PuTTYgen first).

### 3.2 Update System

```bash
# Update all system packages to latest versions
sudo yum update -y
```

**What this does**: Updates the Linux operating system and all installed packages, similar to Windows Update. This ensures you have the latest security patches and bug fixes.

### 3.3 Install Java 11

```bash
# Install Amazon Corretto JDK 11 (Java runtime for Spring Boot)
sudo yum install java-11-amazon-corretto -y

# Verify installation
java -version
```

**Expected output**: `openjdk version "11.0.xx"`

**Why Java 11?** Spring Boot applications require Java to run. Java 11 is a stable, long-term support version that works well with Spring Boot.

### 3.4 Install MySQL

```bash
# Install MySQL Server
sudo yum install mysql-server -y

# Start MySQL service
sudo systemctl start mysqld

# Enable MySQL to start automatically on boot
sudo systemctl enable mysqld

# Verify MySQL is running
sudo systemctl status mysqld
```

**Expected output**: `Active: active (running)`

Press `q` to exit the status view.

**Why MySQL on EC2?** For this deployment, we install MySQL directly on the EC2 instance to keep costs at zero (no separate RDS charges). For production, you might use AWS RDS for managed database services.

### 3.5 Install Nginx

```bash
# Install Nginx web server
sudo yum install nginx -y

# Start Nginx
sudo systemctl start nginx

# Enable Nginx to start automatically on boot
sudo systemctl enable nginx

# Verify Nginx is running
sudo systemctl status nginx
```

**Test Nginx**: Open your browser and go to `http://YOUR_ELASTIC_IP`. You should see the default "Welcome to nginx" page. This confirms Nginx is working and accessible from the internet.

**Why Nginx?** Nginx serves two purposes:
1. **Web Server**: Delivers static files (HTML, CSS, JavaScript) to users' browsers
2. **Reverse Proxy**: Forwards API requests to Spring Boot, keeping the backend secure and hidden

### 3.6 Create Application Directories

```bash
# Create directories for application files
mkdir -p ~/covid-tracker-api/logs
mkdir -p ~/sql-scripts
```

---

## Step 4: Setup MySQL Database

### 4.1 Secure MySQL Installation

```bash
# Run MySQL secure installation script
sudo mysql_secure_installation
```

**Follow the prompts**:

1. **"Set root password?"** → Yes
   - Enter password: `YourSecurePassword123!`
   - Re-enter password: `YourSecurePassword123!`
   - ⚠️ **SAVE THIS PASSWORD** - you'll need it!

2. **"Remove anonymous users?"** → Yes

3. **"Disallow root login remotely?"** → Yes

4. **"Remove test database?"** → Yes

5. **"Reload privilege tables now?"** → Yes

**What this does**: Secures your MySQL installation by removing default insecure configurations and setting a strong root password.

### 4.2 Create Database and User

```bash
# Connect to MySQL as root
mysql -u root -p
# Enter the root password you just set
```

You'll see the MySQL prompt: `mysql>`

```sql
-- Create database for our application
CREATE DATABASE covid_tracker;

-- Create a dedicated user for the application (more secure than using root)
CREATE USER 'covid_user'@'localhost' IDENTIFIED BY 'CovidApp123!';

-- Grant all privileges on covid_tracker database to this user
GRANT ALL PRIVILEGES ON covid_tracker.* TO 'covid_user'@'localhost';

-- Apply the privilege changes
FLUSH PRIVILEGES;

-- Switch to our database
USE covid_tracker;

-- Verify database selection
SELECT DATABASE();

-- Exit MySQL
EXIT;
```

**What we did**:
- Created database: `covid_tracker`
- Created user: `covid_user` with password: `CovidApp123!`
- Granted full access to the `covid_tracker` database
- ⚠️ **SAVE THESE CREDENTIALS** - you'll need them for Spring Boot configuration

**Why a separate user?** Using a dedicated application user instead of root follows security best practices. If the application is compromised, the attacker only has access to one database, not the entire MySQL server.

### 4.3 Import SQL Schema

**On your LOCAL machine** (open a NEW terminal window):

```bash
# Navigate to your project directory
cd /path/to/Covid-19-Tracker/covid-tracker-api

# Copy SQL scripts to EC2
scp -i covid-tracker-key.pem sql/*.sql ec2-user@YOUR_ELASTIC_IP:~/sql-scripts/
```

**Back on EC2** (in your SSH session):

```bash
# Verify files transferred
ls -l ~/sql-scripts/

# Import table structure
mysql -u covid_user -p covid_tracker < ~/sql-scripts/02_create_covid_data_table.sql
# Enter password: CovidApp123!

# Verify table created
mysql -u covid_user -p -e "USE covid_tracker; SHOW TABLES;"
```

**Expected output**: You should see `covid_data` table listed.

---

## Step 5: Setup MongoDB Atlas

### 5.1 Why MongoDB Atlas?

This application uses a **hybrid database architecture**:
- **MySQL**: Stores structured COVID-19 data (countries, cases, deaths) with ACID compliance
- **MongoDB Atlas**: Stores flexible analytics data (API metrics, response times) in the cloud

**Benefits of MongoDB Atlas**:
- ✅ **FREE Forever**: 512 MB storage (not limited to 12 months!)
- ✅ **Zero EC2 Memory**: Runs in cloud, doesn't consume your t2.micro's 1GB RAM
- ✅ **Managed Service**: No installation, backups, or updates needed
- ✅ **Better for Analytics**: Optimized for time-series metrics and flexible schemas
- ✅ **Professional**: Demonstrates knowledge of both SQL and NoSQL databases

### 5.2 Create MongoDB Atlas Account

1. Go to: https://www.mongodb.com/cloud/atlas/register
2. Sign up with email or use Google/GitHub sign-in
3. Verify your email
4. **No credit card required!** ✅

### 5.3 Create Free Cluster

1. After login, click **"Create"** or **"Build a Database"**

2. **Choose deployment type**:
   - Select: **"M0 - FREE"** (Shared)
   - Shows: "FREE Forever" badge

3. **Cloud Provider & Region**:
   - Provider: **AWS**
   - Region: **US East (N. Virginia) us-east-1** (or same region as your EC2)
   - **Why same region?** Lower latency between EC2 and MongoDB

4. **Cluster Name**: `covid-analytics-cluster`

5. Click **"Create Cluster"**
   - Takes 3-5 minutes to provision

### 5.4 Create Database User

1. Left sidebar: **Security → Database Access**

2. Click **"Add New Database User"**

3. **Authentication Method**: Password

4. **Username**: `covid_admin`

5. **Password**: Click **"Autogenerate Secure Password"**
   - ⚠️ **CRITICAL**: Click **"Copy"** and **SAVE THIS PASSWORD!**
   - Example: `xK9mP2nR5qT8vW`

6. **Database User Privileges**: **"Read and write to any database"**

7. Click **"Add User"**

### 5.5 Configure Network Access

1. Left sidebar: **Security → Network Access**

2. Click **"Add IP Address"**

3. **Two options**:

   **Option A: Allow from anywhere** (Easier, less secure):
   - Click **"Allow Access from Anywhere"**
   - Shows: `0.0.0.0/0`
   - Good for: Demo/learning purposes
   
   **Option B: Whitelist EC2 IP** (More secure - Recommended):
   - Manually enter your **Elastic IP**: `YOUR_ELASTIC_IP/32`
   - Good for: Production-like setup

4. **Comment**: `COVID Tracker EC2 Instance`

5. Click **"Confirm"**

**Why IP whitelisting?** MongoDB Atlas requires you to explicitly allow IP addresses that can connect. This is a security feature that prevents unauthorized access.

### 5.6 Get Connection String

1. **Database → Clusters** → Your cluster should show "Active" (green)

2. Click **"Connect"** button

3. Choose: **"Connect your application"**

4. **Driver**: Java, Version: 4.3 or later

5. **Connection String** shown:
   ```
   mongodb+srv://covid_admin:<password>@covid-analytics-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```

6. **⚠️ IMPORTANT**: Copy this string and modify it:
   
   ```
   # Replace <password> with the password you saved earlier
   # Add database name: /covid_analytics before the ?
   
   # Final connection string:
   mongodb+srv://covid_admin:xK9mP2nR5qT8vW@covid-analytics-cluster.ab1cd.mongodb.net/covid_analytics?retryWrites=true&w=majority
   ```

   **Key changes**:
   - `<password>` → Your actual password (no angle brackets!)
   - Added `/covid_analytics` before `?` (database name)

7. **Save this final connection string** - you'll add it to application config next

---

## Step 6: Deploy Spring Boot Backend

### 6.1 Build JAR File (On Local Machine)

**On your LOCAL machine** (new terminal):

```bash
# Navigate to backend directory
cd /path/to/Covid-19-Tracker/covid-tracker-api

# Build the application (skip tests for faster build)
mvn clean package -DskipTests

# Verify JAR created
ls -lh target/*.jar
```

**What this does**: Compiles your Spring Boot application and packages it into a single JAR file (Java Archive) that contains all dependencies. This JAR file is what runs on the server.

### 6.2 Transfer JAR to EC2

```bash
# Still on LOCAL machine
scp -i covid-tracker-key.pem \
    target/covid-tracker-api-*.jar \
    ec2-user@YOUR_ELASTIC_IP:~/covid-tracker-api/

# Also transfer the CSV data file (if needed)
scp -i covid-tracker-key.pem \
    src/main/resources/data/covid19_confirmed_global.csv \
    ec2-user@YOUR_ELASTIC_IP:~/covid-tracker-api/
```

### 6.3 Create Production Configuration

**Back on EC2** (SSH session):

```bash
# Navigate to app directory
cd ~/covid-tracker-api

# Create production properties file
nano application-prod.properties
```

**Paste this configuration** (update values as needed):

```properties
# Application Name
spring.application.name=covid-tracker-api

# Active Profile
spring.profiles.active=prod

# Server Configuration
server.port=8080

# CSV Data File Location
covid.data.file=file:/home/ec2-user/covid-tracker-api/covid19_confirmed_global.csv

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/covid_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=covid_user
spring.datasource.password=CovidApp123!
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# HikariCP Connection Pool (optimizes database connections)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# MongoDB Atlas Configuration (for analytics/metrics)
# IMPORTANT: Replace with YOUR connection string from Step 5.6
spring.data.mongodb.uri=mongodb+srv://covid_admin:YOUR_PASSWORD@covid-analytics-cluster.xxxxx.mongodb.net/covid_analytics?retryWrites=true&w=majority

# Logging Configuration
logging.level.root=INFO
logging.level.com.covidtracker.api=INFO
logging.file.name=logs/covid-tracker-api.log
logging.file.max-size=10MB
logging.file.max-history=7

# Actuator Configuration (for health checks and metrics)
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# CORS Configuration (will be handled by Nginx)
cors.allowed.origins=*
```

**Save and exit**: Press `Ctrl + X`, then `Y`, then `Enter`

**What this configuration does**:
- Sets up database connections (MySQL and MongoDB)
- Configures logging to files
- Enables health check endpoints
- Sets up connection pooling for efficient database access

### 6.4 Test Backend Manually

```bash
# Try running the application manually first (to test)
cd ~/covid-tracker-api

java -jar \
    -Dspring.config.location=file:/home/ec2-user/covid-tracker-api/application-prod.properties \
    covid-tracker-api-*.jar
```

**You should see**: Spring Boot startup logs ending with "Started CovidTrackerApiApplication"

**Test the API** (open a NEW terminal on your local machine):

```bash
# Test global stats
curl http://YOUR_ELASTIC_IP:8080/api/global

# Test countries list
curl http://YOUR_ELASTIC_IP:8080/api/countries
```

If you see JSON responses, **SUCCESS!** ✅

**Stop the application** (on EC2): Press `Ctrl + C`

### 6.5 Create Systemd Service (Auto-Start)

**What is systemd?** Systemd is Linux's service manager. It runs your application as a background service that automatically starts when the server boots and restarts if it crashes.

```bash
# Create systemd service file
sudo nano /etc/systemd/system/covid-tracker-api.service
```

**Paste this content**:

```ini
[Unit]
Description=COVID-19 Tracker Spring Boot API
After=network.target mysqld.service
Requires=mysqld.service

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/covid-tracker-api
ExecStart=/usr/bin/java -jar -Dspring.config.location=file:/home/ec2-user/covid-tracker-api/application-prod.properties /home/ec2-user/covid-tracker-api/covid-tracker-api-1.0.0.jar
Restart=always
RestartSec=10
StandardOutput=append:/home/ec2-user/covid-tracker-api/logs/application.log
StandardError=append:/home/ec2-user/covid-tracker-api/logs/error.log

# Resource limits (important for t2.micro with 1GB RAM)
MemoryLimit=512M
MemoryMax=768M

[Install]
WantedBy=multi-user.target
```

**Note**: Update the JAR filename in `ExecStart` to match your actual JAR file name.

**Save and exit** (`Ctrl+X`, `Y`, `Enter`)

**Start the service**:

```bash
# Reload systemd (to recognize new service)
sudo systemctl daemon-reload

# Enable service (start on boot)
sudo systemctl enable covid-tracker-api

# Start service now
sudo systemctl start covid-tracker-api

# Check status
sudo systemctl status covid-tracker-api
```

**Expected output**: `Active: active (running)`

Press `q` to exit.

**View logs**:
```bash
# View live logs
tail -f ~/covid-tracker-api/logs/application.log

# Press Ctrl+C to stop viewing
```

---

## Step 7: Deploy Frontend with Nginx

### 7.1 Update Frontend Configuration

**On your LOCAL machine**:

```bash
# Navigate to frontend directory
cd /path/to/Covid-19-Tracker/covid-tracker-ui

# Edit config.js
nano js/config.js
```

**Update with your Elastic IP**:

```javascript
const CONFIG = {
    // Use your Elastic IP - everything goes through Nginx on port 80
    API_BASE_URL: 'http://YOUR_ELASTIC_IP/api',
    
    // Swagger is accessible through proxy too
    SWAGGER_UI_URL: 'http://YOUR_ELASTIC_IP/swagger-ui/index.html'
};
```

**Save changes** (`Ctrl+X`, `Y`, `Enter`)

### 7.2 Transfer Frontend Files to EC2

```bash
# Still on LOCAL machine, in covid-tracker-ui directory

# Transfer all frontend files
scp -i covid-tracker-key.pem -r \
    index.html analytics.html css/ js/ assets/ \
    ec2-user@YOUR_ELASTIC_IP:~/frontend-files/
```

### 7.3 Move Files to Nginx Directory

**On EC2** (SSH session):

```bash
# Move files to Nginx web root
sudo cp -r ~/frontend-files/* /usr/share/nginx/html/

# Set correct permissions
sudo chown -R nginx:nginx /usr/share/nginx/html
sudo chmod -R 755 /usr/share/nginx/html

# Verify files are there
ls -la /usr/share/nginx/html/
```

**You should see**: `index.html`, `analytics.html`, `css/`, `js/`, `assets/` directories

---

## Step 8: Configure Nginx Reverse Proxy

### 8.1 What is a Reverse Proxy?

**Reverse Proxy** is a server that sits between clients and backend servers. It:
- Receives requests from clients
- Forwards them to the appropriate backend server
- Returns the response to the client

**Why use it?**
- **Security**: Hides backend servers from direct internet access
- **Single Entry Point**: Users access everything through one URL (port 80)
- **Load Balancing**: Can distribute requests across multiple backend servers
- **SSL Termination**: Can handle HTTPS encryption

### 8.2 Create Nginx Configuration

```bash
# Backup default config
sudo cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.backup

# Create our application config
sudo nano /etc/nginx/conf.d/covid-tracker.conf
```

**Paste this configuration**:

```nginx
# COVID-19 Tracker Application Configuration

# Upstream backend (Spring Boot API)
upstream covid_tracker_backend {
    server localhost:8080;
}

server {
    # Listen on port 80 (HTTP)
    listen 80;
    listen [::]:80;
    
    # Server name (use your Elastic IP or domain)
    server_name YOUR_ELASTIC_IP;
    
    # Root directory for frontend files
    root /usr/share/nginx/html;
    index index.html;
    
    # Logging
    access_log /var/log/nginx/covid-tracker-access.log;
    error_log /var/log/nginx/covid-tracker-error.log;
    
    # Serve static frontend files
    location / {
        try_files $uri $uri/ /index.html;
        
        # Cache static files for better performance
        location ~* \.(css|js|jpg|jpeg|png|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # Reverse proxy for API requests
    location /api/ {
        proxy_pass http://covid_tracker_backend;
        proxy_http_version 1.1;
        
        # Headers (preserve client information)
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Reverse proxy for Actuator endpoints (health checks)
    location /actuator/ {
        proxy_pass http://covid_tracker_backend;
        proxy_http_version 1.1;
        
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Reverse proxy for Swagger UI (API documentation)
    location /swagger-ui/ {
        proxy_pass http://covid_tracker_backend;
        proxy_http_version 1.1;
        
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Gzip compression for better performance
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/json application/javascript;
}
```

**Replace `YOUR_ELASTIC_IP`** with your actual Elastic IP in the `server_name` line.

**Save and exit** (`Ctrl+X`, `Y`, `Enter`)

### 8.3 Test and Restart Nginx

```bash
# Test configuration for syntax errors
sudo nginx -t
```

**Expected output**: `nginx: the configuration file /etc/nginx/nginx.conf syntax is ok`

If there are errors, check your configuration file for typos.

```bash
# Restart Nginx to apply new configuration
sudo systemctl restart nginx

# Check status
sudo systemctl status nginx
```

**Expected**: `Active: active (running)`

---

## Step 9: Setup Auto-Start Services

Ensure all services start automatically when EC2 reboots.

```bash
# Verify services are enabled
sudo systemctl is-enabled mysqld
sudo systemctl is-enabled nginx
sudo systemctl is-enabled covid-tracker-api

# All should output: enabled
```

If any show "disabled", enable them:

```bash
sudo systemctl enable SERVICE_NAME
```

**Test reboot** (optional but recommended):

```bash
# Reboot EC2 instance
sudo reboot
```

**Wait 2-3 minutes**, then:

1. **SSH back in**:
   ```bash
   ssh -i covid-tracker-key.pem ec2-user@YOUR_ELASTIC_IP
   ```

2. **Check all services**:
   ```bash
   sudo systemctl status mysqld
   sudo systemctl status nginx
   sudo systemctl status covid-tracker-api
   ```

All should be "active (running)" ✅

3. **Test application** in browser: `http://YOUR_ELASTIC_IP`

---

## Step 10: Testing and Verification

### 10.1 Test Frontend

**Open your browser**:
```
http://YOUR_ELASTIC_IP
```

**You should see**:
- COVID-19 Tracker homepage
- Global statistics displayed
- Country list loads
- No 404 errors

### 10.2 Test Backend API

**From terminal** (local machine):

```bash
# Test global stats through Nginx
curl http://YOUR_ELASTIC_IP/api/global

# Test countries
curl http://YOUR_ELASTIC_IP/api/countries

# Test specific country
curl http://YOUR_ELASTIC_IP/api/countries/USA

# Test health check
curl http://YOUR_ELASTIC_IP/actuator/health
```

**Expected**: JSON responses for all endpoints

### 10.3 Test Complete User Flow

**In your browser**:

1. Open: `http://YOUR_ELASTIC_IP`
2. Verify: Global stats display at top
3. Verify: Country list loads
4. Test: Search for a country
5. Test: Sort by different columns
6. Check: Browser console (F12) - no errors
7. Test: Analytics page: `http://YOUR_ELASTIC_IP/analytics.html`

### 10.4 Test from Different Network

**Most important test**:
- Use your phone (disconnect from WiFi, use mobile data)
- Open: `http://YOUR_ELASTIC_IP`
- Should work perfectly!

This proves your application is accessible from anywhere on the internet.

---

## Troubleshooting

### Issue 1: Cannot SSH into EC2

**Symptoms**: `Permission denied (publickey)` or `Connection timeout`

**Solutions**:

1. **Check key file permissions**:
   ```bash
   chmod 400 covid-tracker-key.pem
   ```

2. **Check Security Group**:
   - EC2 Console → Security Groups
   - Inbound rules: SSH (port 22) from your IP
   - Your IP changed? Update the rule

3. **Verify instance is running**:
   - EC2 Console → Instances
   - State should be "Running"

### Issue 2: Website Shows "Welcome to Nginx"

**Symptoms**: Default nginx page instead of your app

**Solutions**:

1. **Check frontend files**:
   ```bash
   ls -la /usr/share/nginx/html/
   # Should show index.html, css/, js/, etc.
   ```

2. **Check nginx config**:
   ```bash
   cat /etc/nginx/conf.d/covid-tracker.conf
   # Verify root path: /usr/share/nginx/html
   ```

3. **Restart nginx**:
   ```bash
   sudo systemctl restart nginx
   ```

4. **Clear browser cache** or use incognito mode

### Issue 3: API Requests Fail (404 or 502)

**Symptoms**: Frontend loads but no data, console shows errors

**Solutions**:

1. **Check Spring Boot is running**:
   ```bash
   sudo systemctl status covid-tracker-api
   # Should be "active (running)"
   ```

2. **Check application logs**:
   ```bash
   tail -f ~/covid-tracker-api/logs/application.log
   tail -f ~/covid-tracker-api/logs/error.log
   ```

3. **Test API directly** (bypass nginx):
   ```bash
   curl http://localhost:8080/api/global
   ```
   - Works? Nginx proxy issue
   - Doesn't work? Spring Boot issue

4. **Check nginx error logs**:
   ```bash
   sudo tail -f /var/log/nginx/covid-tracker-error.log
   ```

### Issue 4: Database Connection Errors

**Symptoms**: API logs show "Unable to connect to database"

**Solutions**:

1. **Check MySQL is running**:
   ```bash
   sudo systemctl status mysqld
   ```

2. **Test MySQL connection**:
   ```bash
   mysql -u covid_user -p covid_tracker
   # Enter password: CovidApp123!
   ```

3. **Verify application-prod.properties**:
   ```bash
   cat ~/covid-tracker-api/application-prod.properties
   # Check username, password, URL
   ```

### Issue 5: MongoDB Atlas Connection Errors

**Symptoms**: Analytics not working, "Unable to connect to MongoDB" in logs

**Solutions**:

1. **Check connection string format**:
   ```bash
   cat ~/covid-tracker-api/application-prod.properties | grep mongodb
   ```
   
   Should look like:
   ```
   mongodb+srv://covid_admin:PASSWORD@cluster.mongodb.net/covid_analytics?retryWrites=true&w=majority
   ```
   
   **Common mistakes**:
   - ❌ `<password>` still has angle brackets
   - ❌ Missing `/covid_analytics` before the `?`
   - ❌ Wrong password

2. **Verify Network Access in MongoDB Atlas**:
   - Go to: https://cloud.mongodb.com
   - **Security → Network Access**
   - Check: Your Elastic IP is whitelisted

3. **Check MongoDB Atlas cluster is running**:
   - **Database → Clusters**
   - Status should be: **"Active"** (green dot)

---

## Cost Analysis

### FREE Tier (First 12 Months)

✅ **EC2 t2.micro**: 750 hours/month FREE  
✅ **EBS Storage (20 GB)**: 30 GB FREE  
✅ **Elastic IP**: FREE while attached to running EC2  
✅ **Data Transfer**: 1 GB outbound FREE  
✅ **MongoDB Atlas M0**: FREE Forever (512 MB storage)

**Total Monthly Cost: $0** (within Free Tier) 🎉

### After Free Tier (Month 13+)

💰 **EC2 t2.micro**: ~$8.50/month  
💰 **EBS Storage (20 GB gp3)**: ~$1.60/month  
💰 **Elastic IP**: $0 (while attached)  
💰 **Data Transfer**: $0.09/GB after 1st GB  
✅ **MongoDB Atlas M0**: $0 (still FREE forever!)

**Total: ~$10-12/month** (MongoDB stays free!)

### Cost Saving Tips

1. **Stop EC2 when not demoing**: Saves EC2 costs, but Elastic IP will charge ~$3.60/month if instance is stopped
2. **Release Elastic IP if stopping long-term**: You'll get a new IP when you restart
3. **Use CloudWatch Billing Alerts**: Set alert for $1 threshold
4. **Delete everything when done**: Terminate EC2, release Elastic IP, delete volumes

---

## Quick Reference Commands

### Service Management

```bash
# Check status
sudo systemctl status covid-tracker-api
sudo systemctl status nginx
sudo systemctl status mysqld

# Restart services
sudo systemctl restart covid-tracker-api
sudo systemctl restart nginx
sudo systemctl restart mysqld
```

### Logs

```bash
# Application logs
tail -f ~/covid-tracker-api/logs/application.log

# Nginx logs
sudo tail -f /var/log/nginx/covid-tracker-error.log
```

### Testing

```bash
# Test API endpoints
curl http://YOUR_ELASTIC_IP/api/global
curl http://YOUR_ELASTIC_IP/api/countries

# Test directly (bypass nginx)
curl http://localhost:8080/api/global
```

---

## Interview Talking Points

When demonstrating your deployed application:

1. **"I deployed this on AWS using a single EC2 instance with a hybrid database architecture—MySQL for transactional data and MongoDB Atlas for analytics."**

2. **"I used Nginx as a reverse proxy, which is a production best practice. It serves the frontend and proxies API requests to Spring Boot."**

3. **"The architecture demonstrates polyglot persistence: MySQL for structured COVID data with ACID compliance, and MongoDB for flexible analytics metrics stored in the cloud."**

4. **"Everything auto-starts on reboot, with proper logging, monitoring through Spring Boot Actuator, and analytics dashboards."**

5. **"I can scale this horizontally by adding more EC2 instances behind a load balancer. MongoDB Atlas auto-scales the analytics database."**

6. **"Security is implemented through AWS Security Groups (port 80 only exposed), MongoDB Atlas IP whitelisting, and localhost-only access for MySQL."**

7. **"This stays completely free using AWS Free Tier (12 months) and MongoDB Atlas M0 (free forever), perfect for portfolio projects."**

---

**Congratulations! 🎉 Your application is now deployed and accessible from anywhere in the world at:**  
`http://YOUR_ELASTIC_IP`
