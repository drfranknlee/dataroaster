FROM centos:7

ARG ELASTICSEARCH_VERSION 7.12.1
ENV APP_HOME /opt/elasticsearch
ENV ES_TMPDIR=${APP_HOME}/temp
ENV ES_USER elasticsearch

RUN useradd -ms /bin/bash -d ${APP_HOME} ${ES_USER}

RUN set -ex \
    && FILE_NAME=elasticsearch-${ELASTICSEARCH_VERSION}-linux-x86_64.tar.gz \
    && curl -O https://artifacts.elastic.co/downloads/elasticsearch/${FILE_NAME} \
    && tar -zxf ${FILE_NAME} -C ${APP_HOME} \
    && rm -rf ${APP_HOME}/config/elasticsearch.yml \
    && rm -rf ${APP_HOME}/config/jvm.options \
    && rm -rf ${FILE_NAME}

RUN mkdir -p ${APP_HOME}/temp
RUN ls -al ${APP_HOME}

RUN chmod a+x -R ${APP_HOME}/bin
RUN chown ${ES_USER}: -R ${APP_HOME}

RUN set -ex \
    && echo '${ES_USER}   - nofile 65536' >> /etc/security/limits.d/elasticsearch.conf \
    && echo '${ES_USER}   - nproc  65536' >> /etc/security/limits.d/elasticsearch.conf \
    && echo 'root  soft  memlock unlimited' >> /etc/security/limits.d/elasticsearch.conf \
    && echo 'root  hard  memlock unlimited' >> /etc/security/limits.d/elasticsearch.conf

RUN echo vm.max_map_count=262144 >> /etc/sysctl.conf

USER ${ES_USER}
WORKDIR ${APP_HOME}