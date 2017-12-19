#! env python
import requests
import json
import re

update_site_result = requests.get('https://updates.jenkins.io/current/update-center.json').text
update_site_result = re.sub(r'updateCenter\.post\(\n', '', update_site_result)
update_site_result = re.sub(r'\n\);', '', update_site_result)
update_site_plugins = json.loads(update_site_result)['plugins']

with open('plugins.txt', 'r') as f:
    current_plugins = f.read().split('\n')

with open('plugins.txt', 'w') as f:
    for p in current_plugins:
        if p:
            plugin_id = p.split(':')[0]
            latest = {k:v for k,v in update_site_plugins.items() if v['name'] == plugin_id}
            f.write("%s:%s\n" % (plugin_id, latest.get(plugin_id).get('version')))



