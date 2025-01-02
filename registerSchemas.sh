#!/bin/bash

SCHEMA_REGISTRY_URL="http://localhost:8081"

register_schema() {
  SCHEMA_NAME=$1
  SCHEMA_FILE=$2
  echo "Registering schema $SCHEMA_NAME..."

  SCHEMA_CONTENT=$(<"$SCHEMA_FILE")

  PAYLOAD=$(cat <<EOF
{
  "schema": "$SCHEMA_CONTENT"
}
EOF
)

  RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" --data "$PAYLOAD" "$SCHEMA_REGISTRY_URL/subjects/$SCHEMA_NAME/versions")

  if [[ $? -eq 0 ]]; then
    echo "Schema $SCHEMA_NAME registered successfully."
  else
    echo "Error registering schema $SCHEMA_NAME."
  fi
}

register_schema "postKey" "avro/postKey.avsc"
register_schema "postValue" "avro/postValue.avsc"

register_schema "commentKey" "avro/commentKey.avsc"
register_schema "commentValue" "avro/commentValue.avsc"
