.PHONY: default

default: test

build:
	docker build --rm --force-rm -t odavid/my-bloody-jenkins .

test: build
	bats tests/bats

update-plugins:
	env python $(PWD)/get-latest-plugins.py

release:
	$(eval NEW_INCREMENT := $(shell expr `git describe --tags --abbrev=0 | cut -d'-' -f2` + 1))
	$(eval BASE_VERSION := $(shell grep FROM Dockerfile | cut -d':' -f 2 | cut -d '-' -f 1))
	git tag v$(BASE_VERSION)-$(NEW_INCREMENT)
	git push origin v$(BASE_VERSION)-$(NEW_INCREMENT)
