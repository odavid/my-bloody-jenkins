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

ENVCONSUL_MAX_RETRIES=${ENVCONSUL_MAX_RETRIES:-5}

[[ -n "$ENVCONSUL_CONSUL_PREFIX" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS $( map_csv $ENVCONSUL_CONSUL_PREFIX '-prefix' )"
[[ -n "$ENVCONSUL_VAULT_PREFIX" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS $( map_csv $ENVCONSUL_VAULT_PREFIX '-secret' )"
[[ -n "$ENVCONSUL_UNWRAP_TOKEN" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -vault-unwrap-token"
[[ -n "$ENVCONSUL_ARGS" ]] && [[ -n "$CONSUL_ADDR" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -consul-addr=${CONSUL_ADDR}"
[[ -n "$ENVCONSUL_ARGS" ]] && [[ -n "$VAULT_ADDR" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -vault-addr=${VAULT_ADDR}"
[[ -n "$ENVCONSUL_ARGS" ]] && [[ "$ENVCONSUL_ADDITIONAL_ARGS" ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS $ENVCONSUL_ADDITIONAL_ARGS"
[[ -n "$ENVCONSUL_ARGS" ]] && ! [[ $ENVCONSUL_ARGS =~ '-once' ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -once"
[[ -n "$ENVCONSUL_ARGS" ]] && ! [[ $ENVCONSUL_ARGS =~ '-sanitize' ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -sanitize"
[[ -n "$ENVCONSUL_ARGS" ]] && ! [[ $ENVCONSUL_ARGS =~ '-upcase' ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -upcase"
[[ -n "$ENVCONSUL_ARGS" ]] && ! [[ $ENVCONSUL_ARGS =~ '-vault-retry' ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -vault-retry"
[[ -n "$ENVCONSUL_ARGS" ]] && ! [[ $ENVCONSUL_ARGS =~ '-cosnul-retry' ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -consul-retry"
[[ -n "$ENVCONSUL_ARGS" ]] && ! [[ $ENVCONSUL_ARGS =~ '-consul-retry-attempts' ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -consul-retry-attempts=${ENVCONSUL_MAX_RETRIES}"
[[ -n "$ENVCONSUL_ARGS" ]] && ! [[ $ENVCONSUL_ARGS =~ '-vault-retry-attempts' ]] && ENVCONSUL_ARGS="$ENVCONSUL_ARGS -vault-retry-attempts=${ENVCONSUL_MAX_RETRIES}"

ENVCONSUL_ARGS=$(trim_whitespace $ENVCONSUL_ARGS)

if [[ -n "$ENVCONSUL_ARGS" ]]; then
    set -- envconsul "$ENVCONSUL_ARGS" "$@"
fi

[[ "$DEBUG" == "YES" ]] && echo "$@"
exec $@