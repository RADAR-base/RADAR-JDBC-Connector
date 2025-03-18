# RADAR TimescaleDB Sink Connector and JDBC Connector

This project is based on Confluent's Kafka JDBC connector with additional functionalities, namely:

1. Support for `TimescaleDB` databases
2. Support for multiple `createTable` statements.
3. Support for schema creation and setting of schema name format in the connector config.
4. Support for `TIMESTAMPTZ` data type in `PostgreSQL` databases.
5. Add a custom JDBC Credential Provider to support receiving database username and password from environmental
   variables.
6. Sentry monitoring for the JDBC connector.

## Connect Single Message Transform

This project depends on a transform plugin that transforms the Kafka record before it is written to the database.
See [RADAR-base
/
kafka-connect-transform-keyvalue](https://github.com/RADAR-base/kafka-connect-transform-keyvalue) for more information.

If you're using Docker, the transform plugin image is included in the Dockerfile. If you're installing manually, the
`kafka-connect-transform-keyvalue` plugin must be installed to your Confluent plugin path.

## TimescaleDB Sink Connector

### Installation

This repository relies on a recent version of docker and docker-compose as well as an installation
of Java 8 or later.

### Usage

Copy `docker/sink-timescale.properties.template` to `docker/sink-timescale.properties` and enter your database
connection URL, username, and password.

To localy deploy a full Kafka stack (for development and testing) you can now run:

```shell
docker-compose up -d --build
```

#### Database credentials from environmental variables

In order to receive database credentials from environmental variables, set the `CONNECT_JDBC_CONNECTION_USER` and
`CONNECT_JDBC_CONNECTION_PASSWORD` environmental variables with values for the database username and password
respectively. In addition, set the `jdbc.credentials.provider.class` property in the `docker/sink-timescale.properties` file to
`io.confluent.connect.jdbc.util.EnvVarsJdbcCredentialsProvider` like so:

```shell
jdbc.credentials.provider.class = io.confluent.connect.jdbc.util.EnvVarsJdbcCredentialsProvider
```

## Contributing

Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html).
If you want to contribute a feature or fix browse
our [issues](https://github.com/RADAR-base/RADAR-REST-Connector/issues), and please make a pull request.

## Upgrading

This repository is a fork of Confluent's JDBC connector. This repository is present as a _git subtree_ directory
`kafka-jdbc-connector`.
To upgrade to a newer version of the JDBC connector, follow these steps:

1. Set subtree pull strategy to _rebase_:

```shell
git config pull.rebase true
```

2. Add the Confluent repository as a remote:

```shell
git remote add upstream git@github.com:confluentinc/kafka-connect-jdbc.git
```

3. Pull the latest changes from the Confluent repository. For instance to pull the latest changes from the `10.8.x` tag:

```shell
 git pull -s subtree upstream 10.8.x
```

4. Resolve any conflicts that may arise.

## Sentry monitoring

To enable Sentry monitoring for the JDBC connector, follow these steps:

1. Set a `SENTRY_DSN` environment variable that points to the desired Sentry DSN.
2. (Optional) Set the `SENTRY_LOG_LEVEL` environment variable to control the minimum log level of
   events sent to Sentry.
   The default log level for Sentry is `WARN`. Possible values are `TRACE`, `DEBUG`, `INFO`, `WARN`,
   and `ERROR`.

For further configuration of Sentry via environmental variables
see [here](https://docs.sentry.io/platforms/java/configuration/#configuration-via-the-runtime-environment). For
instance:

```
SENTRY_LOG_LEVEL: 'ERROR'
SENTRY_DSN: 'https://000000000000.ingest.de.sentry.io/000000000000'
SENTRY_ATTACHSTACKTRACE: true
SENTRY_STACKTRACE_APP_PACKAGES: io.confluent.connect.jdbc
```