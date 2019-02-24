.PHONY: default
LTS_VERSION_FILE = LTS_VERSION.txt
LTS_VERSION = `cat $(LTS_VERSION_FILE)`
DEFAULT_BUILD_ARGS = --build-arg http_proxy=$(http_proxy) --build-arg https_proxy=$(https_proxy) --build-arg no_proxy=$(no_proxy)

default: test

build: build-alpine build-debian build-slim

test: test-alpine test-debian test-slim

build-alpine:
	docker build --rm --force-rm -t odavid/my-bloody-jenkins $(DEFAULT_BUILD_ARGS) --build-arg=FROM_TAG=$(LTS_VERSION)-alpine .

build-debian:
	docker build --rm --force-rm -t odavid/my-bloody-jenkins $(DEFAULT_BUILD_ARGS) --build-arg=FROM_TAG=$(LTS_VERSION) .

build-slim:
	docker build --rm --force-rm -t odavid/my-bloody-jenkins $(DEFAULT_BUILD_ARGS) --build-arg=FROM_TAG=$(LTS_VERSION)-slim .

test-alpine: build-alpine
	bats tests

test-debian: build-debian
	bats tests

test-slim: build-slim
	bats tests

update-plugins:
	env python $(PWD)/get-latest-plugins.py
	git diff plugins.txt | grep  '^+' | sed 's|+||' | grep -v + | awk -F \: '{print "* ["$$1":"$$2"](https://plugins.jenkins.io/" $$1 ")"}'

release:
	$(eval NEW_INCREMENT := $(shell expr `git describe --tags --abbrev=0 | cut -d'-' -f2` + 1))
	git tag v$(LTS_VERSION)-$(NEW_INCREMENT)
	git push origin v$(BASE_VERSION)-$(NEW_INCREMENT)
