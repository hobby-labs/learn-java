# UUID Persistence Strategies with Expiration Management

## Problem Statement
The original implementation lost UUIDs when the application restarted, creating new UUIDs even if previous ones hadn't expired. This document outlines several strategies to maintain UUID consistency across application restarts.

## 🎯 **Implemented Solution: File-Based Persistence**

### Strategy Overview
- **Persistence**: Store UUID with creation and expiration timestamps in a properties file
- **Expiration Management**: Check UUID validity on startup and periodically
- **Auto-Renewal**: Generate new UUID only when current one expires
- **Restart Recovery**: Reuse valid UUIDs after application restart

### Implementation Components

#### 1. **UuidPersistenceUtil** - Core Persistence Logic
```java
// File location: uuid-data/uuid-persistence.properties
// Contains: uuid, created.time, expires.time
public class UuidPersistenceUtil {
    public static class UuidInfo {
        private final String uuid;
        private final LocalDateTime createdTime;
        private final LocalDateTime expiresTime;
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresTime);
        }
    }
}
```

#### 2. **Enhanced AppContextListener** - Startup Logic
```java
@Override
public void contextInitialized(ServletContextEvent sce) {
    // 1. Try to load existing UUID
    UuidInfo persistedUuid = persistenceUtil.loadUuidInfo();
    
    // 2. Check if valid and not expired
    if (persistedUuid != null && !persistedUuid.isExpired()) {
        // Use existing UUID
        updateServletContextWithUuid(...);
    } else {
        // Generate new UUID with expiration
        generateAndStoreNewUuid(...);
    }
    
    // 3. Start background checker for expiration
    startUuidUpdaterThread();
}
```

#### 3. **Background Expiration Checker**
```java
private void checkAndUpdateExpiredUuid() {
    UuidInfo persistedUuid = persistenceUtil.loadUuidInfo();
    
    if (persistedUuid == null || persistedUuid.isExpired()) {
        generateAndStoreNewUuid("UUID-Auto-Renewal");
    }
}
```

### Configuration
```java
// UUID expires after 10 minutes
private static final int UUID_EXPIRATION_MINUTES = 10;

// Check expiration every 2 minutes (120 seconds)
private static final int UPDATE_INTERVAL_SECONDS = 120;
```

## 🔄 **Application Lifecycle Scenarios**

### Scenario 1: Fresh Start (No Previous UUID)
```
1. Application starts
2. No persistence file found
3. Generate new UUID (expires in 10 minutes)
4. Save to file: uuid-data/uuid-persistence.properties
5. Start expiration checker (every 2 minutes)
```

### Scenario 2: Restart with Valid UUID
```
1. Application restarts
2. Load UUID from persistence file
3. Check: UUID created 5 minutes ago, expires in 5 minutes
4. ✅ Use existing UUID (no new generation)
5. Continue with existing UUID until expiration
```

### Scenario 3: Restart with Expired UUID
```
1. Application restarts
2. Load UUID from persistence file
3. Check: UUID created 12 minutes ago, expired 2 minutes ago
4. ❌ Generate new UUID (old one expired)
5. Save new UUID to persistence file
```

### Scenario 4: Runtime Expiration
```
1. Application running normally
2. Background checker runs every 2 minutes
3. Detects UUID expired
4. Generate new UUID automatically
5. Update persistence file and ServletContext
```

## 📊 **API Response Examples**

### Enhanced AppInfo Response
```json
{
  "success": true,
  "message": "App info retrieved successfully",
  "data": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "dateString": "2025-08-13 02:30:00",
    "expiresAt": "2025-08-13 02:40:00",
    "expired": false
  },
  "timestamp": 1755019800000
}
```

## 🛠 **Alternative Strategies**

### Strategy 2: Database-Based Persistence
```java
// For production applications with database
public class DatabaseUuidPersistence {
    public void saveUuid(String uuid, LocalDateTime expiresAt) {
        // INSERT INTO uuid_store (uuid, created_at, expires_at) VALUES (?, ?, ?)
    }
    
    public UuidInfo loadValidUuid() {
        // SELECT * FROM uuid_store WHERE expires_at > NOW() ORDER BY created_at DESC LIMIT 1
    }
}
```

### Strategy 3: Redis/Cache-Based Persistence
```java
// For distributed applications
public class RedisUuidPersistence {
    public void saveUuid(String uuid, int ttlSeconds) {
        // Redis: SET app:uuid "uuid-value" EX 600
    }
    
    public String loadUuid() {
        // Redis: GET app:uuid (returns null if expired)
    }
}
```

### Strategy 4: Environment Variable Override
```java
// For containerized deployments
public class EnvironmentUuidStrategy {
    public String getUuid() {
        String envUuid = System.getenv("APP_UUID");
        if (envUuid != null && isValidUuid(envUuid)) {
            return envUuid; // Use externally provided UUID
        }
        return generateOrLoadPersistedUuid();
    }
}
```

## 🔒 **Security Considerations**

### File Permissions
```bash
# Ensure only application can read/write persistence file
chmod 600 uuid-data/uuid-persistence.properties
```

### UUID Validation
```java
private boolean isValidUuid(String uuid) {
    try {
        UUID.fromString(uuid);
        return true;
    } catch (IllegalArgumentException e) {
        return false;
    }
}
```

## 📈 **Monitoring and Logging**

### Key Log Messages
```
[Generated initial UUID] UUID: abc123... at 2025-08-13 02:30:00
UUID will expire at: 2025-08-13 02:40:00

[UUID-Expiration-Check] UUID still valid: abc123... (expires: 2025-08-13 02:40:00)

[UUID-Expiration-Check] UUID expired: abc123...
[UUID-Auto-Renewal] UUID: def456... at 2025-08-13 02:41:00
```

### Metrics to Monitor
- UUID generation frequency
- Expiration check frequency
- File I/O operations
- Application restart recovery success rate

## 🎯 **Benefits of File-Based Strategy**

✅ **Persistence**: UUIDs survive application restarts
✅ **Expiration Management**: Automatic cleanup of expired UUIDs
✅ **No External Dependencies**: Uses local file system
✅ **Configurable TTL**: Easy to adjust expiration time
✅ **Graceful Recovery**: Handles corrupted persistence files
✅ **Monitoring**: Clear logging for debugging
✅ **Thread-Safe**: Atomic file operations

## 🚀 **Production Recommendations**

1. **Monitor Disk Space**: Ensure persistence directory has sufficient space
2. **Backup Strategy**: Include uuid-data directory in backups
3. **Log Rotation**: Monitor log output for UUID operations
4. **Health Checks**: Verify UUID validity in application health endpoints
5. **Configuration**: Make expiration time configurable via properties
6. **Cleanup**: Implement cleanup of old persistence files if needed

This implementation provides a robust solution for UUID persistence with expiration management that handles all the scenarios you mentioned! 🎊
