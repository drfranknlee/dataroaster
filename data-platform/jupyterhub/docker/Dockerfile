FROM jupyterhub/k8s-singleuser-sample:0.9.1

USER 0
RUN apt-get --allow-unauthenticated -o Acquire::Check-Valid-Until=false update && \
    apt-get install --reinstall build-essential -y && \
    apt-get install -y apt-utils && \
    apt-get -y install gcc && \
    apt-get install libsasl2-dev -y

RUN pip install sasl && \
    pip install thrift && \
    pip install thrift-sasl && \
    pip install PyHive

USER jovyan
WORKDIR $HOME