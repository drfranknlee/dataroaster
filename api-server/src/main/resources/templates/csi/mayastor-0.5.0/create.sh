#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

kubectl create ns mayastor;
kubectl create -f moac-rbac.yaml;
kubectl apply -f mayastorpool.yaml;


## Deploy Mayastor Dependencies.
kubectl apply -f nats-deployment.yaml;
kubectl -n mayastor get pods --selector=app=nats

## Deploy Mayastor Components.
kubectl apply -f csi-daemonset.yaml;
kubectl -n mayastor get daemonset mayastor-csi;

kubectl apply -f moac-deployment.yaml;
kubectl get pods -n mayastor --selector=app=moac;

kubectl apply -f mayastor-daemonset.yaml;
kubectl -n mayastor get daemonset mayastor;

kubectl -n mayastor get msn;

## make sure that all the msn are available.
MSN_IS_ONLINE="False";
check_msn_is_online() {
    MSN_STATUS=$(kubectl get msn -n mayastor -o jsonpath={..status});
    MSN_NAME=$(kubectl get msn -n mayastor -o jsonpath={..metadata.name});

    echo "MSN_STATUS: ${MSN_STATUS}";
    echo "MSN_NAME: ${MSN_NAME}";

    if [[ ${MSN_STATUS} != "" ]]
    then
      # Set space as the delimiter
      IFS=' ';

      #Read the split words into an array based on space delimiter
      read -a MSN_STATUS_ARRAY <<< "$MSN_STATUS";
      read -a MSN_NAME_ARRAY <<< "$MSN_NAME";

      {% raw %}
      #Count the total words
      echo "MSN_STATUS_ARRAY count: ${#MSN_STATUS_ARRAY[*]}";
      echo "MSN_NAME_ARRAY count: ${#MSN_NAME_ARRAY[*]}";

      for ((i = 0; i < ${#MSN_STATUS_ARRAY[@]}; ++i)); do
          MSN_STATUS=${MSN_STATUS_ARRAY[i]};
          MSN_NAME=${MSN_NAME_ARRAY[i]};
          printf "status: %s, name: %s\n" "${MSN_STATUS}" "${MSN_NAME}";

          if [[ $MSN_STATUS == "online" ]]
          then
              printf "selected msn - status: %s, name: %s\n" "${MSN_STATUS}" "${MSN_NAME}";
              MSN_IS_ONLINE="True";
              echo "MSN_IS_ONLINE: ${MSN_IS_ONLINE}";
          fi
      done
      {% endraw %}
    fi
}

while [[ MSN_IS_ONLINE != "True" ]];
do
    echo "waiting for msn being online...";
    sleep 2;
    check_msn_is_online;
done


echo "sleep 5s...";
sleep 5;


## create pools.
WORKER_NODES="{{ workerNodes }}";
DISKS="{{ disks }}";

echo "WORKER_NODES: ${WORKER_NODES}";
echo "DISKS: ${DISKS}";

# Set space as the delimiter
IFS=' ';

#Read the split words into an array based on space delimiter
read -a WORKER_NODES_ARRAY <<< "$WORKER_NODES";
read -a DISKS_ARRAY <<< "$DISKS";

{% raw %}
#Count the total words
echo "WORKER_NODES_ARRAY count: ${#WORKER_NODES_ARRAY[*]}";
echo "DISKS_ARRAY count: ${#DISKS_ARRAY[*]}";

for ((i = 0; i < ${#WORKER_NODES_ARRAY[@]}; ++i)); do
    WORKER_NODE=${WORKER_NODES_ARRAY[i]};
    printf "WORKER_NODE: %s\n" "${WORKER_NODE}";

    for ((j = 0; j < ${#DISKS_ARRAY[@]}; ++j)); do
      DISK=${DISKS_ARRAY[j]};
      printf "WORKER_NODE: %s, DISK: %s\n" "${WORKER_NODE}" "${DISK}" ;
      POOL_NAME="pool-${j}-${WORKER_NODE}";
      OUTFILE="${POOL_NAME}.yaml";
      echo "---
apiVersion: \"openebs.io/v1alpha1\"
kind: MayastorPool
metadata:
  name: ${POOL_NAME}
  namespace: mayastor
spec:
  node: ${WORKER_NODE}
  disks:
  - ${DISK}" > ${OUTFILE};
    cat ${OUTFILE} | kubectl apply -f -;
    done
done
{% endraw %}


## make sure that all the pools are available.
while [[ $(kubectl get mayastorpools.openebs.io -n mayastor -o jsonpath={..status.state}) != *"online"* ]]; do echo "waiting for msp being online" && sleep 2; done

echo "sleep 10s...";
sleep 10;

## create storage class.
kubectl apply -f storageclass.yaml;
