#!/bin/bash
set -e

program=$0

usage(){
    cat << EOF
Usage: $program [options...] --url <url> --file-name <destination-file>
Options:
-u, --url <url>                                     - file to be watched and fetched. file://<file>, s3://<file>, http[s]://<file>
-f, --filename <destination-file>                  - Absolute destination filename
-d,--cache-dir <cache-dir>                          - Cached directory (Default: /tmp/.s3.cache)
--polling-interval <polling-interval-in-seconds>]   - Polling interval in seconds (Default 30)
--command <command-to-execute-on-change>            - Command to be executed if the file was changed
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

fetch_config(){
    case "$URL_TYPE" in
        file)
            fetch_config_file
            ;;
        s3)
            fetch_config_s3
            ;;
        http)
            fetch_config_http
            ;;
    esac
    debug "Calulating checksum of ${CACHE_DIR}/${FILE_BASENAME}"
    MD5_CHECKSUM=$(md5 -q ${CACHE_DIR}/${FILE_BASENAME})
    debug "MD5_CHECKSUM = $MD5_CHECKSUM"
    if [ -f ${CACHE_DIR}/${FILE_BASENAME}.md5 ]; then
        ORIG_MD5_CHECKSUM=$(cat ${CACHE_DIR}/${FILE_BASENAME}.md5)
    else
        ORIG_MD5_CHECKSUM=''
    fi
    debug "ORIG_MD5_CHECKSUM = $ORIG_MD5_CHECKSUM"
    md5 -q ${CACHE_DIR}/${FILE_BASENAME} > ${CACHE_DIR}/${FILE_BASENAME}.md5
    RESULT=0
    if [ "$ORIG_MD5_CHECKSUM" != "$MD5_CHECKSUM" ]; then
        cp ${CACHE_DIR}/${FILE_BASENAME} ${FILENAME}
        RESULT=30
    fi
    rm -rf ${CACHE_DIR}/${FILE_BASENAME}
    return $RESULT
        
}

fetch_config_file(){
    debug "Fetching ${SOURCE_FILE} to ${CACHE_DIR}"
    cp "${SOURCE_FILE}" "${CACHE_DIR}/${FILE_BASENAME}"
}

fetch_config_http(){
    debug "Fetching ${URL} to ${CACHE_DIR}"
    curl -SsLo ${CACHE_DIR}/${FILE_BASENAME} $URL
}

fetch_config_s3(){
    if [ -z "$S3_BUCKET_LOCATION" ]; then
        S3_BUCKET_LOCATION="$(aws s3api get-bucket-location --bucket ${S3_BUCKET} --output text)"
        [[ "$S3_BUCKET_LOCATION" == 'None' ]] && S3_BUCKET_LOCATION='us-east-1'
    fi    
    debug "Fetching ${URL} to ${CACHE_DIR}"
    aws --region=${S3_BUCKET_LOCATION} s3 cp ${URL} ${CACHE_DIR}/${FILE_BASENAME} --quiet
}

exec_command(){
    if [ -n "$COMMAND" ]; then
        log "Running $COMMAND."
        $COMMAND
        log "Running $COMMAND. Done..."
    fi
}

parseArgs(){
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

        -f|--filename)
        FILENAME="$2"
        shift # past argument
        shift # past value
        ;;

        -c|--command)
        COMMAND="$2"
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

    URL_TYPE=""
    if [[ $URL == file://* ]] ; then
        URL_TYPE="file"
        SOURCE_FILE="$(echo $URL | sed 's/file:\/\///g')"
    elif [[ $URL == s3://* ]] ; then
        URL_TYPE="s3"
        S3_BUCKET=$(echo $URL | sed 's/s3:\/\///g' | cut -d'/' -f 1)
    elif [[ "$URL" == http://* ]] ; then
        URL_TYPE="http"
    elif [[ "$URL" == https://* ]] ; then
        URL_TYPE="http"
    else
        (>&2 echo "Error: invalid url: $URL")
        usage
    fi

    FILE_BASENAME=$(basename $FILENAME)
}

parseArgs "$@"
log "$program started"
log "URL = $URL"
log "URL_TYPE = $URL_TYPE"
log "CACHE_DIR = $CACHE_DIR"
log "POLLING_INTERVAL = $POLLING_INTERVAL"
log "FILENAME = $FILENAME"
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