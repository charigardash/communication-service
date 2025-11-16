#!/bin/sh

# Read Docker secrets and export them as environment variables
export EMAIL=$(cat /run/secrets/EMAIL)
export APP_PASSWORD=$(cat /run/secrets/APP_PASSWORD)
export TWILIO_PHONE_NUMBER=$(cat /run/secrets/TWILIO_PHONE_NUMBER)
export TWILIO_AUTH_TOKEN=$(cat /run/secrets/TWILIO_AUTH_TOKEN)
export TWILIO_ACCOUNT_SID=$(cat /run/secrets/TWILIO_ACCOUNT_SID)

# Start the Spring Boot application
exec java -jar communication-service.jar

# Spring Boot doesn’t automatically read secrets from files,
# so you need to read the file contents manually in your config class or use a script to inject
# them as environment variables.