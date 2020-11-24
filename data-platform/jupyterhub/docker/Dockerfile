FROM jupyterhub/k8s-singleuser-sample:0.9.1

USER 0
RUN apt-get --allow-unauthenticated -o Acquire::Check-Valid-Until=false update && \
    apt-get install --reinstall build-essential -y && \
    apt-get install -y apt-utils && \
    apt-get -y install gcc && \
    apt-get install sasl2-bin -y 

RUN pip install sasl && \
    pip install thrift && \
    pip install thrift-sasl && \
    pip install 'pyhive[hive]' && \
    pip install 'pyhive[presto]' && \
    pip install boto3 paramiko nltk scipy numpy scikit-learn pandas

USER jovyan
WORKDIR $HOME