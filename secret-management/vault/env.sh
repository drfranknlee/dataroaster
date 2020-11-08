#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

###########################################################################
# Vault environment settings. Source this file.                           #
###########################################################################

export VAULT_TOKEN=s.652G9bFIwOC2XQczhZ633UW0
export VAULT_ADDR=https://localhost:8200
export VAULT_SKIP_VERIFY=false
export VAULT_CAPATH=${DIR}/work/ca/certs/ca.cert.pem
