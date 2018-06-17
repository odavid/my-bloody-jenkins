.PHONY: default

default: test

build:
	docker build --rm --force-rm -t odavid/my-bloody-jenkins --build-arg http_proxy=$(http_proxy) --build-arg https_proxy=$(https_proxy) --build-arg no_proxy=$(no_proxy) .

test: build
	bats tests/bats

update-plugins:
	env python $(PWD)/get-latest-plugins.py
	git diff plugins.txt | grep  '^+' | sed 's|+||' | grep -v + | awk -F \: '{print "* ["$$1":"$$2"](https://plugins.jenkins.io/" $$1 ")"}'

release:
	$(eval NEW_INCREMENT := $(shell expr `git describe --tags --abbrev=0 | cut -d'-' -f2` + 1))
	$(eval BASE_VERSION := $(shell grep FROM Dockerfile | cut -d':' -f 2 | cut -d '-' -f 1))
	git tag v$(BASE_VERSION)-$(NEW_INCREMENT)
	git push origin v$(BASE_VERSION)-$(NEW_INCREMENT)
