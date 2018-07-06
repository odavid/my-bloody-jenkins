#!/usr/bin/env python
from __future__ import print_function
import argparse
import os
from six import string_types
import yaml
import re
import sys

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
    if not os.path.exists(conffile):
        return None
    with open(conffile, 'r') as f:
        return yaml.safe_load(f)

def write_config(config, conffile, backup=False):
    if backup:
        os.rename(conffile, "%s.bak" % conffile)
    with open(conffile, 'w') as f:
        return yaml.safe_dump(config, f, default_flow_style=False)

def load_env_vars_from_dir(directory):
    directory = os.path.expanduser(directory)
    envvars = {}
    if os.path.isdir(directory):
        filenames = [os.path.join(directory, f) for f in os.listdir(directory)]
        filenames = sorted(filenames, key=os.path.abspath)
        for filename in filenames:
            if(os.path.isfile(filename)):
                name = os.path.basename(filename)
                prefix = os.path.basename(os.path.dirname(filename))
                env_var_name = ('%s_%s' % (prefix, name)).upper().replace('.', '_').replace('-', '_')
                with open(filename, 'rb') as f:
                    value = f.read()
                    envvars[env_var_name] = value
    return envvars

def main():
    parser = argparse.ArgumentParser(description='Load configuration yaml and substitute environment variables. Also loads environment variables from files based on directory search path')
    parser.add_argument('--source', type=str, help='Source config file')
    parser.add_argument('--env-dirs', type=str, help='Directories to load environment variables from', default='')
    parser.add_argument('--out', type=str, help='Output config file')
    args = parser.parse_args()
    if not args.source:
        exit(parser.print_usage())
    else:
        source = args.source

    if not args.out:
        out = source
    else:
        out = args.out


    cfg = load_config(source)
    if cfg:
        if args.env_dirs:
            env_dirs = args.env_dirs.split(',')
            for env_dir in env_dirs:
                os.environ.update(load_env_vars_from_dir(env_dir))
        write_config(replace_envvars(cfg), out)

if __name__ == '__main__':
    main()
