# mtls-with-cn-validation
An example microservice that verifies client identities using mTLS and client SAN checks to ensure secure access

## Create following certs in resources directory

### Generate CA, Server Certs

```shell
mkdir -p src/main/resources/certs
./src/main/resources/generate-server-certs.sh
```

## Scenarios

#### Valid Client Request

The mTLS authentication will get established and also request will get passed at SAN/CN validation as well.
Generate Client certs with the CN name that must match with `service.client.allowed-list`

```properties
#Example allowed-list
service.client.allowed-list=allowed.example.com,allowed-client.example.com
```

```shell
export APP_ROOT_DIR=$(pwd)
export CERTS_DIR=${APP_ROOT_DIR}/src/main/resources/certs
./src/main/resources/allowed-client-certs.sh
```

```curl
curl -X GET https://localhost:9443/client-identity/todos \
    --cert ${CERTS_DIR}/client.crt --key ${CERTS_DIR}/client.key --insecure
```

#### Blocked Client Request

The mTLS authentication will get established but the request will fail at SAN/CN validation. 
Generate Client certs with the CN name that must not match with `service.client.allowed-list`

```shell
export APP_ROOT_DIR=$(pwd)
export CERTS_DIR=${APP_ROOT_DIR}/src/main/resources/certs
./src/main/resources/blocked-client-certs.sh
```

```curl
curl -X GET https://localhost:9443/client-identity/todos \
    --cert ${CERTS_DIR}/blocked-client.crt --key ${CERTS_DIR}/blocked-client.key --insecure
```