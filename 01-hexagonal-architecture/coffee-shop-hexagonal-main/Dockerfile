FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

# Perfil de ejecución configurable: SPRING_PROFILES_ACTIVE=dev en desarrollo (por defecto, config base estricta)
ENTRYPOINT ["java","-jar","app.jar"]