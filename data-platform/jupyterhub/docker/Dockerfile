FROM jupyterhub/k8s-singleuser-sample:0.9.1

USER 0
RUN apt-get install libsasl2-dev
RUN pip install sasl
RUN pip install thrift
RUN pip install thrift-sasl
RUN pip install PyHive

USER jovyan
WORKDIR $HOME