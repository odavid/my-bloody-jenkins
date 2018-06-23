#!/usr/bin/env python
from __future__ import print_function
import os
from six import string_types
import yaml
import re

def expand_envvars(data):
    if not data:
        return data
    DOLLAR = r"\&#36;"
    escaped = data.replace(r"\$", r"\%s" % DOLLAR)
    return os.path.expandvars(escaped).replace(DOLLAR, "$")

def replace_envvars(data):
    if isinstance(data, dict):
        return { k: replace_envvars(v) for k, v in data.items() }
    if isinstance(data, list):
        return [ replace_envvars(i) for i in data ]
    if isinstance(data, string_types):
        return expand_envvars(data)
    return data

def load_config(conffile):
    with open(conffile, 'r') as f:
        return yaml.safe_load(f)

def write_config(config, conffile, backup=False):
    if backup:
        os.rename(conffile, "%s.bak" % conffile)
    with open('%s.new' % conffile, 'w') as f:
        return yaml.safe_dump(config, f, default_flow_style=False)

def main():
    cfg = load_config('test.yml')
    write_config(replace_envvars(cfg), 'test.yml')

if __name__ == '__main__':
    main()