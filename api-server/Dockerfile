FROM java:8-jre

ENV APP_HOME /usr/lib/api

RUN echo "Asia/Seoul" > /etc/timezone
RUN dpkg-reconfigure -f noninteractive tzdata

RUN useradd -ms /bin/bash -d ${APP_HOME} api

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ${APP_HOME}/app.jar

RUN chown api: -R ${APP_HOME}

EXPOSE 8082

USER api
WORKDIR ${APP_HOME}
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Xms1024m","-Xmx1024m","-server","-XX:+UseNUMA","-XX:+UseParallelGC","-XX:+AggressiveOpts","-XX:+UseFastAccessorMethods","-jar","app.jar"]
