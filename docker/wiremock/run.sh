#!/bin/bash

WIREMOCK_URL="http://localhost:8089"
MAPPINGS_DIR="$(dirname "$0")/mappings"

start() {
  docker run -d --name wiremock \
    -p 8089:8080 \
    wiremock/wiremock
  echo "WireMock started at $WIREMOCK_URL"
}

stop() {
  docker stop wiremock && docker rm wiremock
  echo "WireMock stopped"
}

load() {
  MAPPING_FILE=$1
  if [ -z "$MAPPING_FILE" ]; then
    echo "Usage: ./run.sh load <mapping-file>"
    echo "  e.g. ./run.sh load 01_baseline.json"
    exit 1
  fi

  curl -s -X DELETE "$WIREMOCK_URL/__admin/mappings" > /dev/null
  curl -s -X POST "$WIREMOCK_URL/__admin/mappings" \
    -H "Content-Type: application/json" \
    -d @"$MAPPINGS_DIR/$MAPPING_FILE"
  echo "Loaded: $MAPPING_FILE"
}

case $1 in
  start)  start ;;
  stop)   stop ;;
  load)   load $2 ;;
  *)
    echo "Usage: ./run.sh [start|stop|load <mapping-file>]"
    echo ""
    echo "Scenarios:"
    echo "  ./run.sh load 01_baseline.json      # 정상 응답"
    echo "  ./run.sh load 02_scenario_500.json  # 연속 500 오류"
    echo "  ./run.sh load 03_scenario_delay.json # 응답 지연 10초"
    echo "  ./run.sh load 04_scenario_hang.json  # 응답 없음"
    ;;
esac
