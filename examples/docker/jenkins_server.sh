#!/bin/bash

. ./set_secrets.sh

action='restart'
if [[ $# > 0 ]]; then
   action=$1
fi

export MY_HOST_IP="$(/sbin/ifconfig | grep 'inet ' | grep -Fv 127.0.0.1 | awk '{print $2}' | head -n 1 | sed -e 's/addr://')"
export GIT_PRIVATE_KEY=`cat ~/.ssh/id_rsa`

echo "-------------------------------------------------"
echo "MY_HOST_IP = $MY_HOST_IP"
echo "-------------------------------------------------"

if [[ "$action" == "stop" || "$action" == "restart" ]]; then
   docker-compose down --remove-orphans
   sleep 2
fi

if [[ "$action" == "start" || "$action" == "restart" ]]; then
   docker-compose up -d
   sleep 2
   docker-compose logs -f
fi
