# Multistage Dockerfile for FileUploadBot
# Builder stage: use Gradle image to build the installDist distribution
FROM gradle:8.3-jdk17 AS builder
WORKDIR /home/gradle/project
# Copy the entire project (build context is the repo root)
COPY . .
# Assemble only the file-upload-bot distribution to reduce work
RUN gradle :examples:file-upload-bot:installDist --no-daemon --warning-mode=all

# Runtime stage: slim JRE to run the distribution
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the installDist output from builder
COPY --from=builder /home/gradle/project/examples/file-upload-bot/build/install/file-upload-bot/ ./

# Optional: set environment variables default (can be overridden at runtime)
ENV YUCHAT_BOT_TOKEN=""
ENV YUCHAT_BASE_URL=""

# Ensure executable bit on start script
RUN chmod +x /app/bin/file-upload-bot

ENTRYPOINT ["/app/bin/file-upload-bot"]
