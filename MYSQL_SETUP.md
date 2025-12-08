# MySQL Setup Guide

## 1. Update MySQL Configuration

Edit `src/main/resources/application-mysql.yml` with your MySQL credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fincore_db
    username: YOUR_MYSQL_USERNAME
    password: YOUR_MYSQL_PASSWORD
```

## 2. Create Database (if not exists)

```sql
CREATE DATABASE IF NOT EXISTS fincore_db;
USE fincore_db;
```

## 3. Run Application with MySQL

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=mysql
```

Or using JAR:

```bash
mvn clean package -DskipTests
java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=mysql
```

## 4. Test the API

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"Admin@123456\"}"
```

## Notes

- Tables will be created automatically (ddl-auto: update)
- Initial data (users, roles, permissions) will be loaded from `data.sql`
- Change `ddl-auto` to `none` if you don't want Hibernate to modify your schema
