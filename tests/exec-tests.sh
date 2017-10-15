#! /bin/bash

set -e
script_dir=$(cd $(dirname "$0"); pwd)
# rm -rf .data

docker rm -f -v jenkins-test || true

docker build --rm --force-rm -t odavid/my-bloody-jenkins-tests .

docker run --name jenkins-test -d \
    -e JAVA_OPTS_MEM='-Xmx1g' \
    -e JENKINS_ENV_ADMIN_USER=admin \
    -e JENKINS_ENV_CONFIG_YAML="
security:
    realm: jenkins_database
    adminPassword: admin
" \
    odavid/my-bloody-jenkins-tests

JENKINS_IS_UP=1
echo "before waiting JENKINS_IS_UP: $JENKINS_IS_UP"
while [ "$JENKINS_IS_UP" != "0" ]; do
    JENKINS_IS_UP=$(docker logs jenkins-test 2>&1 | grep "Jenkins is fully up and running" > /dev/null; echo $?)
    echo "Waiting for jenkins to be up... JENKINS_IS_UP=$JENKINS_IS_UP"
    sleep 10
done

docker exec -t jenkins-test run-test.sh /tests/SanityTest.groovy         
    
docker rm -f -v jenkins-test || true
    