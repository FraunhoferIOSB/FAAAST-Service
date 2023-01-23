#* ASCII text, with CRLF line terminators
From eclipse-temurin:11-jre

# Copy to images tomcat path
COPY ./target/starter-*-SNAPSHOT.jar /app/starter.jar

RUN addgroup --system --gid 1000 faaast \
    && adduser --system --uid 1000 --gid 1000 --no-create-home faaast \
    # Grant read only permissions on working directory /app
    && chgrp -R 0 /app \
    && chmod -R g=u /app \
    && chmod -R ugo+r /app \    
    # Grant read and write permissions on log directory /logs
    && mkdir /app/logs \
    && chmod -R ugo+rw /app/logs
USER faaast
WORKDIR /app
ENTRYPOINT ["java", "-jar", "starter.jar"]   