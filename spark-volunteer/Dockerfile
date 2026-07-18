# =============================================================================
# Dockerfile — 星火后端服务（Spring Boot 多阶段构建）
# =============================================================================

# -------------------------------------------------------------------------
# 阶段1: Maven 编译
# -------------------------------------------------------------------------
FROM maven:3.8-openjdk-8 AS build
WORKDIR /app

# 先复制 pom.xml 并下载依赖（利用 Docker 层缓存加速后续构建）
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# 复制源码并编译打包
COPY src ./src
RUN mvn package -DskipTests -B -q

# -------------------------------------------------------------------------
# 阶段2: 运行镜像（极小体积 JRE）
# -------------------------------------------------------------------------
FROM openjdk:8-jre-alpine

# 时区设置 + curl（用于健康检查）
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

WORKDIR /app
RUN mkdir -p logs && \
    addgroup -S spark && adduser -S spark -G spark && \
    chown -R spark:spark /app

# 切换到非 root 用户运行
USER spark

COPY --from=build /app/target/volunteer-1.0.0.jar app.jar

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=90s \
    CMD curl -sf http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-Xms256m", "-Xmx512m", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/app/logs/", \
    "-Duser.timezone=Asia/Shanghai", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
