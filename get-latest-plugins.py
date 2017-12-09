#! env python
# set -e -o pipefail

# ppp=$(cat plugins.txt| cut -d ':' -f 1 | xargs -I {} bash -c 'echo -n "{},"' | sed 's/\(.*\),$/\1/g')
# cat update-center.json| jq --arg ppp 'docker-plugin,blueocean-web' '.plugins| map(select(.name == ($ppp | split(",")))) []| [.name, .version] | "\(.[0]):\(.[1])"'
# # | xargs -I {} \
# #     bash -c "cat update-center.json| jq '.plugins| map(select(.name == ({}))) []| [.name, .version] | \"\(.[0]):\(.[1])\"'"

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



