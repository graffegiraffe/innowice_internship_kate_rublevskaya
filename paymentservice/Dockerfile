# Этап сборки (Build stage)
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Копируем родительский pom.xml (предполагается, что build context находится в корне проекта)
COPY pom.xml .

# Копируем pom.xml сервиса платежей
COPY paymentservice/pom.xml paymentservice/

# Копируем исходный код
COPY paymentservice/src paymentservice/src

# Собираем сервис, пропуская тесты (так как для тестов нужна БД, которой нет на этапе сборки)
RUN mvn -f paymentservice/pom.xml clean package -DskipTests

# Этап запуска (Runtime stage)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Создаем пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring

# Копируем собранный jar файл из этапа build
COPY --from=build /app/paymentservice/target/*.jar app.jar

# Назначаем права
RUN chown spring:spring app.jar
USER spring:spring

# Важно: В вашем application.properties указан порт 8085
EXPOSE 8085

# Healthcheck также настроен на порт 8085
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]