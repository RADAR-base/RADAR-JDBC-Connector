# Copyright 2018 The Hyve
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
ARG BASE_IMAGE=radarbase/kafka-connect-transform-keyvalue:7.8.1

FROM --platform=$BUILDPLATFORM maven:3.8.8-eclipse-temurin-17-focal AS builder

# Make kafka-connect-jdbc source folder
RUN mkdir /code /code/kafka-connect-jdbc
WORKDIR /code/kafka-connect-jdbc

# Install maven dependency packages (keep in image)
COPY kafka-connect-jdbc /code/kafka-connect-jdbc
RUN mvn package -DskipTests -Dcheckstyle.skip

WORKDIR /code

FROM ${BASE_IMAGE}

LABEL org.opencontainers.image.authors="@mpgxvii"

LABEL description="Kafka JDBC connector"

ENV CONNECT_PLUGIN_PATH=/usr/share/kafka-connect/plugins

# To isolate the classpath from the plugin path as recommended
COPY --from=builder /code/kafka-connect-jdbc/target/components/packages/confluentinc-kafka-connect-jdbc-*/confluentinc-kafka-connect-jdbc-*/ ${CONNECT_PLUGIN_PATH}/kafka-connect-jdbc/

# Load topics validator
COPY ./docker/kafka-wait /usr/bin/kafka-wait

# Load modified launcher
COPY ./docker/launch /etc/confluent/docker/launch

# Overwrite the log4j configuration to include Sentry monitoring.
COPY ./docker/log4j.properties.template /etc/confluent/docker/log4j.properties.template
# Copy Sentry monitoring jars.
COPY --from=builder /code/kafka-connect-jdbc/target/components/packages/confluentinc-kafka-connect-jdbc-*/confluentinc-kafka-connect-jdbc-*/lib/sentry-* /etc/kafka-connect/jars

USER root
# create parent directory for storing offsets in standalone mode
RUN mkdir -p /var/lib/kafka-connect-jdbc/logs
