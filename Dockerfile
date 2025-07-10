# Copyright 2025 The Hyve
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

ARG BASE_IMAGE=radarbase/kafka-connect-transform-keyvalue:8.0.0

FROM confluentinc/cp-kafka-connect:8.0.0 AS hub

ARG KAFKA_CONNECT_JDBC_VERSION=10.8.3
ARG CONFLUENT_VERSION=7.7.2

RUN mkdir -p /tmp/deps/kafka-connect-jdbc/ /tmp/deps/kafka-connect-avro-converter/
# RUN confluent-hub install --no-prompt --component-dir /tmp/deps/kafka-connect-jdbc/ confluentinc/kafka-connect-jdbc:${KAFKA_CONNECT_JDBC_VERSION}
RUN confluent-hub install --no-prompt --component-dir /tmp/deps/kafka-connect-avro-converter/ confluentinc/kafka-connect-avro-converter:${CONFLUENT_VERSION}

FROM --platform=$BUILDPLATFORM maven:3.8.8-eclipse-temurin-17-focal AS builder

RUN mkdir /code /code/kafka-connect-jdbc
WORKDIR /code/kafka-connect-jdbc

COPY kafka-connect-jdbc /code/kafka-connect-jdbc
RUN mvn package -DskipTests -Dcheckstyle.skip

WORKDIR /code

FROM ${BASE_IMAGE}

USER root

LABEL org.opencontainers.image.authors="pim@thehyve.nl"
LABEL description="RADAR-base version of the JDBC connector for Strimzi Kafka Connect"

ENV CONNECT_PLUGIN_PATH=/opt/kafka/plugins

COPY --from=builder /code/kafka-connect-jdbc/target/components/packages/confluentinc-kafka-connect-jdbc-*/confluentinc-kafka-connect-jdbc-*/ ${CONNECT_PLUGIN_PATH}/kafka-connect-jdbc/
COPY --from=hub /tmp/deps/* ${CONNECT_PLUGIN_PATH}/
RUN ln -s ${CONNECT_PLUGIN_PATH}/confluentinc-kafka-connect-avro-converter/lib/kafka-schema-registry-client*.jar ${CONNECT_PLUGIN_PATH}/kafka-connect-jdbc/lib/kafka-schema-registry-client.jar
# Copy Sentry monitoring .
COPY --from=builder /code/kafka-connect-jdbc/target/components/packages/confluentinc-kafka-connect-jdbc-*/confluentinc-kafka-connect-jdbc-*/lib/sentry-* /opt/kafka/libs/

USER 1001
