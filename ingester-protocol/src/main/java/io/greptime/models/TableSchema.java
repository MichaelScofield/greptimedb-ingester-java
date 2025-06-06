/*
 * Copyright 2023 Greptime Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.greptime.models;

import io.greptime.common.util.Ensures;
import io.greptime.v1.Common;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the schema structure for writing data to the database.
 * <p>
 * For optimal performance, it is recommended to cache and reuse the same {@code TableSchema} instance
 * when writing data multiple times. The caching responsibility is delegated to the user since
 * the {@code Ingester} client aims to minimize memory overhead by avoiding cache management.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * TableSchema schema = TableSchema.newBuilder("my_table")
 *     .addTag("tag1", DataType.String)
 *     .addTimestamp("ts", DataType.TimestampMillisecond)
 *     .addField("field1", DataType.Float64)
 *     .build();
 *
 * Table table = Table.from(schema);
 * // The order of the values must match the schema definition.
 * table.addRow(tag_value_1, now, field_value_1);
 * table.addRow(tag_value_2, now, field_value_2);
 * table.complete();
 * }</pre>
 */
public class TableSchema {

    private String tableName;
    private List<String> columnNames;
    private List<Common.SemanticType> semanticTypes;
    private List<Common.ColumnDataType> dataTypes;
    private List<Common.ColumnDataTypeExtension> dataTypeExtensions;

    private TableSchema() {}

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<Common.SemanticType> getSemanticTypes() {
        return semanticTypes;
    }

    public List<Common.ColumnDataType> getDataTypes() {
        return dataTypes;
    }

    public List<Common.ColumnDataTypeExtension> getDataTypeExtensions() {
        return dataTypeExtensions;
    }

    public static Builder newBuilder(String tableName) {
        return new Builder(tableName);
    }

    public static class Builder {
        private final String tableName;
        private final List<String> columnNames = new ArrayList<>();
        private final List<Common.SemanticType> semanticTypes = new ArrayList<>();
        private final List<Common.ColumnDataType> dataTypes = new ArrayList<>();
        private final List<Common.ColumnDataTypeExtension> dataTypeExtensions = new ArrayList<>();

        public Builder(String tableName) {
            this.tableName = tableName;
        }

        /**
         * Add tag schema.
         * <p>
         * It is strongly recommended to use snake case naming convention and avoid
         * using camel case. This is because GreptimeDB treats column names as
         * case-insensitive, which can cause confusion when querying with camel case.
         *
         * @param name the name of this tag
         * @param dataType the data type of this tag
         * @return this builder
         */
        public Builder addTag(String name, DataType dataType) {
            return addColumn(name, SemanticType.Tag, dataType);
        }

        /**
         * Add timestamp schema.
         * <p>
         * It is strongly recommended to use snake case naming convention and avoid
         * using camel case. This is because GreptimeDB treats column names as
         * case-insensitive, which can cause confusion when querying with camel case.
         *
         * @param name the name of this timestamp
         * @param dataType the data type of this timestamp
         * @return this builder
         */
        public Builder addTimestamp(String name, DataType dataType) {
            Ensures.ensure(
                    dataType.isTimestamp(),
                    "Invalid timestamp data type: %s, only support `DataType.TimestampXXX`",
                    dataType);
            return addColumn(name, SemanticType.Timestamp, dataType);
        }

        /**
         * Add field schema.
         * <p>
         * It is strongly recommended to use snake case naming convention and avoid
         * using camel case. This is because GreptimeDB treats column names as
         * case-insensitive, which can cause confusion when querying with camel case.
         *
         * @param name the name of this field
         * @param dataType the data type of this field
         * @return this builder
         */
        public Builder addField(String name, DataType dataType) {
            return addColumn(name, SemanticType.Field, dataType);
        }

        /**
         * Add column schema.
         * <p>
         * It is strongly recommended to use snake case naming convention and avoid
         * using camel case. This is because GreptimeDB treats column names as
         * case-insensitive, which can cause confusion when querying with camel case.
         *
         * @param name the name of this column
         * @param semanticType the semantic type of this column (`Tag`, `Field` or `Timestamp`)
         * @param dataType the data type of this column
         * @return this builder
         */
        public Builder addColumn(String name, SemanticType semanticType, DataType dataType) {
            return addColumn(name, semanticType, dataType, null);
        }

        /**
         * Add column schema.
         * <p>
         * It is strongly recommended to use snake case naming convention and avoid
         * using camel case. This is because GreptimeDB treats column names as
         * case-insensitive, which can cause confusion when querying with camel case.
         *
         * @param name the name of this column
         * @param semanticType the semantic type of this column (`Tag`, `Field` or `Timestamp`)
         * @param dataType the data type of this column
         * @param decimalTypeExtension the decimal type extension of this column(only for `DataType.Decimal128`)
         * @return this builder
         */
        public Builder addColumn(
                String name,
                SemanticType semanticType,
                DataType dataType,
                DataType.DecimalTypeExtension decimalTypeExtension) {
            Ensures.ensureNonNull(name, "Null column name");
            Ensures.ensureNonNull(semanticType, "Null semantic type");
            Ensures.ensureNonNull(dataType, "Null data type");

            if (semanticType == SemanticType.Timestamp) {
                Ensures.ensure(
                        dataType.isTimestamp(),
                        "Invalid timestamp data type: %s, only support `DataType.TimestampXXX`",
                        dataType);
            }

            // Trim leading and trailing spaces
            name = name.trim();

            this.columnNames.add(name);
            this.semanticTypes.add(semanticType.toProtoValue());
            this.dataTypes.add(dataType.toProtoValue());

            if (dataType == DataType.Json) {
                Common.ColumnDataTypeExtension ext = Common.ColumnDataTypeExtension.newBuilder()
                        .setJsonType(Common.JsonTypeExtension.JSON_BINARY)
                        .build();
                this.dataTypeExtensions.add(ext);
            } else if (dataType == DataType.Decimal128) {
                if (decimalTypeExtension == null) {
                    decimalTypeExtension = DataType.DecimalTypeExtension.DEFAULT;
                }
                Common.ColumnDataTypeExtension ext = Common.ColumnDataTypeExtension.newBuilder()
                        .setDecimalType(decimalTypeExtension.into())
                        .build();
                this.dataTypeExtensions.add(ext);
            } else {
                Ensures.ensure(decimalTypeExtension == null, "Only decimal type can have decimal type extension");
                this.dataTypeExtensions.add(null);
            }
            return this;
        }

        /**
         * Build the table schema.
         *
         * @return the table schema
         */
        public TableSchema build() {
            Ensures.ensureNonNull(this.tableName, "Null table name");
            Ensures.ensureNonNull(this.columnNames, "Null column names");
            Ensures.ensureNonNull(this.semanticTypes, "Null semantic types");
            Ensures.ensureNonNull(this.dataTypes, "Null data types");

            int columnCount = this.columnNames.size();

            Ensures.ensure(columnCount > 0, "Empty column names");
            Ensures.ensure(
                    columnCount == this.semanticTypes.size(), "Column names size not equal to semantic types size");
            Ensures.ensure(columnCount == this.dataTypes.size(), "Column names size not equal to data types size");
            Ensures.ensure(
                    columnCount == this.dataTypeExtensions.size(),
                    "Column names size not equal to data type extensions size");

            TableSchema tableSchema = new TableSchema();
            tableSchema.tableName = this.tableName;
            tableSchema.columnNames = this.columnNames;
            tableSchema.semanticTypes = this.semanticTypes;
            tableSchema.dataTypes = this.dataTypes;
            tableSchema.dataTypeExtensions = this.dataTypeExtensions;
            return tableSchema;
        }
    }
}
