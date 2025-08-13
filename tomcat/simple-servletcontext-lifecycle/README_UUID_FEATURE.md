# UUID and Date CSV Feature Implementation with Auto-Update

## Overview
This feature generates a UUID and date string when the application starts, stores them as CSV in the ServletContext, and **automatically updates the UUID every 10 seconds** using a background thread. API endpoints provide access to retrieve the latest information as JSON.

## Implementation Details

### 1. AppInfo Model (`AppInfo.java`)
- Represents the UUID and date string data
- Provides methods to convert to/from CSV format
- Located in `com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model`

### 2. Enhanced ServletContextListener (`AppContextListener.java`) - **NEW: Background Thread**
- Generates initial UUID using `UUID.randomUUID().toString()`
- Creates formatted date string using `LocalDateTime.now()`
- Converts data to CSV format and stores in ServletContext
- **NEW: Starts a background thread that updates UUID every 10 seconds**
- **NEW: Properly manages thread lifecycle on application shutdown**
- Logs all generated values for debugging

### 3. Background Thread Features
- **ScheduledExecutorService**: Uses a single-threaded scheduler for UUID updates
- **Daemon Thread**: Configured as daemon thread to not prevent JVM shutdown
- **10-Second Interval**: Updates UUID and timestamp every 10 seconds
- **Thread Safety**: Updates ServletContext atomically
- **Graceful Shutdown**: Properly terminates thread on application shutdown
- **Error Handling**: Catches and logs any exceptions during updates

### 4. AppInfo Controller (`AppInfoController.java`)
- Endpoint: `/api/appinfo`
- Retrieves latest CSV data from ServletContext
- Parses CSV back to AppInfo object
- Returns current data as JSON using existing ApiResponse structure

### 5. Enhanced User Controller (`UserController.java`)
- Modified to include latest app info in the response
- Demonstrates accessing ServletContext data from any controller
- Returns both user data and current app info in combined response

## API Endpoints

### GET /api/appinfo
Returns the UUID and date string generated at application startup.

**Response Format:**
```json
{
  "success": true,
  "message": "App info retrieved successfully",
  "data": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "dateString": "2025-08-13 00:51:21"
  },
  "timestamp": 1723477881000
}
```

### GET /api/users/*
Enhanced to include both user data and app info.

**Response Format:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "users": [
      {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com",
        "age": 30
      }
    ],
    "appInfo": {
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "dateString": "2025-08-13 00:51:21"
    }
  },
  "timestamp": 1723477881000
}
```

## CSV Storage Format
The data is stored in ServletContext as a simple CSV string:
```
550e8400-e29b-41d4-a716-446655440000,2025-08-13 00:51:21
```

## Console Output
When the application starts, you'll see output like:
```
Hello!
Application started successfully at Wed Aug 13 02:08:55 JST 2025
Generated initial UUID: 550e8400-e29b-41d4-a716-446655440000
Generated Date: 2025-08-13 02:08:55
Stored CSV: 550e8400-e29b-41d4-a716-446655440000,2025-08-13 02:08:55
UUID updater thread started - will update every 10 seconds
[UUID-Updater] Updated UUID: 123e4567-e89b-12d3-a456-426614174000 at 2025-08-13 02:09:05
[UUID-Updater] Updated UUID: 987fcdeb-51a2-43c1-9876-543210987654 at 2025-08-13 02:09:15
```

When the application shuts down:
```
Good bye!
Shutting down UUID updater thread...
UUID updater thread shut down completed
Application shutdown completed at Wed Aug 13 02:15:30 JST 2025
```

## Real-Time Features
- **Auto-Update**: UUID automatically changes every 10 seconds
- **Live Data**: API calls always return the most current UUID and timestamp
- **Thread Management**: Background thread properly starts and stops with application lifecycle
- **No Manual Intervention**: Updates happen automatically without user action

## Usage
1. Deploy the application to Tomcat
2. The UUID and date will be automatically generated on startup
3. Access `/api/appinfo` to get just the app info
4. Access `/api/users` to get users with app info included
5. The same UUID and date will be returned until the application is restarted

## Features
- Thread-safe storage using ServletContext
- Automatic generation on application startup
- CSV format for lightweight storage
- JSON API responses using existing infrastructure
- Error handling for missing or corrupt data
- Reusable across multiple controllers
