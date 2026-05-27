#!/bin/sh

TASK=${1:-}
APP_HOME=$(CDPATH= cd -P "$(dirname "$0")/.." && pwd) || exit 1
FLOWIT_GRADLE_FALLBACK_TASK=$TASK
export FLOWIT_GRADLE_FALLBACK_TASK

exec /bin/sh "$APP_HOME/scripts/local-docker.sh" "$TASK"
