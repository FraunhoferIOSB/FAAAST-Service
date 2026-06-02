FROM eclipse-temurin:17-jre

ARG ARTIFACT_FILE
COPY ./target/${ARTIFACT_FILE} /app/starter.jar

RUN groupadd --system --gid 1001 faaast \
    && useradd --system --uid 1001 --gid 1001 --no-create-home faaast \
    && chgrp -R 0 /app \
    && chmod -R g=u /app \
    && mkdir /app/resources /app/logs /app/PKI /app/USERS_PKI \
    && chown -R faaast:faaast /app

USER faaast
WORKDIR /app
ENTRYPOINT ["java", "-jar", "starter.jar"]
