FROM jupyterhub/k8s-singleuser-sample:1.1.3

USER 0
RUN apt-get --allow-unauthenticated -o Acquire::Check-Valid-Until=false update && \
    apt-get install --reinstall build-essential -y && \
    apt-get install -y apt-utils && \
    apt-get -y install gcc && \
    apt-get install libsasl2-2 libsasl2-dev libsasl2-modules -y

RUN /opt/conda/bin/pip install 'pyhive[hive]' && \
    /opt/conda/bin/pip install 'pyhive[trino]'

USER jovyan
WORKDIR $HOME