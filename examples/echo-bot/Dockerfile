# Multistage Dockerfile for EchoBot
# Builder stage: use Gradle image to build the installDist distribution
FROM gradle:8.3-jdk17 AS builder
WORKDIR /home/gradle/project
# Copy the entire project (build context is the repo root)
COPY . .
# Assemble only the echo-bot distribution to reduce work
RUN gradle :examples:echo-bot:installDist --no-daemon --warning-mode=all

# Runtime stage: slim JRE to run the distribution
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the installDist output from builder
COPY --from=builder /home/gradle/project/examples/echo-bot/build/install/echo-bot/ ./

# Optional: set environment variables default (can be overridden at runtime)
ENV YUCHAT_BOT_TOKEN=""
ENV YUCHAT_BASE_URL=""

# Ensure executable bit on start script
RUN chmod +x /app/bin/echo-bot

ENTRYPOINT ["/app/bin/echo-bot"]
