#!/bin/bash

cd {{ tempDirectory }};

export KUBECONFIG={{ kubeconfig }};

kubectl delete -f .;

# make sure all the pools are removed.
MSP=$(kubectl get msp -n mayastor -o jsonpath={..metadata.name});
echo "MSP: ${MSP}";

# Set space as the delimiter
IFS=' ';

#Read the split words into an array based on space delimiter
read -a MSP_ARRAY <<< "${MSP}";

{% raw %}
#Count the total words
echo "MSP_ARRAY count: ${#MSP_ARRAY[*]}";

for ((i = 0; i < ${#MSP_ARRAY[@]}; ++i)); do
    MSP_NAME=${MSP_ARRAY[i]};
    printf "MSP_NAME: %s\n" "${MSP_NAME}";
    kubectl -n mayastor get msp ${MSP_NAME} -o yaml > msp.yaml &&  sed 's/- finalizer.mayastor.openebs.io//' msp.yaml | kubectl replace -f -
done
{% endraw %}