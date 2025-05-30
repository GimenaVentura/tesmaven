
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia los archivos necesarios
COPY pom.xml ./
COPY src ./src

# Empaqueta la app sin ejecutar tests (puedes quitar -DskipTests si deseas)
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final de producción con JDK liviano
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copia el jar desde el builder
COPY --from=builder /app/target/*.jar app.jar

# Expón el puerto (Render usará la variable PORT automáticamente)
EXPOSE 8081

# Ejecuta la app
ENTRYPOINT ["java", "-jar", "app.jar"]
