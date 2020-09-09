#!/usr/bin/env python3
from __future__ import print_function
import argparse
import boto3
import botocore
import glob
import os
import requests
import yaml
import io
from shutil import copyfile

try:
    from collections.abc import Mapping
except ImportError:
    from collections import Mapping

def merge_dict(source, target):
    source = source.copy()
    for k, v in target.items():
        if (k in source and isinstance(source[k], Mapping)
                and isinstance(target[k], Mapping)):
            source[k] = merge_dict(source[k], target[k])
        else:
            source[k] = target[k]
    return source

def fetch_http(source):
    configs = []
    res = requests.get(source)
    if res.status_code != 200:
        raise requests.HTTPError("Could not get %s, Response code: %s, Reason: %s" % (source, res.status_code, res.reason))
    configs.append(dict(src=source, contents=res.text))
    return configs


def fetch_s3(source):
    configs = []
    if source.startswith('s3://'):
        source = source.replace('s3://', '')
    s3 = boto3.resource('s3')

    bucket_name = source.split('/')[0]
    s3_key = '/'.join(source.split('/')[1:])
    try:
        outbuff = io.BytesIO()
        s3.Bucket(bucket_name).download_fileobj(s3_key, outbuff)
        data = outbuff.getvalue()
        configs.append(dict(src=source, contents=data))
        outbuff.close()
    except botocore.exceptions.ClientError as e:
        if e.response['Error']['Code'] == "404":
            print("The object %s does not exist in bucket: %s" % (s3_key, bucket_name))
            raise
        else:
            raise
    return configs

def fetch_files(source):
    configs = []
    if source.startswith('file://'):
        source = source.replace('file://', '')
    matched_files = []
    source = os.path.expanduser(source)
    filenames = []
    if '*' in source:
        filenames = sorted(glob.glob(source), key=os.path.abspath)
        matched_files += [f for f in filenames if os.path.isfile(f)]
    elif os.path.isdir(source):
        filenames = [os.path.join(source, f) for f in os.listdir(source)]
        filenames = sorted(filenames, key=os.path.abspath)
        matched_files += [f for f in filenames if os.path.isfile(f)]
    elif os.path.isfile(source):
        matched_files.append(source)

    for src in matched_files:
        with open(src, 'r') as f:
            configs.append(dict(src=src, contents=f.read()))

    return configs

def fetch_merged_config(source):
    sources = source.split(',')
    raw_configs = []
    for src in sources:
        if not src:
            continue
        if src.startswith('file://'):
            raw_configs += fetch_files(src)
        elif src.startswith('s3://'):
            raw_configs += fetch_s3(src)
        elif src.startswith('http://') or src.startswith('https://'):
            raw_configs += fetch_http(src)

    config = {}
    for rc in raw_configs:
        c = yaml.safe_load(rc['contents'])
        if c is None:
            continue
        elif not isinstance(c, Mapping):
            raise ValueError("Invalid Yaml content: %s" % rc['src'])
        config = merge_dict(config, c)
    return config


def main():
    parser = argparse.ArgumentParser(description='Fetch configuration yaml from different data sources and merge them into one config file')
    parser.add_argument('--source', type=str, help='comma delimited source URIs')
    parser.add_argument('--out', type=str, help='Output config file')
    args = parser.parse_args()
    if not args.source:
        exit(parser.print_usage())
    config = fetch_merged_config(args.source)
    out = args.out
    if not out:
        print(yaml.safe_dump(config, default_flow_style=False))
    else:
        out = os.path.expanduser(out)
        parent_dir = os.path.dirname(out)
        if parent_dir and not os.path.isdir(parent_dir):
            os.makedirs(parent_dir)
        with open(out, 'w') as f:
            if config:
                yaml.safe_dump(config, stream=f, default_flow_style=False)

if __name__ == '__main__':
    main()
