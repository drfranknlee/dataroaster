FROM java:8-jre

ENV APP_HOME /usr/lib/authorizer

RUN echo "Asia/Seoul" > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata

RUN useradd -ms /bin/bash -d ${APP_HOME} authorizer

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ${APP_HOME}/app.jar

RUN chown authorizer: -R ${APP_HOME}

EXPOSE 8081

USER authorizer
WORKDIR ${APP_HOME}
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Xms1024m","-Xmx1024m","-server","-XX:+UseNUMA","-XX:+UseParallelGC","-XX:+AggressiveOpts","-XX:+UseFastAccessorMethods","-jar","app.jar"]
