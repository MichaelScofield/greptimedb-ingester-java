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

import io.greptime.common.Into;
import io.greptime.v1.Common;

/**
 * GreptimeDB's data type.
 */
public enum DataType {
    Bool,
    Int8,
    Int16,
    Int32,
    Int64,
    UInt8,
    UInt16,
    UInt32,
    UInt64,
    Float32,
    Float64,
    Binary,
    String,
    Date,
    @Deprecated
    DateTime,
    TimestampSecond,
    TimestampMillisecond,
    TimestampMicrosecond,
    TimestampNanosecond,
    TimeSecond,
    TimeMilliSecond,
    TimeMicroSecond,
    TimeNanoSecond,
    Decimal128,
    Json,
    ;

    public boolean isTimestamp() {
        return this == TimestampSecond
                || this == TimestampMillisecond
                || this == TimestampMicrosecond
                || this == TimestampNanosecond;
    }

    public Common.ColumnDataType toProtoValue() {
        switch (this) {
            case Bool:
                return Common.ColumnDataType.BOOLEAN;
            case Int8:
                return Common.ColumnDataType.INT8;
            case Int16:
                return Common.ColumnDataType.INT16;
            case Int32:
                return Common.ColumnDataType.INT32;
            case Int64:
                return Common.ColumnDataType.INT64;
            case UInt8:
                return Common.ColumnDataType.UINT8;
            case UInt16:
                return Common.ColumnDataType.UINT16;
            case UInt32:
                return Common.ColumnDataType.UINT32;
            case UInt64:
                return Common.ColumnDataType.UINT64;
            case Float32:
                return Common.ColumnDataType.FLOAT32;
            case Float64:
                return Common.ColumnDataType.FLOAT64;
            case Binary:
                return Common.ColumnDataType.BINARY;
            case String:
                return Common.ColumnDataType.STRING;
            case Date:
                return Common.ColumnDataType.DATE;
            case TimestampSecond:
                return Common.ColumnDataType.TIMESTAMP_SECOND;
            case TimestampMillisecond:
                return Common.ColumnDataType.TIMESTAMP_MILLISECOND;
            case DateTime:
                // DateTime is an alias of TIMESTAMP_MICROSECOND
                // https://github.com/GreptimeTeam/greptimedb/issues/5489
            case TimestampMicrosecond:
                return Common.ColumnDataType.TIMESTAMP_MICROSECOND;
            case TimestampNanosecond:
                return Common.ColumnDataType.TIMESTAMP_NANOSECOND;
            case TimeSecond:
                return Common.ColumnDataType.TIME_SECOND;
            case TimeMilliSecond:
                return Common.ColumnDataType.TIME_MILLISECOND;
            case TimeMicroSecond:
                return Common.ColumnDataType.TIME_MICROSECOND;
            case TimeNanoSecond:
                return Common.ColumnDataType.TIME_NANOSECOND;
            case Decimal128:
                return Common.ColumnDataType.DECIMAL128;
            case Json:
                return Common.ColumnDataType.JSON;
            default:
                return null;
        }
    }

    public static class DecimalTypeExtension implements Into<Common.DecimalTypeExtension> {
        // The maximum precision for [Decimal128] values
        static final int MAX_DECIMAL128_PRECISION = 38;

        // The default scale for [Decimal128] values
        static final int DEFAULT_DECIMAL128_SCALE = 10;

        public static final DecimalTypeExtension DEFAULT =
                new DecimalTypeExtension(MAX_DECIMAL128_PRECISION, DEFAULT_DECIMAL128_SCALE);

        private final int precision;
        private final int scale;

        public DecimalTypeExtension(int precision, int scale) {
            this.precision = precision;
            this.scale = scale;
        }

        @Override
        public Common.DecimalTypeExtension into() {
            return Common.DecimalTypeExtension.newBuilder()
                    .setPrecision(this.precision)
                    .setScale(this.scale)
                    .build();
        }
    }
}
