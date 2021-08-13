# export spark home and path.
export SPARK_HOME=.;
export PATH=$PATH:${SPARK_HOME}/bin;

# export kubeconfig.
export KUBECONFIG=kubeconfig;

echo "S3_ACCESS_KEY   = ${S3_ACCESS_KEY}"
echo "S3_SECRET_KEY   = ${S3_SECRET_KEY}"
echo "S3_BUCKET       = ${S3_BUCKET}"
echo "S3_ENDPOINT     = ${S3_ENDPOINT}"

export BUCKET=mykidong;


# download kubeconfig from s3.
aws s3api --profile=minio --endpoint=$S3_ENDPOINT get-object --bucket ${S3_BUCKET} --key kubeconfig/admin/kubeconfig ./${KUBECONFIG};


# spark job jar name.
export SPARK_JOB_JAR=spark-example-3.0.0-SNAPSHOT-spark-job.jar;

# download spark job jar from s3.
aws s3api --profile=minio --endpoint=$S3_ENDPOINT get-object --bucket ${S3_BUCKET} --key spark-job/jar/${SPARK_JOB_JAR} ./${SPARK_JOB_JAR};


############## spark job: create delta table

# submit spark job onto kubernetes.
export MASTER=k8s://https://192.168.10.21:6443;
export NAMESPACE=ai-developer;
export HIVE_METASTORE=metastore.ai-developer:9083;
export SPARK_IMAGE=mykidong/spark:v3.0.1

random="$RANDOM-$RANDOM";
export JOB_NAME="spark-delta-example-${random}"
echo "JOB_NAME = ${JOB_NAME}";

spark-submit \
--master ${MASTER} \
--deploy-mode cluster \
--name ${JOB_NAME} \
--class com.cloudcheflabs.dataroaster.spark.examples.DeltaLakeExample \
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
--conf spark.kubernetes.file.upload.path=s3a://${S3_BUCKET}/spark-jobs \
--conf spark.kubernetes.container.image.pullPolicy=Always \
--conf spark.kubernetes.namespace=$NAMESPACE \
--conf spark.kubernetes.container.image=${SPARK_IMAGE} \
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
--conf spark.hadoop.hive.metastore.client.connect.retry.delay=5 \
--conf spark.hadoop.hive.metastore.client.socket.timeout=1800 \
--conf spark.hadoop.hive.metastore.uris=thrift://$HIVE_METASTORE \
--conf spark.hadoop.hive.server2.enable.doAs=false \
--conf spark.hadoop.hive.server2.thrift.http.port=10002 \
--conf spark.hadoop.hive.server2.thrift.port=10016 \
--conf spark.hadoop.hive.server2.transport.mode=binary \
--conf spark.hadoop.metastore.catalog.default=spark \
--conf spark.hadoop.hive.execution.engine=spark \
--conf spark.hadoop.hive.input.format=io.delta.hive.HiveInputFormat \
--conf spark.hadoop.hive.tez.input.format=io.delta.hive.HiveInputFormat \
--conf spark.sql.warehouse.dir=s3a://${S3_BUCKET}/apps/spark/warehouse \
--conf spark.hadoop.fs.defaultFS=s3a://${S3_BUCKET} \
--conf spark.hadoop.fs.s3a.access.key=${S3_ACCESS_KEY} \
--conf spark.hadoop.fs.s3a.secret.key=${S3_SECRET_KEY} \
--conf spark.hadoop.fs.s3a.connection.ssl.enabled=true \
--conf spark.hadoop.fs.s3a.endpoint=$S3_ENDPOINT \
--conf spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem \
--conf spark.hadoop.fs.s3a.fast.upload=true \
--conf spark.hadoop.fs.s3a.path.style.access=true \
--conf spark.driver.extraJavaOptions="-Divy.cache.dir=/tmp -Divy.home=/tmp" \
--conf spark.executor.instances=3 \
--conf spark.executor.memory=2G \
--conf spark.executor.cores=1 \
--conf spark.driver.memory=1G \
file://$(pwd)/${SPARK_JOB_JAR} \
--master ${MASTER};



# check pod job status.
pod_name=$(kubectl get po -n ${NAMESPACE} | grep ${JOB_NAME} | awk '{print $1}');
echo "pod name: $pod_name";

status_phase=$(kubectl get po $pod_name -n ${NAMESPACE} -o jsonpath={..status.phase});
echo "status phase: $status_phase";

if [[ $status_phase == "Succeeded" ]]
then
   echo "job $pod_name succeeded...";
   exit 0;
elif [[ $status_phase == "Failed" ]]
then
   echo "job $pod_name failed...";
   exit 1;
fi