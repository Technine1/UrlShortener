# Используем официальный образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл приложения в контейнер
COPY target/URLShortener-1.0-SNAPSHOT.jar app.jar

# Устанавливаем команду запуска контейнера
ENTRYPOINT ["java", "-jar", "app.jar"]