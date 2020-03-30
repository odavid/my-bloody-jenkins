#!/bin/bash -e
#NOTE: to work with CSRF enabled it requires: '-Dhudson.security.csrf.DefaultCrumbIssuer.EXCLUDE_SESSION_ID=true', ie ENV: JAVA_OPTS_LONGLIVING_CRUMB: '-Dhudson.security.csrf.DefaultCrumbIssuer.EXCLUDE_SESSION_ID=true'

script_dir=$(cd $(dirname "$0"); pwd)
token_file="$TOKEN_FILE_LOCATION"
external_jenkins_admin_creds="$EXTERNAL_JENKINS_ADMIN_CREDENTIALS"

AUTH_ARG=""

if [ -z "$external_jenkins_admin_creds" ]; then
    if [ -f $token_file ]; then
        AUTH_ARG="$(cat $token_file)"
    fi
else
    AUTH_ARG=$external_jenkins_admin_creds
fi 

is_csrf_enabled=$(curl --user $AUTH_ARG -s http://localhost:8080/api/json 2> /dev/null | python -c 'import sys,json;exec "try:\n  j=json.load(sys.stdin)\n  print str(j[\"useCrumbs\"]).lower()\nexcept:\n  pass"')

token_data=""
if [ $is_csrf_enabled == "true" ]; then
    mytoken=$(curl --user $AUTH_ARG -s http://localhost:8080/crumbIssuer/api/json | python -c 'import sys,json;j=json.load(sys.stdin);print j["crumbRequestField"] + "=" + j["crumb"]')
    token_data="-d "$mytoken""
fi

echo "Updating Jenkins Configuration"
curl --user $AUTH_ARG $token_data --data-urlencode "script=$(< /var/jenkins_home/init.groovy.d/JenkinsConfigLoader.groovy)" http://localhost:8080/scriptText
echo "Jenkins Configuration Updated"
