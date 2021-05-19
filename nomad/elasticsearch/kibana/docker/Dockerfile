FROM centos:7

ENV APP_HOME /opt/kibana
ENV KIBANA_USER kibana

RUN useradd -ms /bin/bash -d ${APP_HOME} ${KIBANA_USER}

RUN yum install nmap-ncat -y

RUN set -ex \
    && PACK_NAME=kibana-7.12.1 \
    && FILE_NAME=${PACK_NAME}-linux-x86_64.tar.gz \
    && curl -O https://artifacts.elastic.co/downloads/kibana/${FILE_NAME} \
    && tar -zxf ${FILE_NAME} \
    && cp -R ${PACK_NAME}/* ${APP_HOME} \
    && rm -rf ${APP_HOME}/config/kibana.yml \
    && rm -rf ${FILE_NAME} \
    && rm -rf ${PACK_NAME}

RUN mkdir -p ${APP_HOME}/temp
RUN ls -al ${APP_HOME}

RUN chmod a+x -R ${APP_HOME}/bin
RUN chown ${KIBANA_USER}: -R ${APP_HOME}

USER ${KIBANA_USER}
WORKDIR ${APP_HOME}
EXPOSE 5601