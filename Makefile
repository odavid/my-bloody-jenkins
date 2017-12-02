.PHONY: default

default: test

build:
	docker build --rm --force-rm -t odavid/my-bloody-jenkins .

test: build
	bats tests/bats
