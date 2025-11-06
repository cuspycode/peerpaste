#!/bin/sh

JAVA=java
export APP_ROOT="$(cd $(dirname "$0")/../..; pwd)"
"$JAVA" -jar "${APP_ROOT}/Contents/Java/peerpaste.jar"
