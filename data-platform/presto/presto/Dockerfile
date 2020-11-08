FROM openjdk:11-slim

RUN apt-get update && apt-get install -y curl dnsutils netcat python --no-install-recommends \
	&& rm -rf /var/lib/apt/lists/*

# Download and extract the Presto package.
# https://prestosql.io/docs/current/installation/deployment.html
ARG PRESTO_VER
RUN curl https://repo1.maven.org/maven2/io/prestosql/presto-server/$PRESTO_VER/presto-server-$PRESTO_VER.tar.gz \
	| tar xvz -C /opt/  \
	&& ln -s /opt/presto-server-$PRESTO_VER /opt/presto-server \
	&& mkdir -p /opt/presto-server/etc \
	&& mkdir -p /opt/presto-server/etc/catalog

# Copy script to automatically generate the remaining configs at launch time.
COPY autoconfig-and-launch.sh /opt/autoconfig-and-launch.sh
RUN chmod a+x /opt/autoconfig-and-launch.sh
