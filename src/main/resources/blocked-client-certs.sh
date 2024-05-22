#!/bin/sh

export APP_ROOT_DIR=$(pwd)
export CERTS_DIR=${APP_ROOT_DIR}/src/main/resources/certs

echo "Generate client certificate with the CN name that is not in allowed list: \n"
openssl genrsa -out ${CERTS_DIR}/blocked-client.key 2048
openssl req -new -key ${CERTS_DIR}/blocked-client.key -out ${CERTS_DIR}/blocked-client.csr
openssl x509 -req -in ${CERTS_DIR}/blocked-client.csr -CA ${CERTS_DIR}/ca.crt -CAkey ${CERTS_DIR}/ca.key -CAcreateserial -out ${CERTS_DIR}/blocked-client.crt -days 500 -sha256