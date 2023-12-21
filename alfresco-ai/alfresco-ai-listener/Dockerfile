FROM alfresco/alfresco-base-java:jre17-rockylinux8-202306121108

ARG UID=10001
RUN yum install -y passwd && \
    adduser \
    --comment "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser && \
    passwd --lock appuser
USER appuser

WORKDIR /opt
COPY target/alfresco-ai-listener-*.jar app.jar

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
