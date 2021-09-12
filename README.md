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

### Ingress Controller
* Ingress Controller NGINX: Popular Ingress Controller
* Cert Manager: manage certificates for ingress resources


## DataRoaster Kubernetes Version Matrix
| DataRoaster | Kubernetes  | 
| ------- | --- | 
| 3.0.1 | 1.17 | 


## DataRoaster Component Version Matrix

### DataRoaster 3.0.1
| Component | Version  | 
| ------- | --- | 
| Hive Metastore | 3.0.0 | 
| Spark Thrift Server | Spark 3.0.3 | 
| Trino | 360 | 
| Redash | 10.0.0-beta.b49597 | 
| JupyterHub | 1.4.2 | 
| Kafka | 2.8.0 | 




## DataRoaster Demo
This demo shows how to create the components like hive metastore, spark thrift server, trino, redash and jupyterhub deployed on Kubernetes with ease using DataRoaster.

[![DataRoaster Demo](http://www.cloudchef-labs.com/images/demo-thumbnail.jpg)](https://youtu.be/AeqkkQDwPqY "DataRoaster Demo")






## Install DataRoaster with ansible
With dataroaster ansible playbook, dataroaster will be installed automatically.

The following components will be installed with dataroaster ansible playbook.
* JDK 1.8
* Kubectl
* Helm
* MySQL Server
* Vault
* DataRoaster API Server
* DataRoaster Authorizer
* DataRoaster CLI


### Download and extract ansible playbook for dataroaster installation
```
curl -L -O https://github.com/cloudcheflabs/dataroaster/releases/download/3.0.1/dataroaster-ansible-3.0.1-dist.tgz
tar zxvf dataroaster-ansible-3.0.1-dist.tgz
cd dataroaster/
```

### Edit inventory
Edit the file `inventory/dataroaster.ini`
```
...
[all]
dataroaster ansible_ssh_host=<ip-address> ip=<ip-address>
...
```
`<ip-address>` is the ip address of the machine where dataroaster will be installed.

### Run ansible playbook
Now, you can run ansible playbook to install/uninstall/reinstall/start/stop/restart DataRoaster automatically.
The following `<sudo-user>` is sudo user who will execute ansible playbook on local and remote machine.

#### Install
```
ansible-playbook -i inventory/dataroaster.ini install-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

You will meet the prompts while installing vault.
```
...
              "stdout_lines": [
                    "Unseal Key 1: QZ27JD9nJOPQLozKUvbwdHSTHKafOprwT4xw+RGUxBLI",
                    "Unseal Key 2: GxnjXc5IHo3vRuh8boQD+u4FZM7nW+Y5xpWRXTSXfHBe",
                    "Unseal Key 3: phA5yLU2csyAME9e8H+3NzmYq7ypilksIzLxkanmKUvl",
                    "Unseal Key 4: BVZx/+hL6MLYcwkvONFD3CXZj8ND2yAlSPrvZ6+3lRN9",
                    "Unseal Key 5: etU5dE+Nn+tYztFqoffUOJPQc5vy4RZuinAghI8RHVUH",
                    "",
                    "Initial Root Token: s.M6MNcOX92nAZjEwH5u4yVkbn",
                    "",
                    "Vault initialized with 5 key shares and a key threshold of 3. Please securely",
                    "distribute the key shares printed above. When the Vault is re-sealed,",
                    "restarted, or stopped, you must supply at least 3 of these keys to unseal it",
                    "before it can start servicing requests.",
                    "",
                    "Vault does not store the generated master key. Without at least 3 key to",
                    "reconstruct the master key, Vault will remain permanently sealed!",
                    "",
                    "It is possible to generate new unseal keys, provided you have a quorum of",
                    "existing unseal keys shares. See \"vault operator rekey\" for more information."
                ]
...
TASK [vault/install : prompt for unseal vault 1] *********************************************************************************************************************************************
[WARNING]: conditional statements should not include jinja2 templating delimiters such as {{ }} or {% %}. Found: ("{{ run_option }}" == "reinstall")
[vault/install : prompt for unseal vault 1]
Enter 1. Unseal Key :
```
Because thease generated unseal keys and initial root token of vault cannot be obtained again, you have to copy them to your file. Enter the unseal keys and initial root token of vault for the prompts.

You will also encounter the prompt to enter vault init. root token while installing apiserver like this:
```
...
TASK [apiserver/install : prompt for vault initial root token] *******************************************************************************************************************************
[WARNING]: conditional statements should not include jinja2 templating delimiters such as {{ }} or {% %}. Found: ("{{ run_option }}" == "reinstall")
[apiserver/install : prompt for vault initial root token]
Enter vault initial root token :
```
Enter initial root token of vault which you have obtained above.


#### Uninstall
```
ansible-playbook -i inventory/dataroaster.ini uninstall-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

#### Reinstall
```
ansible-playbook -i inventory/dataroaster.ini reinstall-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

#### Start
```
ansible-playbook -i inventory/dataroaster.ini start-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

#### Stop
```
ansible-playbook -i inventory/dataroaster.ini stop-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```

#### Restart
```
ansible-playbook -i inventory/dataroaster.ini restart-all.yml \
--extra-vars "exec_user=<sudo-user> target_hosts=all";
```



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
CREATE USER 'dataroaster'@'localhost' IDENTIFIED BY 'dataroaster123';
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
  username = "dataroaster"
  password = "dataroaster123"
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

#### Enable KV Secret Engine
Enable KV Secret Engine, for instance.
```
export VAULT_ADDR="https://localhost:8200";

# set initial root token generated.
export VAULT_TOKEN=s.ZuCVMzRIM4eYjMZuvDXqOBJg

# Enable the kv secrets engine at: secret/
vault secrets enable -path=secret/ kv

### test secret engine at the path of 'secret/'
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
CREATE USER 'dataroaster'@'localhost' IDENTIFIED BY 'dataroaster123';
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
mvn -e -DskipTests=true clean install assembly:single;

cp target/authorizer-*-dist.tgz $AUTHORIZER_HOME;

cd $AUTHORIZER_HOME;
tar zxvf authorizer-*-dist.tgz;
cd authorizer;

# start authorizer.
./start-authorizer.sh;
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
mvn -e -DskipTests=true clean install assembly:single;

cp target/api-server-*-dist.tgz $APISERVER_HOME;

cd $APISERVER_HOME;
tar zxvf api-server-*-dist.tgz;
```

Replace the value of `vault.token` in `$APISERVER_HOME/apiserver/conf/application-prod.yml` with the initial root token generated when vault was initialized.


Run API Server:
```
cd apiserver;

./start-apiserver.sh;
```

### Install CLI
```
export DATAROASTER_SRC=~/dataroaster;

# build all.
cd $DATAROASTER_SRC;
mvn -e -DskipTests=true clean install;

# move to cli source.
cd $DATAROASTER_SRC/cli;

# package dist.
mvn -e -DskipTests=true clean install assembly:single;

# untar.
cd target;
tar zxvf cli-*-dist.tgz;

# install cli.
cp cli/dataroaster ~/bin/dataroaster;

# check commands.
dataroaster;
```


## Getting started

### Step 1: Login to API Server
```
dataroaster login http://localhost:8082;
...

# user / password: dataroaster / dataroaster123
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

### Step 5: Create Ingress Controller NGINX and Cert Manager in your kubernetes cluster
All the ingresses of DataRoaster services will be created with this ingress controller,
and all the certificates for the ingresses will be managed by this cert manager.
```
dataroaster ingresscontroller create;
```

### Step 6: Create Services like Data Catalog, Query Engine, etc in your Project
See [DataRoaster CLI usage](https://github.com/cloudcheflabs/dataroaster#dataroaster-cli-usage) how to create services.

## Usage Example: DataRoater Demo
As shown in dataroaster demo video above, the architecture of the demo looks like this.

![Demo Architecture](https://miro.medium.com/max/1400/1*5htePIy2DKpuzFuI7wWU9g.png)

For this demo, ceph storage as external storage has been used by which ceph storage class has been installed on kubernetes. Ceph also provides S3 API and can be used as s3 compatible object storage.

The scenario of the demo is:
* create parquet table in s3 compatible object storage which is provided by ceph storage with running spark example job using hive metastore.
* query data in parquet table saved in ceph using spark thrift server and trino which use hive metastore.
* query data with the connectors to spark thrift server and trino coordinator from redash and jupyter.

### Create Data Catalog
Hive metastore will be created.

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

### Create Query Engine
Spark thrift server(hive on spark) and trino will be created. Both of them depends on hive metastore which needs to be installed on your kubernetes cluster before.

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

### Create Parquet Table using Spark Example Job
This is simple spark job to create parquet table in ceph s3 object storage using hive metastore.

```
cd <dataroaster-src>/components/hive/spark-thrift-server;
mvn -e -Dtest=JsonToParquetTestRunner \
-DmetastoreUrl=$(kubectl get svc metastore-service -n dataroaster-hivemetastore -o jsonpath={.status.loadBalancer.ingress[0].ip}):9083 \
-Ds3Bucket=mykidong \
-Ds3AccessKey=TOW32G9ULH63MTUI6NNW \
-Ds3SecretKey=jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
-Ds3Endpoint=https://ceph-rgw-test.cloudchef-labs.com \
test;
```

### Query Data using CLI

#### Connect to Spark Thrift Server using Beeline
Query data in parquet table created by the spark job above with the connection to spark thrift server using beeline.

```
cd ${SPARK_HOME};
export SPARK_THRIFT_SERVER_NAMESPACE=dataroaster-spark-thrift-server;
bin/beeline -u jdbc:hive2://$(kubectl get svc spark-thrift-server-service -n ${SPARK_THRIFT_SERVER_NAMESPACE} -o jsonpath={.status.loadBalancer.ingress[0].ip}):10016;

...
# query data.
show tables;
select * from test_parquet;
select count(*) from test_parquet;
...

```

#### Connect to Trino Coordinator using Trino CLI
Query data in parquet table created by the spark job above with the connection to trino coordinator using trino cli.

```
kubectl exec -it trino-cli -n dataroaster-trino -- /opt/trino-cli --server trino-coordinator:8080 --catalog hive --schema default;

...
# query data.
show tables;
select * from test_parquet;
select count(*) from test_parquet;
...
```

### Create Analytics
Redash and jupyterhub will be created.

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

### Query Data from Redash and Jupyter

#### Query Data from Redash
Query data in parquet tables using hive connector to spark thrift server and trino connector to trino coordinator from redash.

```
# get external ip of redash loadbalancer.
kubectl get svc -n dataroaster-redash;

# redash ui
http://<external-ip>:5000/

# get external ip of trino service.
kubectl get svc -n dataroaster-trino;

# get external ip of spark thrift server service.
kubectl get svc -n dataroaster-spark-thrift-server;
```

#### Query Data from Jupyter
Query data in parquet table with trino connector to trino coordinator from jupyter.

```
# jupyterhub ui.
https://jupyterhub-test.cloudchef-labs.com/

# trino example in jupyter.

## get external ip of trino service.
kubectl get svc -n dataroaster-trino;

...
from pyhive import trino
host_name = "146.56.138.128"
port = 8080
protocol = "http"
user = "anyuser"
password = None
schema = "default"
catalog = "hive"
def trinoconnection(host_name, port, protocol, user, password, schema, catalog):
    conn = trino.connect(host=host_name, port=port, username=user, password=password, schema=schema, catalog=catalog)
    cur = conn.cursor()
    cur.execute('select * from test_parquet')
    result = cur.fetchall()	
    return result
	
# Call above function
output = trinoconnection(host_name, port, protocol, user, password, schema, catalog)
print(output)
...	
```
`host_name` needs to be replaced with external ip of trino service. To get the external ip of it:
```
kubectl get svc -n dataroaster-trino;
```


## DataRoaster CLI Usage

Most of the components provided by DataRoaster will be deployed as statefulset on kubernetes, so storage classes should be installed on your kubernetes cluster to provision persistent volumes automatically.
If you use managed kubernetes services provided by public cloud providers, you don't have to install storage classes for most of cases, but if your kubernetes cluster is installed in on-prem environment, you have to install storage class on your kubernetes cluster for yourself. For instance, if you have installed ceph as external storage, ceph storage class can be installed on your kubernetes cluster, see this blog how to do it: https://itnext.io/provision-volumes-from-external-ceph-storage-on-kubernetes-and-nomad-using-ceph-csi-7ad9b15e9809.

S3 compatible object storage will be also required to save data for several components provided by DataRoaster. There are many S3 compatible object storages out there, for example you can use the following:
* MinIO: Popular S3 compatible object storage, see https://min.io/
* Ceph S3 compatible object storage: ceph provides S3 API, that is, ceph can be used as S3 compatible object storage. See https://docs.ceph.com/en/latest/radosgw/
* AWS S3: aws s3 object storage.


### Login
Login to API server.

```
dataroaster login <server>
```
* `server`: API Server URL.


Example:
```
dataroaster login http://localhost:8082;
...

# user / password: dataroaster / dataroaster123
```


### Cluster
Register Kubernetes Clusters.

#### Create Cluster
```
dataroaster cluster create <params>
```
* `name`: kubernetes cluster name.
* `description`: description of the kubernetes cluster.

Example:
```
dataroaster cluster create --name dataroaster-cluster --description dataroaster-desc...;
```

#### Update Cluster
```
dataroaster cluster update;
```

#### Delete Cluster
```
dataroaster cluster delete;
```


### Kubeconfig
Upload kubeconfig file for the kubernetes cluster.

#### Create Kubeconfig
```
dataroaster kubeconfig create <params>
```
* `kubeconfig`: kubeconfig file path.

Example:
```
dataroaster kubeconfig create --kubeconfig ~/.kube/config
```

#### Update Kubeconfig
```
dataroaster kubeconfig update <params>
```
* `kubeconfig`: kubeconfig file path.

Example:
```
dataroaster kubeconfig update --kubeconfig ~/.kube/config
```


### Project
Manage Project where services will be created.

#### Create Project
```
dataroaster project create <params>
```
* `name`: kubernetes cluster name.
* `description`: description of the kubernetes cluster.

Example:
```
dataroaster project create  --name new-test-project --description new-test-desc;
```

#### Update Project
```
dataroaster project update;
```

#### Delete Project
```
dataroaster project delete;
```


### Ingress Controller
Manage Ingress Controller NGINX and Cert Manager.

#### Create Ingress Controller
Ingress controller nginx and cert manager will be created.
```
dataroaster ingresscontroller create;
```

#### Delete Ingress Controller
```
dataroaster ingresscontroller delete;
```


### Data Catalog
Manage Data Catalog.

#### Create Data Catalog
Hive metastore and mysql server will be created.
```
dataroaster datacatalog create <params>
```
* `s3-bucket`: s3 bucket for hive metastore warehouse.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.
* `storage-size`: mysql storage size in GiB.

Example:
```
dataroaster datacatalog create \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com \
--storage-size 1;
```

#### Delete Data Catalog
```
dataroaster datacatalog delete;
```

### Query Engine
Manage Query Engine.

#### Create Query Engine
Spark thrift server(hive on spark) and trino will be created.

Query engine service depends on Data Catalog servcice. Before creating query engine service, you have to create data catalog service above on your kubernetes cluster.

To run spark thrift server on kubernetes, `ReadWriteMany` supported storage class, for instance, nfs, is required to save intermediate data on PVs.
To install nfs storage class, run the following helm chart.
```
cd <dataroaster-src>/component/nfs/nfs-server-provisioner-1.1.1;

helm install \
nfs-server . \
--set replicaCount=1 \
--set namespace=nfs \
--set persistence.enabled=true \
--set persistence.size=1000Gi \
--set persistence.storageClass=<storage-class>;
```
`<storage-class>` can be `ReadWriteOnce` supported storage class already installed on kubernetes cluster.

Run the following command to create query engine service including spark thrift server and trino.
```
dataroaster queryengine create <params>
```
* `s3-bucket`: s3 bucket where spark thrift server jar will be uploaded.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.
* `spark-thrift-server-executors`: executor count of spark thrift server.
* `spark-thrift-server-executor-memory`: spark thrift server executor memory in GB.
* `spark-thrift-server-executor-cores`: spark thrift server executor core count.
* `spark-thrift-server-driver-memory`: spark thrift server driver memory in GB.
* `trino-workers`: trino worker count.
* `trino-server-max-memory`: trino server max. memory in GB.
* `trino-cores`: trino server core count.
* `trino-temp-data-storage`: trino temporary data storage size in GiB.
* `trino-data-storage`: trino data storage size in GB.

Example:
```
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
```


#### Delete Query Engine
```
dataroaster queryengine delete;
```

### Streaming
Manage Streaming.

#### Create Streaming
Kafka will be created.
```
dataroaster streaming create <params>
```
* `kafka-replica-count`: kafka node count.
* `kafka-storage-size`: kafka storage size in GiB.
* `zk-replica-count`: zookeeper node count.

Example:
```
dataroaster streaming create \
--kafka-replica-count 3 \
--kafka-storage-size 4 \
--zk-replica-count 3;
```

#### Delete Streaming
```
dataroaster streaming delete;
```

### Analytics
Manage Analytics.

#### Create Analytics
Redash and jupyterhub will be created.
```
dataroaster analytics create <params>
```
* `jupyterhub-github-client-id`: jupyterhub github oauth client id.
* `jupyterhub-github-client-secret`: jupyterhub github oauth client secret.
* `jupyterhub-ingress-host`: jupyterhub ingress host name.
* `jupyterhub-storage-size`: storage size in GiB of single jupyter instance.
* `redash-storage-size`: redash database storage size in GiB.

Example:
```
dataroaster analytics create \
--jupyterhub-github-client-id 0b322767446baedb3203 \
--jupyterhub-github-client-secret 828688ff8be545b6434df2dbb2860a1160ae1517 \
--jupyterhub-ingress-host jupyterhub-test.cloudchef-labs.com \
--jupyterhub-storage-size 1 \
--redash-storage-size 1;
```

#### Delete Analytics
```
dataroaster analytics delete;
```

### Workflow
Manage Workflow.

#### Create Workflow
Argo Workflow will be created.
```
dataroaster workflow create <params>
```
* `storage-size`: database storage size in GiB.
* `s3-bucket`: s3 bucket where application logs of workflow will be saved.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.

Example:
```
dataroaster workflow create \
--storage-size 1 \
--s3-bucket mykidong \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint ceph-rgw-test.cloudchef-labs.com;
```

#### Delete Workflow
```
dataroaster workflow delete;
```



### Pod Log Monitoring
Manage Pod Log Monitoring.

#### Create Pod Log Monitoring
Logstash and filebeat will be created.
```
dataroaster podlogmonitoring create <params>
```
* `elasticsearch-hosts`: List of Elasticsearch hosts.

Example:
```
dataroaster podlogmonitoring create \
--elasticsearch-hosts 192.168.10.10:9200,192.168.10.134:9200,192.168.10.145:9200;
```

#### Delete Pod Log Monitoring
```
dataroaster podlogmonitoring delete;
```


### Metrics Monitoring
Manage Metrics Monitoring.

#### Create Metrics Monitoring
Prometheus, grafana, metrics server will be created.
```
dataroaster metricsmonitoring create;
```

#### Delete Metrics Monitoring
```
dataroaster metricsmonitoring delete;
```

### Distributed Tracing
Manage Distributed Tracing.

#### Create Distributed Tracing
Jaeger will be created.
```
dataroaster distributedtracing create <params>
```
* `ingress-host`: Host name of jaeger ingress.
* `elasticsearch-host-port`: an elasticsearch host and port.

Example:
```
dataroaster distributedtracing create \
--ingress-host ingress-nginx-jaeger-test.cloudchef-labs.com \
--elasticsearch-host-port 192.168.10.10:9200;
```

#### Delete Distributed Tracing
```
dataroaster distributedtracing delete;
```

### Private Registry
Manage private registry for docker images and helm charts.

#### Create Private Registry
Harbor will be created.
```
dataroaster privateregistry create <params>
```
* `core-host`: Harbor core ingress host name.
* `notary-host`: Harbor notary ingress host name.
* `registry-storage-size`: regisry storage size in GiB.
* `chartmuseum-storage-size`: chart museum storage size in GiB.
* `jobservice-storage-size`: job service storage size in GiB.
* `database-storage-size`: database storage size in GiB.
* `redis-storage-size`: redis storage size in GiB.
* `trivy-storage-size`: trivy storage size in GiB.
* `s3-bucket`: name of s3 bucket where artifacts of harbor will be saved.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.

Example:
```
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
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com;
```

#### Delete Private Registry
```
dataroaster privateregistry delete;
```

### CI / CD
Manage CI / CD.

#### Create CI / CD
Argo cd and jenkins will be created.
```
dataroaster cicd create <params>
```
* `argocd-ingress-host`: ingress host name of Argo CD.
* `jenkins-ingress-host`: ingress host name of Jenkins.

Example:
```
dataroaster cicd create \
--argocd-ingress-host argocd-test.cloudchef-labs.com \
--jenkins-ingress-host jenkins-test.cloudchef-labs.com;
```

#### Delete CI / CD
```
dataroaster cicd delete;
```

### Backup
Manage Backup for Persistent Volumes and resources.

#### Create Backup
Velero will be created.
```
dataroaster backup create <params>
```
* `s3-bucket`: s3 bucket for backup.
* `s3-access-key`: s3 access key.
* `s3-secret-key`: s3 secret key.
* `s3-endpoint`: s3 endpoint.

Example:
```
dataroaster backup create \
--s3-bucket velero-backups \
--s3-access-key TOW32G9ULH63MTUI6NNW \
--s3-secret-key jXqViVmSqIDTEKKKzdgSssHVykBrX4RrlnSeVgMi \
--s3-endpoint https://ceph-rgw-test.cloudchef-labs.com;
```

#### Delete Backup
```
dataroaster backup delete;
```


## Community

* DataRoaster Community Mailing Lists: https://groups.google.com/g/dataroaster



## License
The use and distribution terms for this software are covered by the Apache 2.0 license.







