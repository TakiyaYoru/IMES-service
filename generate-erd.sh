#!/bin/bash
docker run --rm --network host \
  -v "$PWD/erd-output:/output" \
  schemaspy/schemaspy:latest \
  -t pgsql \
  -host localhost:5433 \
  -db imes_db \
  -u imes_user \
  -p imes_password \
  -s public
echo "✅ ERD generated in ./erd-output/diagrams/"
