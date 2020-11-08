#!/bin/sh

cd {{ tempDirectory }};

# download spark tar file from google drive.
# https://drive.google.com/file/d/1_hpk6p_mgQ3gCA3ZV_cSUuEdt_yZlAaX/view?usp=sharing
SPARK_FILE_NAME=spark-3.0.0-bin-custom-spark
fileId=1_hpk6p_mgQ3gCA3ZV_cSUuEdt_yZlAaX
fileName=${SPARK_FILE_NAME}.tgz

curl -sc /tmp/cookie "https://drive.google.com/uc?export=download&id=${fileId}" > /dev/null
code="$(awk '/_warning_/ {print $NF}' /tmp/cookie)"
curl -Lb /tmp/cookie "https://drive.google.com/uc?export=download&confirm=${code}&id=${fileId}" -o ${fileName}



# download spark thrift server jar file from google drive.
# https://drive.google.com/file/d/1U8-Tlp2783psK-AR_m1TC6CFA4eSeiDL/view?usp=sharing
SPARK_THRIFT_SERVER_FILE_NAME=spark-thrift-server-1.0.0-SNAPSHOT-spark-job
fileId=1U8-Tlp2783psK-AR_m1TC6CFA4eSeiDL
fileName=${SPARK_THRIFT_SERVER_FILE_NAME}.jar

curl -sc /tmp/cookie "https://drive.google.com/uc?export=download&id=${fileId}" > /dev/null
code="$(awk '/_warning_/ {print $NF}' /tmp/cookie)"
curl -Lb /tmp/cookie "https://drive.google.com/uc?export=download&confirm=${code}&id=${fileId}" -o ${fileName}


# download delta core jar file from google drive.
# https://drive.google.com/file/d/1WCzSnwXEYc3Q8VkvvJ5nuidq9yGYDIsa/view?usp=sharing
DELTA_CORE_FILE_NAME=delta-core-shaded-assembly_2.12-0.1.0
fileId=1WCzSnwXEYc3Q8VkvvJ5nuidq9yGYDIsa
fileName=${DELTA_CORE_FILE_NAME}.jar

curl -sc /tmp/cookie "https://drive.google.com/uc?export=download&id=${fileId}" > /dev/null
code="$(awk '/_warning_/ {print $NF}' /tmp/cookie)"
curl -Lb /tmp/cookie "https://drive.google.com/uc?export=download&confirm=${code}&id=${fileId}" -o ${fileName}



# download hive delta file from google drive.
# https://drive.google.com/file/d/1PcSraIo9Fc5sKIuDmDfFytcE8i5DcgN_/view?usp=sharing
HIVE_DELTA_FILE_NAME=hive-delta_2.12-0.1.0
fileId=1PcSraIo9Fc5sKIuDmDfFytcE8i5DcgN_
fileName=${HIVE_DELTA_FILE_NAME}.jar

curl -sc /tmp/cookie "https://drive.google.com/uc?export=download&id=${fileId}" > /dev/null
code="$(awk '/_warning_/ {print $NF}' /tmp/cookie)"
curl -Lb /tmp/cookie "https://drive.google.com/uc?export=download&confirm=${code}&id=${fileId}" -o ${fileName}

# install spark.
mkdir -p spark;
tar zxvf ${SPARK_FILE_NAME}.tgz -C spark/;
cd spark/;
cp -R ${SPARK_FILE_NAME}/* .;
rm -rf ${SPARK_FILE_NAME};

# set spark home.
SPARK_HOME={{ tempDirectory }}/spark;
PATH=$PATH:$SPARK_HOME/bin;


cd {{ tempDirectory }};

echo "whoami: $(whoami), java: $JAVA_HOME"

# export kubeconfig.
export KUBECONFIG={{ kubeconfig }};

echo "KUBECONFIG: $KUBECONFIG";

# submit spark thrift server job.
MASTER=k8s://{{ masterUrl }};
NAMESPACE={{ namespace }};
ENDPOINT={{ endpoint }};

spark-submit \
--master $MASTER \
--deploy-mode cluster \
--name spark-thrift-server \
--class io.spongebob.hive.SparkThriftServerRunner \
--packages com.amazonaws:aws-java-sdk-s3:1.11.375,org.apache.hadoop:hadoop-aws:3.2.0 \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.mount.path=/checkpoint \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.mount.subPath=checkpoint \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.mount.readOnly=false \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.options.claimName=spark-driver-pvc \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.mount.path=/checkpoint \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.mount.subPath=checkpoint \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.mount.readOnly=false \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.options.claimName=spark-exec-pvc \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.path=/localdir \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.readOnly=false \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.options.claimName=spark-driver-localdir-pvc \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.path=/localdir \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.readOnly=false \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.options.claimName=spark-exec-localdir-pvc \
--conf spark.kubernetes.file.upload.path=s3a://{{ bucket }}/spark-thrift-server \
--conf spark.kubernetes.container.image.pullPolicy=Always \
--conf spark.kubernetes.namespace=$NAMESPACE \
--conf spark.kubernetes.container.image=mykidong/spark:v3.0.0 \
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
--conf spark.hadoop.hive.metastore.client.connect.retry.delay=5 \
--conf spark.hadoop.hive.metastore.client.socket.timeout=1800 \
--conf spark.hadoop.hive.metastore.uris=thrift://{{ hiveMetastore }} \
--conf spark.hadoop.hive.server2.enable.doAs=false \
--conf spark.hadoop.hive.server2.thrift.http.port=10002 \
--conf spark.hadoop.hive.server2.thrift.port=10016 \
--conf spark.hadoop.hive.server2.transport.mode=binary \
--conf spark.hadoop.metastore.catalog.default=spark \
--conf spark.hadoop.hive.execution.engine=spark \
--conf spark.hadoop.hive.input.format=io.delta.hive.HiveInputFormat \
--conf spark.hadoop.hive.tez.input.format=io.delta.hive.HiveInputFormat \
--conf spark.sql.warehouse.dir=s3a:/{{ bucket }}/apps/spark/warehouse \
--conf spark.hadoop.fs.defaultFS=s3a://{{ bucket }} \
--conf spark.hadoop.fs.s3a.access.key={{ accessKey }} \
--conf spark.hadoop.fs.s3a.secret.key={{ secretKey }} \
--conf spark.hadoop.fs.s3a.connection.ssl.enabled=true \
--conf spark.hadoop.fs.s3a.endpoint=$ENDPOINT \
--conf spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem \
--conf spark.hadoop.fs.s3a.fast.upload=true \
--conf spark.hadoop.fs.s3a.path.style.access=true \
--conf spark.driver.extraJavaOptions="-Divy.cache.dir=/tmp -Divy.home=/tmp" \
--conf spark.executor.instances={{ executors }} \
--conf spark.executor.memory={{ executorMemory }}G \
--conf spark.executor.cores={{ executorCore }} \
--conf spark.driver.memory={{ driverMemory }}G \
--conf spark.jars={{ tempDirectory }}/${DELTA_CORE_FILE_NAME}.jar,{{ tempDirectory }}/${HIVE_DELTA_FILE_NAME}.jar \
file://{{ tempDirectory }}/${SPARK_THRIFT_SERVER_FILE_NAME}.jar \
> /dev/null 2>&1 &

PID=$!
echo "$PID" > pid;

echo "pid created...";

# check if spark thrift server pod is running.

SPARK_THRIFT_SERVER_IS_RUNNING="False";
check_spark_thrift_server_is_running() {
    echo "check_spark_thrift_server_is_running is being called...";

    POD_STATUS=$(kubectl get pods -n {{ namespace }} -l spark-role=driver -o jsonpath={..status.phase});
    POD_NAME=$(kubectl get pods -n {{ namespace }} -l spark-role=driver -o jsonpath={..metadata.name});

    echo "POD_STATUS: ${POD_STATUS}";
    echo "POD_NAME: ${POD_NAME}";

    if [[ ${POD_STATUS} != "" ]]
    then
      # Set space as the delimiter
      IFS=' ';

      #Read the split words into an array based on space delimiter
      read -a POD_STATUS_ARRAY <<< "$POD_STATUS";
      read -a POD_NAME_ARRAY <<< "$POD_NAME";

      {% raw %}
      #Count the total words
      echo "POD_STATUS_ARRAY count: ${#POD_STATUS_ARRAY[*]}";
      echo "POD_NAME_ARRAY count: ${#POD_NAME_ARRAY[*]}";

      for ((i = 0; i < ${#POD_STATUS_ARRAY[@]}; ++i)); do
          pod_status=${POD_STATUS_ARRAY[i]};
          pod_name=${POD_NAME_ARRAY[i]};
          printf "status: %s, name: %s\n" "${pod_status}" "${pod_name}";

          if [[ $pod_status == "Running" ]]
          then
              if [[ $pod_name =~ "spark-thrift-server" ]]
              then
                  printf "selected pod - status: %s, name: %s\n" "${pod_status}" "${pod_name}";
                  SPARK_THRIFT_SERVER_IS_RUNNING="True";
              fi
          fi
      done
      {% endraw %}
    fi
}

while [[ $SPARK_THRIFT_SERVER_IS_RUNNING != "True" ]];
do
    echo "waiting for spark thrift server being run...";
    sleep 2;
    check_spark_thrift_server_is_running;
done

# kill current spark submit process.
kill $(cat pid);

# create service.
kubectl apply -f spark-thrift-server-service.yaml;

unset KUBECONFIG;
unset SPARK_HOME;






