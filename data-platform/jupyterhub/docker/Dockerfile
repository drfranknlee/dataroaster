FROM jupyterhub/k8s-singleuser-sample:0.9.1

USER 0
RUN apt-get --allow-unauthenticated -o Acquire::Check-Valid-Until=false update
RUN apt-get install -y apt-utils
RUN apt-get install libsasl2-dev -y
RUN pip install sasl
RUN pip install thrift
RUN pip install thrift-sasl
RUN pip install PyHive

USER jovyan
WORKDIR $HOME