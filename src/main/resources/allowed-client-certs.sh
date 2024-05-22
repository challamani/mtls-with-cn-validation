#!/bin/sh

export APP_ROOT_DIR=$(pwd)
export CERTS_DIR=${APP_ROOT_DIR}/src/main/resources/certs

echo "Generate Client certificates: \n"
openssl genrsa -out ${CERTS_DIR}/client.key 2048
openssl req -new -key ${CERTS_DIR}/client.key -out ${CERTS_DIR}/client.csr

openssl x509 -req -in ${CERTS_DIR}/client.csr -CA ${CERTS_DIR}/ca.crt -CAkey ${CERTS_DIR}/ca.key -CAcreateserial -out ${CERTS_DIR}/client.crt -days 500 -sha256