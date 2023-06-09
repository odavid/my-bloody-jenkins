import requests
import json
import re

update_site_result = requests.get('https://updates.jenkins.io/stable/update-center.json').text
update_site_result = re.sub(r'updateCenter\.post\(\n', '', update_site_result)
update_site_result = re.sub(r'\n\);', '', update_site_result)
update_site_plugins = json.loads(update_site_result)['plugins']

with open('.ignored-update-plugins', 'r') as f:
    ignored_plugins = f.read().split('\n')

with open('plugins.txt', 'r') as f:
    current_plugins = f.read().split('\n')

with open('plugins.txt', 'w') as f:
    for p in current_plugins:
        if p:
            (plugin_id, current_version) = p.split(':')
            latest = {k:v for k,v in update_site_plugins.items() if v['name'] == plugin_id}
            if latest:
                version = latest.get(plugin_id).get('version') if not plugin_id in ignored_plugins else current_version
            else:
                version = current_version
            f.write("%s:%s\n" % (plugin_id, version))



