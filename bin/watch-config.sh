#!/bin/bash
set -e

program=$0

usage(){
    cat << EOF
Usage: $program [options...] --url <url>
Options:
-u, --url <url>                                     - file to be watched and fetched. file://<file>, s3://<file>, http[s]://<file>
-d,--cache-dir <cache-dir>                          - Cached directory (Default: /tmp/.s3.cache)
--polling-interval <polling-interval-in-seconds>]   - Polling interval in seconds (Default 30)
--first-time-execute                                - Should execute command on first time (Default: false)
--skip-watch                                        - Skip watch the file, only fetch it once (Default: false)
--debug                                             - Log debug (Default: false)

EOF
    exit 1
}

log(){
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1"
}

debug(){
    [[ "$DEBUG" == "YES" ]] && log "$1"
}

_md5(){
    md5sum $1 | cut -f1 -d ' '
}

fetch_config(){
    fetchconfig.py --source "${URL}" --out ${CACHE_DIR}/${FILE_BASENAME}
    PROCESS_COMMAND="processconfig.py --source ${CACHE_DIR}/${FILE_BASENAME} --out ${CACHE_DIR}/${FILE_BASENAME}"
    [[ -n "${ENVVARS_DIRS}" ]] && PROCESS_COMMAND="$PROCESS_COMMAND --env-dirs ${ENVVARS_DIRS}"
    if [[ "$DEBUG" == "YES" ]]; then
        DEBUG=YES envconsul-wrapper.sh $PROCESS_COMMAND
    else
        {
            envconsul-wrapper.sh $PROCESS_COMMAND
        } &>/dev/null
    fi

    debug "Calulating checksum of ${CACHE_DIR}/${FILE_BASENAME}"
    MD5_CHECKSUM=$(_md5 ${CACHE_DIR}/${FILE_BASENAME})
    debug "MD5_CHECKSUM = $MD5_CHECKSUM"
    if [ -f ${CACHE_DIR}/${FILE_BASENAME}.md5 ]; then
        ORIG_MD5_CHECKSUM=$(cat ${CACHE_DIR}/${FILE_BASENAME}.md5)
    else
        ORIG_MD5_CHECKSUM=''
    fi
    debug "ORIG_MD5_CHECKSUM = $ORIG_MD5_CHECKSUM"
    _md5 ${CACHE_DIR}/${FILE_BASENAME} > ${CACHE_DIR}/${FILE_BASENAME}.md5
    RESULT=0
    if [ "$ORIG_MD5_CHECKSUM" != "$MD5_CHECKSUM" ]; then
        cp ${CACHE_DIR}/${FILE_BASENAME} ${FILENAME}
        RESULT=30
    fi
    rm -rf ${CACHE_DIR}/${FILE_BASENAME}
    return $RESULT

}

exec_command(){
    if [ -n "$COMMAND" ]; then
        log "Running $COMMAND."
        $COMMAND
        log "Running $COMMAND. Done..."
    fi
}

parseArgs(){
    FILENAME=$CONFIG_FILE_LOCATION
    COMMAND=update-config.sh

    POSITIONAL=()
    while [[ $# -gt 0 ]]
    do
    key="$1"

    case "$key" in
        -u|--url)
        URL="$2"
        shift # past argument
        shift # past value
        ;;

        -d|--cache-dir)
        CACHE_DIR="$2"
        shift # past argument
        shift # past value
        ;;

        -p|--polling-interval)
        POLLING_INTERVAL="$2"
        shift # past argument
        shift # past value
        ;;

        --skip-watch)
        SKIP_WATCH=YES
        shift # past argument
        ;;

        --first-time-execute)
        FIRST_TIME_EXECUTE=YES
        shift # past argument
        ;;

        -h|--help)
        HELP=YES
        shift # past argument
        ;;

        --debug)
        DEBUG=YES
        shift # past argument
        ;;
        *)    # unknown option
        POSITIONAL+=("$1") # save it in an array for later
        shift # past argument
        ;;
    esac
    done
    set -- "${POSITIONAL[@]}" # restore positional parameters

    if [ -n "$HELP" ]; then
        usage
    fi

    if [[ -z "$URL" ]]; then
        (>&2 echo "Error: --url must be provided")
        usage
    fi
    if [[ -z "$FILENAME" ]]; then
        (>&2 echo "Error: --filename must be provided")
        usage
    fi

    POLLING_INTERVAL=${POLLING_INTERVAL:-30}
    CACHE_DIR=${CACHE_DIR:-/tmp/.file-watch-cache}
    FILE_BASENAME=$(basename $FILENAME)
}

parseArgs "$@"
log "$program started"
log "URL = $URL"
log "URL_TYPE = $URL_TYPE"
log "CACHE_DIR = $CACHE_DIR"
log "POLLING_INTERVAL = $POLLING_INTERVAL"
log "FILENAME = $CONFIG_FILE_LOCATION"
log "COMMAND = $COMMAND"
log "SKIP_WATCH = $SKIP_WATCH"
log "DEBUG = $DEBUG"

mkdir -p $CACHE_DIR

log "Fetching $FILENAME for the first time..."
fetch_config && FETCH_RES=$? || FETCH_RES=$?

if [ "$FIRST_TIME_EXECUTE" == "YES" ]; then
    log "FIRST_TIME_EXECUTE = YES, executing command"
    exec_command
fi

if [ "$SKIP_WATCH" == "YES" ]; then
    log "SKIP_WATCH = YES, Going out..."
    exit 0
fi

log "Entering watch loop"
while true; do
    sleep $POLLING_INTERVAL
    fetch_config && RES=$? || RES=$?
    if [ "$RES" == 30 ]; then
        log "Checksum was changed, executing command"
        exec_command || true
    fi
done