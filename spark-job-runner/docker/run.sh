#!/bin/bash

for i in "$@"
do
case $i in
    --run.job.path=*)
    RUN_JOB_PATH="${i#*=}"
    shift # past argument=value
    ;;
    *)
          # unknown option
    ;;
esac
done

echo "RUN_JOB_PATH    = ${RUN_JOB_PATH}"

echo "S3_ACCESS_KEY   = ${S3_ACCESS_KEY}"
echo "S3_SECRET_KEY   = ${S3_SECRET_KEY}"
echo "S3_ENDPOINT     = ${S3_ENDPOINT}"
echo "S3_BUCKET       = ${S3_BUCKET}"

# configure aws client with profile minio.
aws configure --profile=minio set default.s3.signature_version s3v4
aws configure --profile=minio set aws_access_key_id ${S3_ACCESS_KEY}
aws configure --profile=minio set aws_secret_access_key ${S3_SECRET_KEY}
aws configure --profile=minio set region us-west-1

export RUN_JOB_SH=run-job.sh

# download run job shell file from s3.
aws s3api --profile=minio --endpoint=$S3_ENDPOINT get-object --bucket ${S3_BUCKET} --key ${RUN_JOB_PATH} ./${RUN_JOB_SH};

# run job.
echo "ready to run main job..."
chmod a+x ./${RUN_JOB_SH};
./${RUN_JOB_SH};


