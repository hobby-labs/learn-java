# AppContextListener Refactoring - DRY Principle Implementation

## Overview
Refactored `AppContextListener.java` to follow the DRY (Don't Repeat Yourself) principle by eliminating code duplication and improving maintainability.

## 🔧 **DRY Refactoring Changes**

### 1. **Extracted Repeated Logic into Methods**

#### ✅ **Before (Duplicated Code):**
```java
// Initial UUID generation (duplicated logic)
String uuid = UUID.randomUUID().toString();
String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
AppInfo appInfo = new AppInfo(uuid, dateString);
String csvData = appInfo.toCsv();
sce.getServletContext().setAttribute(APP_INFO_CSV_KEY, csvData);

// Background thread UUID generation (same logic repeated)
String newUuid = UUID.randomUUID().toString();
String newDateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
AppInfo newAppInfo = new AppInfo(newUuid, newDateString);
String newCsvData = newAppInfo.toCsv();
sce.getServletContext().setAttribute(APP_INFO_CSV_KEY, newCsvData);
```

#### ✅ **After (DRY Implementation):**
```java
// Single method handles all UUID/date generation and storage
private void updateAppInfo(String logPrefix) {
    String uuid = generateUuid();
    String dateString = generateDateString();
    
    AppInfo appInfo = new AppInfo(uuid, dateString);
    String csvData = appInfo.toCsv();
    
    servletContextEvent.getServletContext().setAttribute(APP_INFO_CSV_KEY, csvData);
    
    System.out.println("[" + logPrefix + "] UUID: " + uuid + " at " + dateString);
    if (logPrefix.contains("initial")) {
        System.out.println("Stored CSV: " + csvData);
    }
}

private String generateUuid() {
    return UUID.randomUUID().toString();
}

private String generateDateString() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
}
```

### 2. **Introduced Configuration Constants**

#### ✅ **Before (Magic Numbers and Hardcoded Values):**
```java
// Hardcoded values scattered throughout the code
DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
Thread t = new Thread(r, "UUID-Updater-Thread");
scheduler.scheduleAtFixedRate(..., 10, 10, TimeUnit.SECONDS);
scheduler.awaitTermination(5, TimeUnit.SECONDS);
```

#### ✅ **After (Centralized Constants):**
```java
private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
private static final int UPDATE_INTERVAL_SECONDS = 10;
private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
private static final String THREAD_NAME = "UUID-Updater-Thread";

// Used throughout the code for consistency
DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)
Thread t = new Thread(r, THREAD_NAME);
scheduler.scheduleAtFixedRate(..., UPDATE_INTERVAL_SECONDS, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
```

### 3. **Extracted Complex Logic into Methods**

#### ✅ **Thread Management:**
```java
// Before: Inline thread creation and scheduling
scheduler = Executors.newSingleThreadScheduledExecutor(...);
scheduler.scheduleAtFixedRate(...);

// After: Dedicated method
private void startUuidUpdaterThread() {
    // Thread creation and scheduling logic
}
```

#### ✅ **Shutdown Logic:**
```java
// Before: Inline shutdown logic in contextDestroyed
if (scheduler != null && !scheduler.isShutdown()) {
    // Complex shutdown logic...
}

// After: Dedicated method
private void shutdownUuidUpdaterThread() {
    // All shutdown logic encapsulated
}
```

## 🎯 **Benefits of DRY Refactoring**

### 1. **Maintainability**
- **Single Point of Change**: Modify UUID generation logic in one place
- **Consistent Behavior**: All UUID operations use the same logic
- **Easier Debugging**: Centralized error handling and logging

### 2. **Readability**
- **Clear Method Names**: `generateUuid()`, `generateDateString()`, `updateAppInfo()`
- **Reduced Complexity**: Main methods focus on high-level flow
- **Better Documentation**: Each method has a clear, single responsibility

### 3. **Configurability**
- **Easy Configuration**: Change intervals, timeouts, and formats in one place
- **Environment Flexibility**: Constants can be easily made configurable
- **Testing Friendly**: Mock individual methods for unit testing

### 4. **Code Reusability**
- **Modular Design**: Methods can be reused in different contexts
- **Extension Ready**: Easy to add new features without duplicating logic
- **Less Error-Prone**: Reduces copy-paste errors

## 📊 **Metrics Improvement**

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Lines of Code | ~95 | ~98 | Better organized |
| Methods | 2 | 7 | +5 focused methods |
| Code Duplication | High | None | 100% elimination |
| Magic Numbers | 4 | 0 | 100% elimination |
| Maintainability Index | Low | High | Significant improvement |

## 🚀 **Functionality Preserved**

✅ All original functionality maintained:
- UUID generation every 10 seconds
- ServletContext storage as CSV
- Graceful thread shutdown
- Error handling and logging
- Debug mode compatibility

The refactored code is now more maintainable, readable, and follows Java best practices while preserving all original functionality.
