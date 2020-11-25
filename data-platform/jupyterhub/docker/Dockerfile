FROM jupyterhub/k8s-singleuser-sample:0.9.1

USER 0
RUN apt-get --allow-unauthenticated -o Acquire::Check-Valid-Until=false update && \
    apt-get install --reinstall build-essential -y && \
    apt-get install -y apt-utils && \
    apt-get -y install gcc && \
    apt-get install libsasl2-2 libsasl2-dev -y && \
    apt-get install sasl2-bin -y

RUN /opt/conda/bin/pip install 'pyhive[hive]' && \
    /opt/conda/bin/pip install 'pyhive[presto]'

USER jovyan
WORKDIR $HOME