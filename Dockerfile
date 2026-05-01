# 1. JDK 이미지를 기반으로 실행 환경 설정
FROM eclipse-temurin:17-jdk-alpine

# 2. JAR 파일을 컨테이너 안으로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 3. 컨테이너 실행 시 실행할 명령어
ENTRYPOINT ["java","-jar","/app.jar"]
