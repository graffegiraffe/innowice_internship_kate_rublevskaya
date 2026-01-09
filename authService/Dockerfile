FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./

COPY authService ./authService

WORKDIR /build/authService
RUN mvn clean package -DskipTests || \
    (sleep 5 && mvn clean package -DskipTests) || \
    (sleep 10 && mvn clean package -DskipTests)

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /build/authService/target/*.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker} -jar app.jar"]