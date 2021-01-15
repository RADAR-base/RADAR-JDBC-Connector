/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.connect.jdbc.dialect;

import java.sql.JDBCType;
import java.sql.Types;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Schema.Type;
import org.apache.kafka.connect.data.Time;
import org.apache.kafka.connect.data.Timestamp;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.confluent.connect.jdbc.util.ColumnDefinition;
import io.confluent.connect.jdbc.util.ColumnId;
import io.confluent.connect.jdbc.util.QuoteMethod;
import io.confluent.connect.jdbc.util.TableDefinition;
import io.confluent.connect.jdbc.util.TableDefinitionBuilder;
import io.confluent.connect.jdbc.util.TableId;

import static org.junit.Assert.assertEquals;

public class PostgreSqlDatabaseDialectTest extends BaseDialectTest<PostgreSqlDatabaseDialect> {

  @Override
  protected PostgreSqlDatabaseDialect createDialect() {
    return new PostgreSqlDatabaseDialect(sourceConfigWithUrl("jdbc:postgresql://something"));
  }

  @Test
  public void shouldMapPrimitiveSchemaTypeToSqlTypes() {
    assertPrimitiveMapping(Type.INT8, "SMALLINT");
    assertPrimitiveMapping(Type.INT16, "SMALLINT");
    assertPrimitiveMapping(Type.INT32, "INT");
    assertPrimitiveMapping(Type.INT64, "BIGINT");
    assertPrimitiveMapping(Type.FLOAT32, "REAL");
    assertPrimitiveMapping(Type.FLOAT64, "DOUBLE PRECISION");
    assertPrimitiveMapping(Type.BOOLEAN, "BOOLEAN");
    assertPrimitiveMapping(Type.BYTES, "BYTEA");
    assertPrimitiveMapping(Type.STRING, "TEXT");
  }

  @Test
  public void shouldMapDecimalSchemaTypeToDecimalSqlType() {
    assertDecimalMapping(0, "DECIMAL");
    assertDecimalMapping(3, "DECIMAL");
    assertDecimalMapping(4, "DECIMAL");
    assertDecimalMapping(5, "DECIMAL");
  }

  @Test
  public void testCustomColumnConverters() {
    assertColumnConverter(Types.OTHER, PostgreSqlDatabaseDialect.JSON_TYPE_NAME, Schema.STRING_SCHEMA, String.class);
    assertColumnConverter(Types.OTHER, PostgreSqlDatabaseDialect.JSONB_TYPE_NAME, Schema.STRING_SCHEMA, String.class);
    assertColumnConverter(Types.OTHER, PostgreSqlDatabaseDialect.UUID_TYPE_NAME, Schema.STRING_SCHEMA, UUID.class);
  }

  @Test
  public void shouldMapDataTypesForAddingColumnToTable() {
    verifyDataTypeMapping("SMALLINT", Schema.INT8_SCHEMA);
    verifyDataTypeMapping("SMALLINT", Schema.INT16_SCHEMA);
    verifyDataTypeMapping("INT", Schema.INT32_SCHEMA);
    verifyDataTypeMapping("BIGINT", Schema.INT64_SCHEMA);
    verifyDataTypeMapping("REAL", Schema.FLOAT32_SCHEMA);
    verifyDataTypeMapping("DOUBLE PRECISION", Schema.FLOAT64_SCHEMA);
    verifyDataTypeMapping("BOOLEAN", Schema.BOOLEAN_SCHEMA);
    verifyDataTypeMapping("TEXT", Schema.STRING_SCHEMA);
    verifyDataTypeMapping("BYTEA", Schema.BYTES_SCHEMA);
    verifyDataTypeMapping("DECIMAL", Decimal.schema(0));
    verifyDataTypeMapping("DATE", Date.SCHEMA);
    verifyDataTypeMapping("TIME", Time.SCHEMA);
    verifyDataTypeMapping("TIMESTAMP", Timestamp.SCHEMA);
  }

  @Test
  public void shouldMapDateSchemaTypeToDateSqlType() {
    assertDateMapping("DATE");
  }

  @Test
  public void shouldMapTimeSchemaTypeToTimeSqlType() {
    assertTimeMapping("TIME");
  }

  @Test
  public void shouldMapTimestampSchemaTypeToTimestampSqlType() {
    assertTimestampMapping("TIMESTAMP");
  }

  @Test
  public void shouldBuildCreateQueryStatement() {
    assertEquals(
        "CREATE TABLE \"myTable\" (\n"
        + "\"c1\" INT NOT NULL,\n"
        + "\"c2\" BIGINT NOT NULL,\n"
        + "\"c3\" TEXT NOT NULL,\n"
        + "\"c4\" TEXT NULL,\n"
        + "\"c5\" DATE DEFAULT '2001-03-15',\n"
        + "\"c6\" TIME DEFAULT '00:00:00.000',\n"
        + "\"c7\" TIMESTAMP DEFAULT '2001-03-15 00:00:00.000',\n"
        + "\"c8\" DECIMAL NULL,\n"
        + "\"c9\" BOOLEAN DEFAULT TRUE,\n"
        + "PRIMARY KEY(\"c1\"))",
        dialect.buildCreateTableStatement(tableId, sinkRecordFields)
    );

    quoteIdentfiiers = QuoteMethod.NEVER;
    dialect = createDialect();

    assertEquals(
        "CREATE TABLE myTable (\n"
        + "c1 INT NOT NULL,\n"
        + "c2 BIGINT NOT NULL,\n"
        + "c3 TEXT NOT NULL,\n"
        + "c4 TEXT NULL,\n"
        + "c5 DATE DEFAULT '2001-03-15',\n"
        + "c6 TIME DEFAULT '00:00:00.000',\n"
        + "c7 TIMESTAMP DEFAULT '2001-03-15 00:00:00.000',\n"
        + "c8 DECIMAL NULL,\n"
        + "c9 BOOLEAN DEFAULT TRUE,\n"
        + "PRIMARY KEY(c1))",
        dialect.buildCreateTableStatement(tableId, sinkRecordFields)
    );
  }

  @Test
  public void shouldBuildAlterTableStatement() {
    assertEquals(
        Arrays.asList(
            "ALTER TABLE \"myTable\" \n"
            + "ADD \"c1\" INT NOT NULL,\n"
            + "ADD \"c2\" BIGINT NOT NULL,\n"
            + "ADD \"c3\" TEXT NOT NULL,\n"
            + "ADD \"c4\" TEXT NULL,\n"
            + "ADD \"c5\" DATE DEFAULT '2001-03-15',\n"
            + "ADD \"c6\" TIME DEFAULT '00:00:00.000',\n"
            + "ADD \"c7\" TIMESTAMP DEFAULT '2001-03-15 00:00:00.000',\n"
            + "ADD \"c8\" DECIMAL NULL,\n"
            + "ADD \"c9\" BOOLEAN DEFAULT TRUE"
        ),
        dialect.buildAlterTable(tableId, sinkRecordFields)
    );

    quoteIdentfiiers = QuoteMethod.NEVER;
    dialect = createDialect();

    assertEquals(
        Arrays.asList(
            "ALTER TABLE myTable \n"
            + "ADD c1 INT NOT NULL,\n"
            + "ADD c2 BIGINT NOT NULL,\n"
            + "ADD c3 TEXT NOT NULL,\n"
            + "ADD c4 TEXT NULL,\n"
            + "ADD c5 DATE DEFAULT '2001-03-15',\n"
            + "ADD c6 TIME DEFAULT '00:00:00.000',\n"
            + "ADD c7 TIMESTAMP DEFAULT '2001-03-15 00:00:00.000',\n"
            + "ADD c8 DECIMAL NULL,\n"
            + "ADD c9 BOOLEAN DEFAULT TRUE"
        ),
        dialect.buildAlterTable(tableId, sinkRecordFields)
    );
  }

  @Test
  public void shouldBuildInsertStatement() {
    TableDefinitionBuilder builder = new TableDefinitionBuilder().withTable("myTable");
    builder.withColumn("id1").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("id2").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("columnA").type("varchar", JDBCType.VARCHAR, String.class);
    builder.withColumn("columnB").type("varchar", JDBCType.VARCHAR, String.class);
    builder.withColumn("columnC").type("varchar", JDBCType.VARCHAR, String.class);
    builder.withColumn("columnD").type("varchar", JDBCType.VARCHAR, String.class);
    TableDefinition tableDefn = builder.build();
    assertEquals(
        "INSERT INTO \"myTable\" (\"id1\",\"id2\",\"columnA\",\"columnB\"," +
        "\"columnC\",\"columnD\") VALUES (?,?,?,?,?,?)",
        dialect.buildInsertStatement(tableId, pkColumns, columnsAtoD, tableDefn)
    );

    quoteIdentfiiers = QuoteMethod.NEVER;
    dialect = createDialect();

    assertEquals(
        "INSERT INTO myTable (id1,id2,columnA,columnB," +
        "columnC,columnD) VALUES (?,?,?,?,?,?)",
        dialect.buildInsertStatement(tableId, pkColumns, columnsAtoD, tableDefn)
    );

    builder = new TableDefinitionBuilder().withTable("myTable");
    builder.withColumn("id1").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("id2").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("columnA").type("varchar", JDBCType.VARCHAR, Integer.class);
    builder.withColumn("uuidColumn").type("uuid", JDBCType.OTHER, UUID.class);
    builder.withColumn("dateColumn").type("date", JDBCType.DATE, java.sql.Date.class);
    tableDefn = builder.build();
    List<ColumnId> nonPkColumns = new ArrayList<>();
    nonPkColumns.add(new ColumnId(tableId, "columnA"));
    nonPkColumns.add(new ColumnId(tableId, "uuidColumn"));
    nonPkColumns.add(new ColumnId(tableId, "dateColumn"));
    assertEquals(
        "INSERT INTO myTable (" +
        "id1,id2,columnA,uuidColumn,dateColumn" +
        ") VALUES (?,?,?,?::uuid,?)",
        dialect.buildInsertStatement(tableId, pkColumns, nonPkColumns, tableDefn)
    );
  }
  @Test
  public void shouldBuildUpsertStatement() {
    TableDefinitionBuilder builder = new TableDefinitionBuilder().withTable("myTable");
    builder.withColumn("id1").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("id2").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("columnA").type("varchar", JDBCType.VARCHAR, String.class);
    builder.withColumn("columnB").type("varchar", JDBCType.VARCHAR, String.class);
    builder.withColumn("columnC").type("varchar", JDBCType.VARCHAR, String.class);
    builder.withColumn("columnD").type("varchar", JDBCType.VARCHAR, String.class);
    TableDefinition tableDefn = builder.build();
    assertEquals(
        "INSERT INTO \"myTable\" (\"id1\",\"id2\",\"columnA\",\"columnB\"," +
        "\"columnC\",\"columnD\") VALUES (?,?,?,?,?,?) ON CONFLICT (\"id1\"," +
        "\"id2\") DO UPDATE SET \"columnA\"=EXCLUDED" +
        ".\"columnA\",\"columnB\"=EXCLUDED.\"columnB\",\"columnC\"=EXCLUDED" +
        ".\"columnC\",\"columnD\"=EXCLUDED.\"columnD\"",
        dialect.buildUpsertQueryStatement(tableId, pkColumns, columnsAtoD, tableDefn)
    );

    quoteIdentfiiers = QuoteMethod.NEVER;
    dialect = createDialect();

    assertEquals(
        "INSERT INTO myTable (id1,id2,columnA,columnB," +
        "columnC,columnD) VALUES (?,?,?,?,?,?) ON CONFLICT (id1," +
        "id2) DO UPDATE SET columnA=EXCLUDED" +
        ".columnA,columnB=EXCLUDED.columnB,columnC=EXCLUDED" +
        ".columnC,columnD=EXCLUDED.columnD",
        dialect.buildUpsertQueryStatement(tableId, pkColumns, columnsAtoD, tableDefn)
    );

    builder = new TableDefinitionBuilder().withTable("myTable");
    builder.withColumn("id1").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("id2").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("columnA").type("varchar", JDBCType.VARCHAR, Integer.class);
    builder.withColumn("uuidColumn").type("uuid", JDBCType.OTHER, UUID.class);
    builder.withColumn("dateColumn").type("date", JDBCType.DATE, java.sql.Date.class);
    tableDefn = builder.build();
    List<ColumnId> nonPkColumns = new ArrayList<>();
    nonPkColumns.add(new ColumnId(tableId, "columnA"));
    nonPkColumns.add(new ColumnId(tableId, "uuidColumn"));
    nonPkColumns.add(new ColumnId(tableId, "dateColumn"));
    assertEquals(
        "INSERT INTO myTable (" +
        "id1,id2,columnA,uuidColumn,dateColumn" +
        ") VALUES (?,?,?,?::uuid,?) ON CONFLICT (id1," +
        "id2) DO UPDATE SET " +
        "columnA=EXCLUDED.columnA," +
        "uuidColumn=EXCLUDED.uuidColumn," +
        "dateColumn=EXCLUDED.dateColumn",
        dialect.buildUpsertQueryStatement(tableId, pkColumns, nonPkColumns, tableDefn)
    );
  }

  @Test
  public void shouldComputeValueTypeCast() {
    TableDefinitionBuilder builder = new TableDefinitionBuilder().withTable("myTable");
    builder.withColumn("id1").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("id2").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("columnA").type("varchar", JDBCType.VARCHAR, Integer.class);
    builder.withColumn("uuidColumn").type("uuid", JDBCType.OTHER, UUID.class);
    builder.withColumn("dateColumn").type("date", JDBCType.DATE, java.sql.Date.class);
    TableDefinition tableDefn = builder.build();
    ColumnId uuidColumn = tableDefn.definitionForColumn("uuidColumn").id();
    ColumnId dateColumn = tableDefn.definitionForColumn("dateColumn").id();
    assertEquals("", dialect.valueTypeCast(tableDefn, columnPK1));
    assertEquals("", dialect.valueTypeCast(tableDefn, columnPK2));
    assertEquals("", dialect.valueTypeCast(tableDefn, columnA));
    assertEquals("::uuid", dialect.valueTypeCast(tableDefn, uuidColumn));
    assertEquals("", dialect.valueTypeCast(tableDefn, dateColumn));
  }

  @Test
  public void createOneColNoPk() {
    verifyCreateOneColNoPk(
        "CREATE TABLE \"myTable\" (" + System.lineSeparator() + "\"col1\" INT NOT NULL)");
  }

  @Test
  public void createOneColOnePk() {
    verifyCreateOneColOnePk(
        "CREATE TABLE \"myTable\" (" + System.lineSeparator() + "\"pk1\" INT NOT NULL," +
        System.lineSeparator() + "PRIMARY KEY(\"pk1\"))");
  }

  @Test
  public void createThreeColTwoPk() {
    verifyCreateThreeColTwoPk(
        "CREATE TABLE \"myTable\" (" + System.lineSeparator() + "\"pk1\" INT NOT NULL," +
        System.lineSeparator() + "\"pk2\" INT NOT NULL," + System.lineSeparator() +
        "\"col1\" INT NOT NULL," + System.lineSeparator() + "PRIMARY KEY(\"pk1\",\"pk2\"))");

    quoteIdentfiiers = QuoteMethod.NEVER;
    dialect = createDialect();

    verifyCreateThreeColTwoPk(
        "CREATE TABLE myTable (" + System.lineSeparator() + "pk1 INT NOT NULL," +
        System.lineSeparator() + "pk2 INT NOT NULL," + System.lineSeparator() +
        "col1 INT NOT NULL," + System.lineSeparator() + "PRIMARY KEY(pk1,pk2))");
  }

  @Test
  public void alterAddOneCol() {
    verifyAlterAddOneCol("ALTER TABLE \"myTable\" ADD \"newcol1\" INT NULL");
  }

  @Test
  public void alterAddTwoCol() {
    verifyAlterAddTwoCols(
        "ALTER TABLE \"myTable\" " + System.lineSeparator() + "ADD \"newcol1\" INT NULL," +
        System.lineSeparator() + "ADD \"newcol2\" INT DEFAULT 42");
  }

  @Test
  public void upsert() {
    TableDefinitionBuilder builder = new TableDefinitionBuilder().withTable("Customer");
    builder.withColumn("id").type("int", JDBCType.INTEGER, Integer.class);
    builder.withColumn("name").type("varchar", JDBCType.VARCHAR, String.class);
    builder.withColumn("salary").type("real", JDBCType.FLOAT, String.class);
    builder.withColumn("address").type("varchar", JDBCType.VARCHAR, String.class);
    TableDefinition tableDefn = builder.build();
    TableId customer = tableDefn.id();
    assertEquals(
        "INSERT INTO \"Customer\" (\"id\",\"name\",\"salary\",\"address\") " +
         "VALUES (?,?,?,?) ON CONFLICT (\"id\") DO UPDATE SET \"name\"=EXCLUDED.\"name\"," +
         "\"salary\"=EXCLUDED.\"salary\",\"address\"=EXCLUDED.\"address\"",
        dialect.buildUpsertQueryStatement(
            customer,
            columns(customer, "id"),
            columns(customer, "name", "salary", "address"),
            tableDefn
        )
    );

    assertEquals(
            "INSERT INTO \"Customer\" (\"id\",\"name\",\"salary\",\"address\") " +
                    "VALUES (?,?,?,?) ON CONFLICT (\"id\",\"name\",\"salary\",\"address\") DO NOTHING",
            dialect.buildUpsertQueryStatement(
                    customer,
                    columns(customer, "id", "name", "salary", "address"),
                    columns(customer),
                    tableDefn
            )
    );

    quoteIdentfiiers = QuoteMethod.NEVER;
    dialect = createDialect();

    assertEquals(
        "INSERT INTO Customer (id,name,salary,address) " +
        "VALUES (?,?,?,?) ON CONFLICT (id) DO UPDATE SET name=EXCLUDED.name," +
        "salary=EXCLUDED.salary,address=EXCLUDED.address",
        dialect.buildUpsertQueryStatement(
            customer,
            columns(customer, "id"),
            columns(customer, "name", "salary", "address"),
            tableDefn
        )
    );

    assertEquals(
            "INSERT INTO Customer (id,name,salary,address) " +
                    "VALUES (?,?,?,?) ON CONFLICT (id,name,salary,address) DO NOTHING",
            dialect.buildUpsertQueryStatement(
                    customer,
                    columns(customer, "id", "name", "salary", "address"),
                    columns(customer),
                    tableDefn
            )
    );
  }

  @Test
  public void shouldSanitizeUrlWithoutCredentialsInProperties() {
    assertSanitizedUrl(
        "jdbc:postgresql://localhost/test?user=fred&ssl=true",
        "jdbc:postgresql://localhost/test?user=fred&ssl=true"
    );
  }

  @Test
  public void shouldSanitizeUrlWithCredentialsInUrlProperties() {
    assertSanitizedUrl(
        "jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true",
        "jdbc:postgresql://localhost/test?user=fred&password=****&ssl=true"
    );
  }
}