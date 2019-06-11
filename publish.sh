#!/bin/bash -e

docker-build(){
    local from_tag=${1}
    local to_tag=${2}
    echo "docker build --rm --force-rm -t odavid/my-bloody-jenkins:${to_tag} --build-arg=FROM_TAG=${from_tag} ."
    docker build --rm --pull --force-rm -t odavid/my-bloody-jenkins:${to_tag} --build-arg=FROM_TAG=${from_tag} .
    echo "docker push odavid/my-bloody-jenkins:${to_tag}"
    docker push odavid/my-bloody-jenkins:${to_tag}
}

echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
lts_version=$(cat LTS_VERSION.txt)
version_type=$1
case $version_type in
latest)
    docker-build ${lts_version}-alpine latest
    docker-build ${lts_version}-alpine alpine
    docker-build ${lts_version} debian
    docker-build ${lts_version}-jdk11 jdk11
    ;;
v*)
    tag=$(echo $version_type | sed 's/v//g')
    short_tag=$(echo $tag | cut -d '-' -f 1)

    docker-build ${lts_version}-alpine $tag
    docker-build ${lts_version}-alpine $short_tag
    docker-build ${lts_version}-alpine lts
    docker-build ${lts_version}-alpine lts-alpine

    docker-build ${lts_version} ${tag}-debian
    docker-build ${lts_version} ${short_tag}-debian
    docker-build ${lts_version} lts-debian

    docker-build ${lts_version}-jdk11 ${tag}-jdk11
    docker-build ${lts_version}-jdk11 ${short_tag}-jdk11
    docker-build ${lts_version}-jdk11 lts-jdk11
    ;;
*)
    tag=$version_type
    docker-build ${lts_version}-alpine $tag
    docker-build ${lts_version}-alpine $tag-alpine
    docker-build ${lts_version} $tag-debian
    docker-build ${lts_version}-jdk11 $tag-jdk11
    ;;
esac
#docker-build $1 $2
