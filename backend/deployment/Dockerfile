FROM openjdk:11-jdk
MAINTAINER UMB AG

COPY demo/build/libs/*.jar ${INSTALL_DIR}/app.jar

ENV JAVA_OPTS="-Xms256m -Xmx1024m -XX:+StartAttachListener"
# ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

USER 1001

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ${INSTALL_DIR}/app.jar" ]
