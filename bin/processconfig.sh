#!/bin/bash
set -e -o pipefail

map_csv(){
    CSV="$1"
    ARG="$2"
    OUT=""
    
    if [[ -n "$CSV" ]]; then
        IFS=, read -ra PREFIXES <<< $CSV
        for p in ${PREFIXES[@]}; do OUT="${OUT}${ARG}=${p} "; done
    fi
    echo -n "$OUT"
}

trim_whitespace(){
    echo "$@" | awk '{$1=$1};1'
}

# PROGRAM="processconfig.py"
PROGRAM="env"
ENVCONSUL_MAX_RETRIES=${ENVCONSUL_MAX_RETRIES:-5}
DEFAULT_ENVCONSUL_ARGS="-sanitize -upcase -consul-retry-attempts=${ENVCONSUL_MAX_RETRIES} -vault-retry-attempts=${ENVCONSUL_MAX_RETRIES}"
ENVCONSUL_ARGS=""
[[ -n "$CONSUL_PREFIX" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS $( map_csv $CONSUL_PREFIX '-prefix' )"
[[ -n "$VAULT_PREFIX" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS $( map_csv $VAULT_PREFIX '-secret' )"

[[ -n "$ENVCONSUL_ARGS" ]] && ENVCONSUL_ARGS="$DEFAULT_ENVCONSUL_ARGS $ENVCONSUL_ARGS"
ENVCONSUL_ARGS=$(trim_whitespace $ENVCONSUL_ARGS)

if [[ -n "$ENVCONSUL_ARGS" ]]; then
    set -- envconsul "$ENVCONSUL_ARGS -once" $PROGRAM "$@"
else 
    set -- $PROGRAM "$@"
fi

echo "$@"
exec $@