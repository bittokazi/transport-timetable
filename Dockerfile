FROM eclipse-temurin:21-jammy
EXPOSE 8081

ARG RELEASE_TAG=""
ARG CONFIG_ENV=""

RUN mkdir /app

COPY --chown=gradle:gradle ./backend-${RELEASE_TAG}.jar /app/app.jar
COPY --chown=gradle:gradle ./info.json /app/info.json

ENTRYPOINT ["java","-jar","/app/app.jar", "-config=/app/application.yaml"]
