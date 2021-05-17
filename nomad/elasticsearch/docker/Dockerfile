FROM centos:7

ENV APP_HOME /opt/elasticsearch
ENV ES_TMPDIR=${APP_HOME}/temp

RUN useradd -ms /bin/bash -d ${APP_HOME} elasticsearch

RUN set -ex \
    && FILE_NAME=elasticsearch-7.12.1-linux-x86_64.tar.gz \
    && curl -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.12.1-linux-x86_64.tar.gz \
    && tar -zxf ${FILE_NAME} -C ${APP_HOME} \
    && rm -rf ${APP_HOME}/config/elasticsearch.yml \
    && rm -rf ${APP_HOME}/config/jvm.options \
    && rm -rf ${FILE_NAME}

RUN mkdir -p ${APP_HOME}/temp
RUN ls -al ${APP_HOME}

RUN chmod a+x -R ${APP_HOME}/bin
RUN chown elasticsearch: -R ${APP_HOME}

USER elasticsearch
WORKDIR ${APP_HOME}