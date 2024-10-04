# RADAR TimescaleDB Sink Connector and JDBC Connector

This project is based on Confluent's Kafka JDBC connector with additional functionalities, namely:

1. Support for `TimescaleDB` databases
2. Support for multiple `createTable` statements.
3. Support for schema creation and setting of schema name format in the connector config.
4. Support for `TIMESTAMPTZ` data type in `PostgreSQL` databases.

## Connect Single Message Transform

This project depends on a transform plugin that transforms the Kafka record before it is written to the database.
See [RADAR-base
/
kafka-connect-transform-keyvalue](https://github.com/RADAR-base/kafka-connect-transform-keyvalue) for more information.

If you're using Docker, the transform plugin image is included in the Dockerfile. If you're installing manually, the `kafka-connect-transform-keyvalue` plugin must be installed to your Confluent plugin path.

## TimescaleDB Sink Connector

### Installation

This repository relies on a recent version of docker and docker-compose as well as an installation
of Java 8 or later.

### Usage

Copy `docker/sink-timescale.properties.template` to `docker/sink-timescale.properties` and enter your database connection URL, username, and password.

Now you can run a full Kafka stack using

```shell
docker-compose up -d --build
```

## Contributing

Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html).
If you want to contribute a feature or fix browse our [issues](https://github.com/RADAR-base/RADAR-REST-Connector/issues), and please make a pull request.


## Sentry monitoring

To enable Sentry monitoring for the JDBC connector, follow these steps:

1. Set a `SENTRY_DSN` environment variable that points to the desired Sentry DSN.
2. (Optional) Set the `SENTRY_LOG_LEVEL` environment variable to control the minimum log level of
   events sent to Sentry.
   The default log level for Sentry is `WARN`. Possible values are `TRACE`, `DEBUG`, `INFO`, `WARN`,
   and `ERROR`.

For further configuration of Sentry via environmental variables see [here](https://docs.sentry.io/platforms/java/configuration/#configuration-via-the-runtime-environment). For instance:

```
SENTRY_LOG_LEVEL: 'ERROR'
SENTRY_DSN: 'https://000000000000.ingest.de.sentry.io/000000000000'
SENTRY_ATTACHSTACKTRACE: true
SENTRY_STACKTRACE_APP_PACKAGES: io.confluent.connect.jdbc
```