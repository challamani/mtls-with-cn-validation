#!/bin/sh

export APP_ROOT_DIR=$(pwd)
export CERTS_DIR=${APP_ROOT_DIR}/src/main/resources/certs

echo "CERTS_DIR: ${CERTS_DIR}\n"

echo "Create CA certification:: \n"
openssl genrsa -out ${CERTS_DIR}/ca.key 2048
openssl req -x509 -new -nodes -key ${CERTS_DIR}/ca.key -sha256 -days 1024 -out ${CERTS_DIR}/ca.crt

echo "\nGenerate Server certificate:"

openssl genrsa -out ${CERTS_DIR}/server.key 2048
openssl req -new -key ${CERTS_DIR}/server.key -out ${CERTS_DIR}/server.csr
openssl x509 -req -in ${CERTS_DIR}/server.csr -CA ${CERTS_DIR}/ca.crt -CAkey ${CERTS_DIR}/ca.key \
  -CAcreateserial -out ${CERTS_DIR}/server.crt -days 500 -sha256

echo "Convert server crt and key to jks format: \n"
openssl pkcs12 -export -in ${CERTS_DIR}/server.crt -inkey ${CERTS_DIR}/server.key -out ${CERTS_DIR}/server.p12 -name server

keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore ${CERTS_DIR}/server.jks -srckeystore ${CERTS_DIR}/server.p12 -srcstoretype PKCS12 -srcstorepass changeit -alias server
keytool -import -alias ca -file ${CERTS_DIR}/ca.crt -keystore ${CERTS_DIR}/truststore.jks -storepass changeit -noprompt