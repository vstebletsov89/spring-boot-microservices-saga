#!/bin/bash

SERVICE_NAME="payment-service"
IMAGE_NAME="localhost:5000/$SERVICE_NAME"
DOCKER_CONTEXT="../$SERVICE_NAME"
HELM_CHART_DIR="./$SERVICE_NAME/chart"
VALUES_FILE="./$SERVICE_NAME/values.yaml"
NAMESPACE="microservices"

# build and push
docker build --no-cache -t $SERVICE_NAME $DOCKER_CONTEXT
docker tag $SERVICE_NAME $IMAGE_NAME
docker push $IMAGE_NAME

# create helm chart if not exist
if [ ! -d "$HELM_CHART_DIR" ]; then
  helm create $HELM_CHART_DIR
fi

# template and check
helm template $SERVICE_NAME --values $VALUES_FILE $HELM_CHART_DIR --debug
helm lint $HELM_CHART_DIR --values $VALUES_FILE --debug

# deploy
helm upgrade --install --namespace $NAMESPACE --create-namespace \
  $SERVICE_NAME --values $VALUES_FILE $HELM_CHART_DIR --debug
