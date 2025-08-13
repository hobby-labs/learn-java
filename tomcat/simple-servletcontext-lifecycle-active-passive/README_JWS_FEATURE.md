# JWS (JSON Web Signature) Feature Implementation with User Data

## Overview
This feature generates JWS (JSON Web Signature) tokens signed with ES256 algorithm when the application starts, **includes user data in the payload**, and stores them with expiration info in the ServletContext. The system **automatically updates the JWS tokens every 10 minutes** using a background thread. API endpoints provide access to retrieve the latest JWS tokens in a simplified JSON format.

## JWS Specifications
- **Header**: `{"typ":"JWT","alg":"ES256"}`
- **Payload**: User data from the repository (all users)
- **Algorithm**: ES256 (ECDSA using P-256 and SHA-256)
- **Private Key**: Hardcoded PKCS#8 format (provided in requirements)

## Response Format
All endpoints now return a simplified format:
```json
{
  "jws": "<JWS_TOKEN_VALUE>"
}
```

## Implementation Details

### 1. JWS Utility (`JwsUtil.java`)
- Handles JWS generation using the Nimbus JOSE library
- Uses the provided ES256 private key for signing
- **Enhanced**: Now supports custom payload data (user data)
- Includes proper error handling and validation

### 2. JWS Management Service (`JwsManagementService.java`)
- Manages JWS lifecycle including generation, expiration checking, and persistence
- **Enhanced**: Integrates with UserService to include user data in JWS payload
- Encapsulates all JWS-related business logic
- Handles expiration (10 minutes) and automatic renewal

### 3. JWS Persistence Utility (`JwsPersistenceUtil.java`)
- Persists JWS tokens with creation and expiration timestamps
- Stores data in `jws-data/jws-persistence.properties`
- Enables JWS persistence across application restarts
- Handles loading and saving of JWS information

### 4. Enhanced AppInfo Model (`AppInfo.java`)
- Updated to store JWS tokens instead of UUIDs
- Maintains backward compatibility with `getUuid()` method
- Includes expiration tracking
- Supports CSV format for backward compatibility

### 5. Simplified Controllers
- **AppInfoController**: Returns only `{"jws": "<token>"}`
- **UserController**: Returns only `{"jws": "<token>"}` (user data is embedded in JWS)
- **Enhanced**: User data is now embedded within the JWS payload instead of separate response fields

## API Endpoints

### GET /api/appinfo
Returns the current JWS token containing user data.

**Response Format:**
```json
{
  "jws": "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJkYXRhIjpbeyJpZCI6MSwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwiYWdlIjozMH0seyJpZCI6MiwibmFtZSI6IkphbmUgU21pdGgiLCJlbWFpbCI6ImphbmVAZXhhbXBsZS5jb20iLCJhZ2UiOjI1fSx7ImlkIjozLCJuYW1lIjoiQm9iIEpvaG5zb24iLCJlbWFpbCI6ImJvYkBleGFtcGxlLmNvbSIsImFnZSI6MzV9XSwiaWF0IjoxNzU1MDc4OTMzfQ.nmWcsfe2BBWiVfzDx5QAXQyb9GOrtiCoaurLXvKTOYdal-gjFg11uKI6Cqq_3CdAj1BhX0PfjHKksI7lqUBPBQ"
}
```

### GET /api/users/*
Returns the same JWS token (user data is embedded in the JWS payload).

**Response Format:**
```json
{
  "jws": "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJkYXRhIjpbeyJpZCI6MSwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwiYWdlIjozMH1dLCJpYXQiOjE3NTUwNzg5MzN9.signature"
}
```

## JWS Token Structure

### Decoded Header:
```json
{
  "typ": "JWT",
  "alg": "ES256"
}
```

### Decoded Payload (Contains User Data):
```json
{
  "data": [
    {
      "id": 1,
      "name": "John Doe", 
      "email": "john@example.com",
      "age": 30
    },
    {
      "id": 2,
      "name": "Jane Smith",
      "email": "jane@example.com", 
      "age": 25
    },
    {
      "id": 3,
      "name": "Bob Johnson",
      "email": "bob@example.com",
      "age": 35
    }
  ],
  "iat": 1755078933
}
```

### Signature:
- Generated using ES256 algorithm with the provided private key
- Ensures token authenticity and integrity

## Key Features
- **User Data in JWS**: All user data is embedded in the JWS payload
- **Simplified Response**: Clean `{"jws": "<token>"}` format
- **Automatic JWS Generation**: Creates JWS tokens on startup using ES256 algorithm  
- **Expiration Management**: Tokens expire after 10 minutes with automatic renewal
- **Persistence**: JWS tokens survive application restarts if not expired
- **Background Monitoring**: Checks expiration every 2 minutes
- **Error Handling**: Graceful fallback if JWS generation fails
- **Thread Safety**: Daemon threads don't prevent application shutdown

## Console Output
When the application starts, you'll see output like:
```
Hello!
No persistence file found: jws-data/jws-persistence.properties
[JWS-Service] No persisted JWS found, generating new one
JWS info saved to: jws-data/jws-persistence.properties
[JWS-Service] Generated new JWS with user data at 2025-08-13 18:55:33
[JWS-Service] JWS will expire at: 2025-08-13 19:05:33
Application started successfully at Wed Aug 13 18:55:33 JST 2025
JWS expiration checker started - will check every 120 seconds
```

## Testing JWS Tokens
You can decode and verify the JWS tokens using online tools or command line:

```bash
# Decode header (same as before)
echo "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9" | base64 -d | jq .

# Decode payload (now contains user data)
echo "eyJkYXRhIjpbeyJpZCI6MSwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwiYWdlIjozMH0seyJpZCI6MiwibmFtZSI6IkphbmUgU21pdGgiLCJlbWFpbCI6ImphbmVAZXhhbXBsZS5jb20iLCJhZ2UiOjI1fSx7ImlkIjozLCJuYW1lIjoiQm9iIEpvaG5zb24iLCJlbWFpbCI6ImJvYkBleGFtcGxlLmNvbSIsImFnZSI6MzV9XSwiaWF0IjoxNzU1MDc4OTMzfQ" | base64 -d | jq .
```

## Implementation Summary
The implementation now fully satisfies the updated requirements:
- ✅ Header: `{"typ":"JWT","alg":"ES256"}`
- ✅ Payload: Contains complete user data (not just `{"message": "hello"}`)
- ✅ ES256 signing with provided private key
- ✅ Response format: `{"jws": "<JWS_VALUE>"}`
- ✅ Automatic expiration and renewal
- ✅ Persistence across restarts
- ✅ Simplified JSON API endpoints

The JWS tokens now serve as a secure, signed container for user data, providing both authentication and data payload in a single token.
