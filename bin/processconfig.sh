#!/bin/bash
set -e -o pipefail

map_csv(){
    CSV="$1"
    ARG="$2"
    OUT=""
    
    if [[ -n "$CSV" ]]; then
        IFS=, read -ra PREFIXES <<< $CSV
        for p in ${PREFIXES[@]}; do OUT="${OUT}${ARG} ${p} "; done
    fi
    echo -n "$OUT"
}

trim_whitespace(){
    echo "$@" | awk '{$1=$1};1'
}

DEFAULT_ENVCONSUL_ARGS="-sanitize -upcase -once"
ENVCONSUL_ARGS=""
[[ -n "$CONSUL_PREFIX" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS $( map_csv $CONSUL_PREFIX '-prefix' )"
[[ -n "$VAULT_PREFIX" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS $( map_csv $VAULT_PREFIX '-secret' )"

ENVCONSUL_ARGS=$(trim_whitespace $ENVCONSUL_ARGS)
echo "XXX${ENVCONSUL_ARGS}XXX" 