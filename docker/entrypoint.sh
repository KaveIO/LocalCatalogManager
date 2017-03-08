#!/bin/bash
set -e

if [ "$1" = 'server' ]; then
    exec lcm-complete/bin/start-server.sh
    exit 0
elif [ "$1" = 'ui' ]; then
    exec lcm-complete/bin/start-ui.sh
    exit 0
fi

exec "$@"

