From eclipse-temurin:11-jre

COPY ./target/starter-*-SNAPSHOT.jar /app/starter.jar

RUN addgroup --system --gid 1000 faaast \
    && adduser --system --uid 1000 --gid 1000 --no-create-home faaast \
    # restrict permissions on working directory /app
    && chgrp -R 0 /app \
    && chmod -R g=u /app \
    # Create directories to which FA³ST needs permissions
    && mkdir /app/resources /app/logs /app/PKI /app/USERS_PKI \
    # Grant read and write permissions on created directories
    && chmod -R ugo+rw /app/resources /app/PKI /app/USERS_PKI /app/logs

USER faaast
WORKDIR /app
ENTRYPOINT ["java", "-jar", "starter.jar"]