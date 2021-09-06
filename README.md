# DataRoaster
DataRoaster is a tool to create data platforms running on Kubernetes. Data platform components like Hive metastore, Spark Thrift Server(Hive on Spark), Trino, Redash, JupyterHub etc. will be deployed on Kubernetes using DataRoaster easily.

## DataRoaster Architecture

![DataRoaster Architecture](http://www.cloudchef-labs.com/images/architecture.png)

DataRoaster consists of the following components.
* CLI: command line interface to API Server.
* API Server: handles requests from clients like CLI.
* Authorizer: runs as OAuth2 Server.
* Secret Manager: manages secrets like kubeconfig using Vault.
* Resource Controller: manages remote kubernetes resources with kubectl, helm and kubernetes client like fabric8 k8s client.



## Services provided by DataRoaster

### Data Catalog
* Hive Metastore: Standard Data Catalog in Data Lake

### Query Engine
* Spark Thrift Server: used as Hive Server, namely Hive on Spark. Interface to query data in Data Lake
* Trino: Fast Interactive Query Engine to query data in Data Lake

### Streaming
* Kafka: Popular Streaming Platform

### Analytics
* JupyterHub: Controller to serve Jupyter Notebook which is most popular web based interactive analytics tool for multiple users
* Redash: Visual Data Analytics SQL Engine which provides a lot of data sources connectors

### Workflow
* Argo Workflow: Workflow engine running on Kubernetes, with which containerized long running batch jobs, ETL Jobs, ML Jobs, etc can be scheduled to run on Kubernetes

### CI / CD
* Jenkins: Popular continuous Integration Server
* Argo CD: Continuous Delivery tool for Kubernetes

### Metrics Monitoring
* Prometheus: Popular monitoring tool
* Grafana: Popular metrics visibility tool

### Pod Log Monitoring
* ELK: Elasticsearch, Logstash and Kibana
* Filebeat: used to fetch log files

### Distributed Tracing
* Jaeger: Popular microservices distributed tracing platform

### Backup
* Velero: used to backup Kubernetes Resources and Persistent Volumes

### Private Registry
* Harbor: used as private registry to manage docker images and helm charts




## DataRoaster Demo
This demo shows how to create the components like hive metastore, spark thrift server, trino, redash and jupyterhub deployed on Kubernetes with ease using DataRoaster.

[![DataRoaster Demo](http://www.cloudchef-labs.com/images/demo-thumbnail.jpg)](https://youtu.be/AeqkkQDwPqY "DataRoaster Demo")






## Install DataRoaster with ansible

TODO: coming soon.


## Install DataRoaster with source

### Prerequisites
Before installing DataRoaster, the following should be installed
* JDK 8
* Maven
* kubectl
* Helm


### Clone source
```
cd ~;
git clone https://github.com/cloudcheflabs/dataroaster.git;
```

### Install MySQL
MySQL is used as DB of API Server and Authorizer and as backend storage of Vault in Secret Manager.

```
sudo wget http://repo.mysql.com/mysql-community-release-el7-5.noarch.rpm;
sudo rpm -ivh mysql-community-release-el7-5.noarch.rpm;
 
sudo yum install mysql-server -y;
 
sudo systemctl start mysqld;
sudo systemctl enable mysqld;
sudo systemctl status mysqld;
 
sudo yum install mysql-connector-java -y;
```

### Install Vault in Secret Manager

#### Create database and user for Vault

```
CREATE DATABASE vault;
CREATE USER 'mykidong'@'localhost' IDENTIFIED BY 'icarus0337';
GRANT ALL PRIVILEGES ON *.* TO 'mykidong'@'localhost' WITH GRANT OPTION;
flush privileges;
```

#### Install Vault Binary
```
sudo yum install -y yum-utils;
sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/RHEL/hashicorp.repo;

# listup vault versions.
yum --showduplicate list vault;

# install vault.
sudo yum -y install vault-1.6.3-1.x86_64;

# verify vault.
vault;
```

#### Create Certificates
To access Vault via TLS, Certificates are required. To create certs, you may use public CA like Let's Encrypt.
For our example, we will create private certs as follows.

Create `create-cert.sh`: 
```
#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CA_DIR=work/ca
KEYSTORE_FILE=work/keystore.jks
CLIENT_CERT_KEYSTORE=work/client-cert.jks

if [[ -d work/ca ]] ; then
    rm -Rf ${CA_DIR}
fi

if [[ -f ${KEYSTORE_FILE} ]] ; then
    rm -Rf ${KEYSTORE_FILE}
fi

if [[ -f ${CLIENT_CERT_KEYSTORE} ]] ; then
    rm -Rf ${CLIENT_CERT_KEYSTORE}
fi

if [ ! -x "$(which openssl)" ] ; then
   echo "[ERROR] No openssl in PATH"
   exit 1
fi

KEYTOOL=keytool

if [  ! -x "${KEYTOOL}" ] ; then
   KEYTOOL=${JAVA_HOME}/bin/keytool
fi

if [  ! -x "${KEYTOOL}" ] ; then
   echo "[ERROR] No keytool in PATH/JAVA_HOME"
   exit 1
fi

mkdir -p ${CA_DIR}/private ${CA_DIR}/certs ${CA_DIR}/crl ${CA_DIR}/csr ${CA_DIR}/newcerts ${CA_DIR}/intermediate

echo "[INFO] Generating CA private key"
# Less bits = less secure = faster to generate
openssl genrsa -passout pass:changeit -aes256 -out ${CA_DIR}/private/ca.key.pem 2048

chmod 400 ${CA_DIR}/private/ca.key.pem

echo "[INFO] Generating CA certificate"
openssl req -config ${DIR}/openssl.cnf \
      -key ${CA_DIR}/private/ca.key.pem \
      -new -x509 -days 7300 -sha256 -extensions v3_ca \
      -out ${CA_DIR}/certs/ca.cert.pem \
      -passin pass:changeit \
      -subj "/C=NN/ST=Unknown/L=Unknown/O=spring-cloud-vault-config/CN=CA Certificate"

echo "[INFO] Prepare CA database"
echo 1000 > ${CA_DIR}/serial
touch ${CA_DIR}/index.txt

echo "[INFO] Generating server private key"
openssl genrsa -aes256 \
      -passout pass:changeit \
      -out ${CA_DIR}/private/localhost.key.pem 2048

openssl rsa -in ${CA_DIR}/private/localhost.key.pem \
      -out ${CA_DIR}/private/localhost.decrypted.key.pem \
      -passin pass:changeit

openssl rsa -in ${CA_DIR}/private/localhost.key.pem \
      -pubout -out ${CA_DIR}/private/localhost.public.key.pem \
      -passin pass:changeit

chmod 400 ${CA_DIR}/private/localhost.key.pem
chmod 400 ${CA_DIR}/private/localhost.decrypted.key.pem

echo "[INFO] Generating server certificate request"
openssl req -config <(cat ${DIR}/openssl.cnf \
        <(printf "\n[SAN]\nsubjectAltName=DNS:localhost,IP:127.0.0.1")) \
      -reqexts SAN \
      -key ${CA_DIR}/private/localhost.key.pem \
      -passin pass:changeit \
      -new -sha256 -out ${CA_DIR}/csr/localhost.csr.pem \
      -subj "/C=NN/ST=Unknown/L=Unknown/O=spring-cloud-vault-config/CN=localhost"

echo "[INFO] Signing certificate request"
openssl ca -config ${DIR}/openssl.cnf \
      -extensions server_cert -days 7300 -notext -md sha256 \
      -passin pass:changeit \
      -batch \
      -in ${CA_DIR}/csr/localhost.csr.pem \
      -out ${CA_DIR}/certs/localhost.cert.pem

echo "[INFO] Generating client auth private key"
openssl genrsa -aes256 \
      -passout pass:changeit \
      -out ${CA_DIR}/private/client.key.pem 2048

openssl rsa -in ${CA_DIR}/private/client.key.pem \
      -out ${CA_DIR}/private/client.decrypted.key.pem \
      -passin pass:changeit

chmod 400 ${CA_DIR}/private/client.key.pem

echo "[INFO] Generating client certificate request"
openssl req -config ${DIR}/openssl.cnf \
      -key ${CA_DIR}/private/client.key.pem \
      -passin pass:changeit \
      -new -sha256 -out ${CA_DIR}/csr/client.csr.pem \
      -subj "/C=NN/ST=Unknown/L=Unknown/O=spring-cloud-vault-config/CN=client"

echo "[INFO] Signing certificate request"
openssl ca -config ${DIR}/openssl.cnf \
      -extensions usr_cert -days 7300 -notext -md sha256 \
      -passin pass:changeit \
      -batch \
      -in ${CA_DIR}/csr/client.csr.pem \
      -out ${CA_DIR}/certs/client.cert.pem

echo "[INFO] Creating  PKCS12 file with client certificate"
openssl pkcs12 -export -clcerts \
      -in ${CA_DIR}/certs/client.cert.pem \
      -inkey ${CA_DIR}/private/client.decrypted.key.pem \
      -passout pass:changeit \
      -out ${CA_DIR}/client.p12

${KEYTOOL} -importcert -keystore ${KEYSTORE_FILE} -file ${CA_DIR}/certs/ca.cert.pem -noprompt -storepass changeit
${KEYTOOL} -importkeystore \
                              -srckeystore ${CA_DIR}/client.p12 -srcstoretype PKCS12 -srcstorepass changeit\
                              -destkeystore ${CLIENT_CERT_KEYSTORE} -deststoretype JKS \
                              -noprompt -storepass changeit

echo "[INFO] Generating intermediate CA private key"
# Less bits = less secure = faster to generate
openssl genrsa -passout pass:changeit -aes256 -out ${CA_DIR}/private/intermediate.key.pem 2048

openssl rsa -in ${CA_DIR}/private/intermediate.key.pem \
      -out ${CA_DIR}/private/intermediate.decrypted.key.pem \
      -passin pass:changeit

chmod 400 ${CA_DIR}/private/intermediate.key.pem
chmod 400 ${CA_DIR}/private/intermediate.decrypted.key.pem

echo "[INFO] Generating intermediate certificate"
openssl req -config ${DIR}/intermediate.cnf \
      -key ${CA_DIR}/private/intermediate.key.pem \
      -new -sha256 \
      -out ${CA_DIR}/csr/intermediate.csr.pem \
      -passin pass:changeit \
      -subj "/C=NN/ST=Unknown/L=Unknown/O=spring-cloud-vault-config/CN=Intermediate CA Certificate"

echo "[INFO] Signing intermediate certificate request"
openssl ca -config ${DIR}/openssl.cnf \
      -days 7300 -notext -md sha256 -extensions v3_intermediate_ca \
      -passin pass:changeit \
      -batch \
      -in ${CA_DIR}/csr/intermediate.csr.pem \
      -out ${CA_DIR}/certs/intermediate.cert.pem
```



And create `intermediate.cnf`:
```
[ ca ]
# `man ca`
default_ca = CA_default

[ CA_default ]
# Directory and file locations.
dir               = work/ca
certs             = $dir/certs
crl_dir           = $dir/crl
new_certs_dir     = $dir/newcerts
database          = $dir/index.txt
serial            = $dir/serial
RANDFILE          = $dir/private/.rand

# The intermediate key and root certificate.
private_key       = $dir/private/intermediate.key.pem
certificate       = $dir/certs/intermediate.cert.pem

# For certificate revocation lists.
crlnumber         = $dir/crlnumber
crl               = $dir/crl/intermediate.crl.pem
crl_extensions    = crl_ext
default_crl_days  = 30

# SHA-1 is deprecated, so use SHA-2 instead.
default_md        = sha256

name_opt          = ca_default
cert_opt          = ca_default
default_days      = 375
preserve          = no
policy            = policy_loose

[ policy_loose ]
countryName             = optional
stateOrProvinceName     = optional
localityName            = optional
organizationName        = optional
organizationalUnitName  = optional
commonName              = supplied
emailAddress            = optional

[ req ]
# Options for the `req` tool (`man req`).
default_bits        = 2048
distinguished_name  = req_distinguished_name
string_mask         = utf8only

# SHA-1 is deprecated, so use SHA-2 instead.
default_md          = sha256

[ req_distinguished_name ]
# See <https://en.wikipedia.org/wiki/Certificate_signing_request>.
countryName                     = Country Name (2 letter code)
stateOrProvinceName             = State or Province Name
localityName                    = Locality Name
0.organizationName              = Organization Name
organizationalUnitName          = Organizational Unit Name
commonName                      = Common Name
emailAddress                    = Email Address

# Optionally, specify some defaults.
countryName_default             = NN
stateOrProvinceName_default     = Vault Test
localityName_default            =
0.organizationName_default      = spring-cloud-vault-config
#organizationalUnitName_default =
#emailAddress_default           = info@spring-cloud-vault-config.dummy
```



and create `openssl.cnf`:
```
[ ca ]
# `man ca`
default_ca = CA_default

[ CA_default ]
# Directory and file locations.
dir               = work/ca
certs             = $dir/certs
crl_dir           = $dir/crl
new_certs_dir     = $dir/newcerts
database          = $dir/index.txt
serial            = $dir/serial
RANDFILE          = $dir/private/.rand

# The root key and root certificate.
private_key       = $dir/private/ca.key.pem
certificate       = $dir/certs/ca.cert.pem

# For certificate revocation lists.
crlnumber         = $dir/crlnumber
crl               = $dir/crl/ca.crl.pem
crl_extensions    = crl_ext
default_crl_days  = 30

# SHA-1 is deprecated, so use SHA-2 instead.
default_md        = sha256

name_opt          = ca_default
cert_opt          = ca_default
default_days      = 375
preserve          = no
policy            = policy_strict
copy_extensions   = copy

[ policy_strict ]
# The root CA should only sign intermediate certificates that match.
# See the POLICY FORMAT section of `man ca`.
countryName             = match
stateOrProvinceName     = match
organizationName        = match
organizationalUnitName  = optional
commonName              = supplied
emailAddress            = optional

[ req ]
# Options for the `req` tool (`man req`).
default_bits        = 2048
distinguished_name  = req_distinguished_name
string_mask         = utf8only

# SHA-1 is deprecated, so use SHA-2 instead.
default_md          = sha256

# Extension to add when the -x509 option is used.
x509_extensions     = v3_ca

[ req_distinguished_name ]
# See <https://en.wikipedia.org/wiki/Certificate_signing_request>.
countryName                     = Country Name (2 letter code)
stateOrProvinceName             = State or Province Name
localityName                    = Locality Name
0.organizationName              = Organization Name
organizationalUnitName          = Organizational Unit Name
commonName                      = Common Name
emailAddress                    = Email Address

# Optionally, specify some defaults.
countryName_default             = NN
stateOrProvinceName_default     = Vault Test
localityName_default            =
0.organizationName_default      = spring-cloud-vault-config
#organizationalUnitName_default =
#emailAddress_default           = info@spring-cloud-vault-config.dummy

[ v3_ca ]
# Extensions for a typical CA (`man x509v3_config`).
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
basicConstraints = critical, CA:true
keyUsage = critical, digitalSignature, cRLSign, keyCertSign

[ v3_intermediate_ca ]
# Extensions for a typical intermediate CA (`man x509v3_config`).
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
basicConstraints = critical, CA:true, pathlen:0
keyUsage = critical, digitalSignature, cRLSign, keyCertSign

[ usr_cert ]
# Extensions for client certificates (`man x509v3_config`).
basicConstraints = CA:FALSE
nsCertType = client, email
nsComment = "OpenSSL Generated Client Certificate"
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer
keyUsage = critical, nonRepudiation, digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth, emailProtection

[ server_cert ]
# Extensions for server certificates (`man x509v3_config`).
basicConstraints = CA:FALSE
nsCertType = server
nsComment = "OpenSSL Generated Server Certificate"
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer:always
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
```


Finally, run `create-cert.sh` to create certs:
```
chmod a+x create-cert.sh;
./create-cert.sh;
```

#### Copy Certs to Vault directory
```
sudo cp work/ca/certs/localhost.cert.pem /etc/vault.d/localhost.cert.pem;
sudo cp work/ca/private/localhost.decrypted.key.pem /etc/vault.d/localhost.decrypted.key.pem;
sudo cp work/keystore.jks /etc/vault.d/keystore.jks;
sudo chown vault:vault -R /etc/vault.d;
sudo chmod 777 -R /etc/vault.d/
```

#### Configure Vault
```
# vault configuration.
sudo su -;
cat <<EOF > /etc/vault.d/vault.hcl
storage "mysql" {
  address  = "127.0.0.1:3306"
  database = "vault"
  table    = "vault_data"
  username = "mykidong"
  password = "icarus0337"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  tls_cert_file = "/etc/vault.d/localhost.cert.pem"
  tls_key_file = "/etc/vault.d/localhost.decrypted.key.pem"
}

disable_mlock = true

EOF

exit;
```

#### Start Vault

```

## create service.

sudo su -;

cat > /etc/systemd/system/vault.service <<EOF
[Unit]
Description="HashiCorp Vault - A tool for managing secrets"
Documentation=https://www.vaultproject.io/docs/
Requires=network-online.target
After=network-online.target
ConditionFileNotEmpty=/etc/vault.d/vault.hcl
StartLimitIntervalSec=60
StartLimitBurst=3

[Service]
User=root
Group=root
ProtectSystem=full
ProtectHome=read-only
PrivateTmp=yes
PrivateDevices=yes
SecureBits=keep-caps
AmbientCapabilities=CAP_IPC_LOCK
Capabilities=CAP_IPC_LOCK+ep
CapabilityBoundingSet=CAP_SYSLOG CAP_IPC_LOCK
NoNewPrivileges=yes
ExecStart=/usr/bin/vault server -config=/etc/vault.d/vault.hcl 
ExecReload=/bin/kill --signal HUP \$MAINPID
KillMode=process
KillSignal=SIGINT
Restart=on-failure
RestartSec=5
TimeoutStopSec=30
LimitNOFILE=65536
LimitMEMLOCK=infinity

[Install]
WantedBy=multi-user.target


[Install]
WantedBy=multi-user.target

EOF

exit;


## start vault.
sudo systemctl daemon-reload;
sudo systemctl enable vault;
sudo systemctl restart vault;
```

#### Initialize Vault
If Vault is initialized, it looks like below.
```
# initialize vault.
export VAULT_ADDR="https://localhost:8200";
export VAULT_SKIP_VERIFY=true;

vault operator init;
Unseal Key 1: Glv/PkTj3zKjcbg462dNBkIB6ffvExv+y342jalzPUXO
Unseal Key 2: P6o0+OFhIlLXizXKMjltnj5U4zViJyzSRbeUWJUbTA2p
Unseal Key 3: XkqpiyuLVTOnrWPHwhZkd6b92esS2W5g+mKnDRYeh14L
Unseal Key 4: e7tiTY4JqFPTV+41G0hE79qo0ymf7VlMdKsF2Cp29hZs
Unseal Key 5: qtSdM2mE4g8IlFKCSHwlyvAJ4HA1VnWM6rNnh5683Zpz

Initial Root Token: s.ZuCVMzRIM4eYjMZuvDXqOBJg

Vault initialized with 5 key shares and a key threshold of 3. Please securely
distribute the key shares printed above. When the Vault is re-sealed,
restarted, or stopped, you must supply at least 3 of these keys to unseal it
before it can start servicing requests.

Vault does not store the generated master key. Without at least 3 key to
reconstruct the master key, Vault will remain permanently sealed!

It is possible to generate new unseal keys, provided you have a quorum of
existing unseal keys shares. See "vault operator rekey" for more information.
```

#### Unseal Vault
Unseal Vault with your keys, for instance.
```
vault operator unseal Glv/PkTj3zKjcbg462dNBkIB6ffvExv+y342jalzPUXO
vault operator unseal P6o0+OFhIlLXizXKMjltnj5U4zViJyzSRbeUWJUbTA2p
vault operator unseal XkqpiyuLVTOnrWPHwhZkd6b92esS2W5g+mKnDRYeh14L
```

#### Test Vault
Test Vault, for instance.
```
export VAULT_ADDR="https://localhost:8200";
export VAULT_TOKEN=s.ZuCVMzRIM4eYjMZuvDXqOBJg

# Enable the kv secrets engine at: secret/
vault secrets enable -path=secret/ kv

# put kv with path 'secret/fakebank'
vault kv put secret/fakebank api_key=abc1234 api_secret=1a2b3c4d

# get kv with path 'secret/fakebank'
vault kv get secret/fakebank;
======= Data =======
Key           Value
---           -----
api_key       abc1234
api_secret    1a2b3c4d
```


### Install Authorizer

#### Create database, user and tables

```
mysql -u root -p;
...
CREATE DATABASE dataroaster; 
CREATE USER 'dataroaster'@'localhost' IDENTIFIED BY 'icarus0337';
GRANT ALL PRIVILEGES ON *.* TO 'dataroaster'@'localhost' WITH GRANT OPTION;
flush privileges;

use dataroaster;
source <dataroaster-src>/authorizer/sql/creat-auth.sql;
...
```

#### Build and run Authorizer
```
export AUTHORIZER_HOME=~/authorizer;
mkdir -p $AUTHORIZER_HOME;

# create log directory.
sudo mkdir -p /data/dataroaster/logs;
sudo chown $(whoami): -R /data/dataroaster/logs;

export DATAROASTER_SRC=~/dataroaster;

# build all.
cd $DATAROASTER_SRC;
mvn -e -DskipTests=true clean install;

# build authorizer.
cd  $DATAROASTER_SRC/authorizer;
mvn -e clean install -P prod;
cp target/*.jar $AUTHORIZER_HOME;

cd $AUTHORIZER_HOME;
nohup java -jar ./authorizer-*.jar >/dev/null &
```

### Install API Server

#### Create database tables
```
mysql -u root -p;
...
use dataroaster;
source <dataroaster-src>/api-server/sql/create-tables.sql;
...
```

#### Build and run API Server
```
export APISERVER_HOME=~/apiserver;
mkdir -p $APISERVER_HOME;

export DATAROASTER_SRC=~/dataroaster;

# build all.
cd $DATAROASTER_SRC;
mvn -e -DskipTests=true clean install;

# build api server.
cd $DATAROASTER_SRC/api-server;
mvn -e clean install -P prod;
cp target/*.jar $APISERVER_HOME;
cd $APISERVER_HOME;

# copy all the necessary confs.
mkdir -p conf;
cp $DATAROASTER_SRC/api-server/src/main/resources/application.properties conf;
cp $DATAROASTER_SRC/api-server/src/main/resources/application-prod.yml conf;

# copy all the template.
mkdir -p templates;
cp -R $DATAROASTER_SRC/api-server/src/main/resources/templates/* templates/;
```

Replace the value of `vault.token` in `$APISERVER_HOME/conf/application-prod.yml` with the initial root token generated when vault was initialized.


Run API Server:
```
nohup java \
-cp api-server-*.jar \
-Dloader.path=$APISERVER_HOME/ \
-Dspring.config.location=file://$APISERVER_HOME/conf/application.properties \
-Dspring.config.location=file://$APISERVER_HOME/conf/application-prod.yml \
-Dspring.profiles.active=prod \
org.springframework.boot.loader.PropertiesLauncher >/dev/null &
```

### Install CLI
```
export DATAROASTER_SRC=~/dataroaster;

# build all.
cd $DATAROASTER_SRC;
mvn -e -DskipTests=true clean install;

# move to cli source.
cd $DATAROASTER_SRC/cli;

# package executable jar.
mvn -e -DskipTests=true clean install shade:shade;

# install dataroaster.
cp target/cli-*-executable.jar ~/bin/dataroaster

# check.
dataroaster;
```


## Getting started

### Step 1: Login to API Server
```
dataroaster login http://localhost:8082;
...

# user / password: mykidong / icarus0337
```

### Step 2: Create Kubernetes Cluster
```
dataroaster cluster create --name my-cluster --description my-cluster-desc;
```

### Step 3: Register kubeconfig file for your kubernetes cluster
```
dataroaster kubeconfig create --kubeconfig ~/.kube/config
```

### Step 4: Create Project where services will be created
```
dataroaster project create  --name my-project --description my-project-desc;
```

### Step 5: Create Services like Data Catalog, Query Engine, etc in your Project
See DataRoaster CLI usage how to create services.



## DataRoaster CLI Usage

TODO: add description of command parameters.

### Login

```
dataroaster login http://localhost:8082;
...

# user / password: mykidong / icarus0337
```


### Cluster
```
# create
dataroaster cluster create --name dataroaster-cluster --description dataroaster-desc...;

# update
dataroaster cluster update;

# delete
dataroaster cluster delete;
```

### Kubeconfig
```
# create.
dataroaster kubeconfig create --kubeconfig ~/.kube/config

# update.
dataroaster kubeconfig update --kubeconfig ~/.kube/config
```

### Project
```
# create.
dataroaster project create  --name new-test-project --description new-test-desc;

# update.
dataroaster project update;

# delete.
dataroaster project delete;
```

### Pod Log Monitoring
```
# create.
dataroaster podlogmonitoring create \
--elasticsearch-hosts 192.168.10.10:9200,192.168.10.134:9200,192.168.10.145:9200;

# delete
dataroaster podlogmonitoring delete;
```

### Metrics Monitoring
```
# create.
dataroaster metricsmonitoring create;

# delete.
dataroaster metricsmonitoring delete;
```

### Distributed Tracing
```
# create.
dataroaster distributedtracing create \
--ingress-host ingress-nginx-jaeger-test.cloudchef-labs.com \
--elasticsearch-host-port 192.168.10.10:9200;

# delete
dataroaster distributedtracing delete;
```

### Private Registry
```
# create.
dataroaster privateregistry create \
--core-host harbor-core-test.cloudchef-labs.com \
--notary-host harbor-notary-test.cloudchef-labs.com \
--registry-storage-size 5 \
--chartmuseum-storage-size 5 \
--jobservice-storage-size 1 \
--database-storage-size 1 \
--redis-storage-size 1 \
--trivy-storage-size 5 \
--s3-bucket harbor \
--s3-accessKey TOW32G9ULH63MTUI6NNW \
--s3-secretKey jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com;

# delete.
dataroaster privateregistry delete;
```

### CI / CD
```
# create.
dataroaster cicd create \
--argocd-ingress-host argocd-test.cloudchef-labs.com \
--jenkins-ingress-host jenkins-test.cloudchef-labs.com;

# delete.
dataroaster cicd delete;
```

### Backup
```
# create.
dataroaster backup create \
--s3-bucket velero-backups \
--s3-accessKey TOW32G9ULH63MTUI6NNW \
--s3-secretKey jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com;

# delete.
dataroaster backup delete;
```

### Data Catalog
```
# create.
dataroaster datacatalog create \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com \
--storage-size 1;

# delete.
dataroaster datacatalog delete;
```

### Query Engine
```
# create.
dataroaster queryengine create \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com \
--spark-thrift-server-executors 1 \
--spark-thrift-server-executor-memory 1 \
--spark-thrift-server-executor-cores 1 \
--spark-thrift-server-driver-memory 1 \
--trino-workers 3 \
--trino-server-max-memory 16 \
--trino-cores 1 \
--trino-temp-data-storage 1 \
--trino-data-storage 1;

# delete.
dataroaster queryengine delete;
```

### Streaming
```
# create.
dataroaster streaming create \
--kafka-replica-count 3 \
--kafka-storage-size 4 \
--zk-replica-count 3;


# delete.
dataroaster streaming delete;
```

### Analytics
```
# create.
dataroaster analytics create \
--jupyterhub-github-client-id 0b322767446baedb3203 \
--jupyterhub-github-client-secret 828688ff8be545b6434df2dbb2860a1160ae1517 \
--jupyterhub-ingress-host jupyterhub-test.cloudchef-labs.com \
--jupyterhub-storage-size 1 \
--redash-storage-size 1;

# delete.
dataroaster analytics delete;
```

### Workflow
```
# create.
dataroaster workflow create \
--storage-size 1 \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint ceph-rgw-test.cloudchef-labs.com;

# delete.
dataroaster workflow delete;
```


## License
The use and distribution terms for this software are covered by the Apache 2.0 license.







