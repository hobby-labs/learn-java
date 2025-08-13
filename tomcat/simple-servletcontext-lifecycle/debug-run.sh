#!/bin/bash

# Set Maven options for debugging
export MAVEN_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# Run Maven with cargo
./mvnw clean package cargo:run -DskipTests

echo ""
echo "🔚 Debug session ended."
