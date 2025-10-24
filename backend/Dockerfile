# Usa uma imagem leve com JDK 17
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho no contêiner
WORKDIR /app

# Copia o JAR para dentro do contêiner (ajuste se o caminho mudar)
COPY target/cardpg-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta da aplicação
EXPOSE 8081

# Executa a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
