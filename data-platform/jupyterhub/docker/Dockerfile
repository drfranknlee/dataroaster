FROM jupyterhub/k8s-singleuser-sample:0.9.1

USER 0
RUN apt-get upgrade
RUN echo "deb [check-valid-until=no] http://cdn-fastly.deb.debian.org/debian jessie main" > /etc/apt/sources.list.d/jessie.list
RUN echo "deb [check-valid-until=no] http://archive.debian.org/debian jessie-backports main" > /etc/apt/sources.list.d/jessie-backports.list
RUN sed -i '/deb http:\/\/deb.debian.org\/debian jessie-updates main/d' /etc/apt/sources.list
RUN apt-get -o Acquire::Check-Valid-Until=false update
RUN apt-get install libsasl2-dev
RUN pip install sasl
RUN pip install thrift
RUN pip install thrift-sasl
RUN pip install PyHive

USER jovyan
WORKDIR $HOME