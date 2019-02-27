#!/bin/bash -e

docker-build(){
    local tag=${1}
    local lts_version=${2}
    local from=${3}
    local to=${4}
    if [[ $from == '-' ]]; then
        local from=""
    fi
    if [[ $to == '-' ]]; then
        local to=""
    fi
    if [ -z $tag ]; then
        echo "tag is required!"
        exit 1
    fi
    if [ -z $lts_version ]; then
        echo "lts_version is required!"
        exit 1
    fi
    [[ -n $from ]] && local from_tag=${lts_version}-${from} || local from_tag=${lts_version}
    echo "docker build --rm --force-rm -t odavid/my-bloody-jenkins:${tag}${to} --build-arg=FROM_TAG=${from_tag} ."
    docker build --rm --pull --force-rm -t odavid/my-bloody-jenkins:${tag}${to} --build-arg=FROM_TAG=${from_tag} .
    echo "docker push odavid/my-bloody-jenkins:${tag}${to}"
}

lts_version=$(cat LTS_VERSION.txt)
tag=${1:-latest}
tag=$(echo $tag | sed 's/^v//g')

docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD

docker-build $tag $lts_version alpine -
docker-build $tag $lts_version alpine -alpine
docker-build $tag $lts_version - -debian
docker-build $tag $lts_version slim -slim
