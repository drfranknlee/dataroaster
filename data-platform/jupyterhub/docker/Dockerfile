FROM jupyterhub/k8s-singleuser-sample:0.9.1

USER 0
RUN pip install 'pyhive[hive]'

USER jovyan
WORKDIR $HOME