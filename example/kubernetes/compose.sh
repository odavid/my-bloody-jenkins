#!/bin/bash

export MINIKUBE_CERT_PASSWORD=secret
if [ ! -f ~/.minikube/minikube.pfx ]; then
    openssl pkcs12 -export \
         -out ~/.minikube/minikube.pfx \
         -inkey ~/.minikube/apiserver.key \
         -in ~/.minikube/apiserver.crt \
         -certfile ~/.minikube/ca.crt \
         -passout pass:${MINIKUBE_CERT_PASSWORD}
fi
export MINIKUBE_CERT="$(cat ~/.minikube/minikube.pfx | base64)"
export MINIKUBE_IP="$(minikube ip)"
export MINIKUBE_PORT=8443
export MY_HOST_IP="$(ifconfig | grep "inet " | grep -Fv 127.0.0.1 | awk '{print $2}' | head -n 1)"

docker-compose "$@"

