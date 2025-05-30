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
import java.util.function.Function;

/**
 * `Result` is a type that represents either success ([`Ok`]) or failure ([`Err`]).
 */
@SuppressWarnings("hiding")
public final class Result<Ok, Err> {

    public static final int FLOW_CONTROL = 503;

    private final Ok ok;
    private final Err err;

    /**
     * Creates a new `Result` from the given value.
     *
     * @param ok the value
     * @return a new `Result` from the given value
     * @param <Ok> the value type
     * @param <Err> the error type
     */
    public static <Ok, Err> Result<Ok, Err> ok(Ok ok) {
        Ensures.ensureNonNull(ok, "null `ok`");
        return new Result<>(ok, null);
    }

    /**
     * Creates a new `Result` from the given error.
     *
     * @param err the error
     * @return a new `Result` from the given error
     * @param <Ok> the value type
     * @param <Err> the error type
     */
    public static <Ok, Err> Result<Ok, Err> err(Err err) {
        Ensures.ensureNonNull(err, "null `err`");
        return new Result<>(null, err);
    }

    private Result(Ok ok, Err err) {
        this.ok = ok;
        this.err = err;
    }

    /**
     * Checks if the result is [`Ok`].
     *
     * @return {@code true} if the result is [`Ok`]
     */
    public boolean isOk() {
        return this.ok != null && this.err == null;
    }

    /**
     * Gets the [`Ok`] value.
     *
     * @return the [`Ok`] value
     */
    public Ok getOk() {
        return Ensures.ensureNonNull(this.ok, "null `ok`");
    }

    /**
     * Gets the [`Err`].
     *
     * @return the [`Err`]
     */
    public Err getErr() {
        return Ensures.ensureNonNull(this.err, "null `err`");
    }

    /**
     * Maps a {@code Result<Ok, Err>} to {@code Result<U, Err>} by applying a function to
     * a contained [`Ok`] value, leaving an [`Err`] value untouched.
     * <p>
     * This function can be used to compose the results of two functions.
     *
     * @param mapper a function to a contained [`Ok`] value
     * @param <U> the [`Ok`] value type to map to
     * @return a new `Result` by applying the mapper function to the [`Ok`] value,
     *         or a `Result` containing the original error if this result is an [`Err`]
     */
    public <U> Result<U, Err> map(Function<Ok, U> mapper) {
        return isOk() ? Result.ok(mapper.apply(getOk())) : Result.err(getErr());
    }

    /**
     * Returns the provided default (if [`Err`]), or applies a function to
     * the contained value (if [`Ok`]).
     * <p>
     * Arguments passed to `mapOr` are eagerly evaluated; if you are passing
     * the result of a function call, it is recommended to use
     * {@link #mapOrElse(Function, Function)}, which is lazily evaluated.
     *
     * @param defaultVal default value (if [`Err`])
     * @param mapper a function to a contained [`Ok`] value
     * @param <U> the value type to map to
     * @return the provided default (if [`Err`]), or applies a function to
     * the contained value (if [`Ok`])
     */
    public <U> U mapOr(U defaultVal, Function<Ok, U> mapper) {
        return isOk() ? mapper.apply(getOk()) : defaultVal;
    }

    /**
     * Maps a {@code Result<OK, Err>} to {@code U} by applying a fallback function to a
     * contained [`Err`] value, or a default function to a contained [`Ok`]
     * value.
     * <p>
     * This function can be used to unpack a successful result while
     * handling an error.
     *
     * @param fallbackMapper a fallback function to a contained [`Err`] value
     * @param mapper a function to a contained [`Ok`] value
     * @param <U> the value type to map to
     * @return {@code U} by applying a fallback function to a contained [`Err`] value,
     * or a default function to a contained [`Ok`] value.
     */
    public <U> U mapOrElse(Function<Err, U> fallbackMapper, Function<Ok, U> mapper) {
        return isOk() ? mapper.apply(getOk()) : fallbackMapper.apply(getErr());
    }

    /**
     * Maps a {@code Result<Ok, Err>} to {@code Result<Ok, F>} by applying a function to a
     * contained [`Err`] value, leaving an [`Ok`] value untouched.
     *
     * @param mapper a function to a contained [`Err`] value
     * @param <F> the error type to map to
     * @return a new `Result` by applying the mapper function to the [`Err`] value,
     *         or a `Result` containing the original [`Ok`] value if this result is an [`Ok`]
     */
    public <F> Result<Ok, F> mapErr(Function<Err, F> mapper) {
        return isOk() ? Result.ok(getOk()) : Result.err(mapper.apply(getErr()));
    }

    /**
     * Calls `mapper` if the result is [`Ok`], otherwise returns the [`Err`] value.
     *
     * @param mapper a function to a contained [`Ok`] value
     * @param <U> the value type witch mapped to
     * @return a new `Result` by applying the mapper function to the [`Ok`] value,
     *         or a `Result` containing the original [`Err`] value if this result is an [`Err`]
     */
    public <U> Result<U, Err> andThen(Function<Ok, Result<U, Err>> mapper) {
        return isOk() ? mapper.apply(getOk()) : Result.err(getErr());
    }

    /**
     * Calls `mapper` if the result is [`Err`], otherwise returns the [`Ok`] value.
     *
     * @param mapper a function to a contained [`Err`] value
     * @param <F> the error type to map to
     * @return a new `Result` by applying the mapper function to the [`Err`] value,
     *         or a `Result` containing the original [`Ok`] value if this result is an [`Ok`]
     */
    public <F> Result<Ok, F> orElse(Function<Err, Result<Ok, F>> mapper) {
        return isOk() ? Result.ok(getOk()) : mapper.apply(getErr());
    }

    /**
     * Returns the contained [`Ok`] value or a provided default.
     *
     * @param defaultVal a provided default value
     * @return the contained [`Ok`] value or a provided default
     */
    public Ok unwrapOr(Ok defaultVal) {
        return isOk() ? getOk() : defaultVal;
    }

    /**
     * Returns the contained [`Ok`] value or computes it from a function.
     *
     * @param mapper computes function
     * @return the contained [`Ok`] value or computes it from a function
     */
    public Ok unwrapOrElse(Function<Err, Ok> mapper) {
        return isOk() ? getOk() : mapper.apply(getErr());
    }

    @Override
    public String toString() {
        return "Result{" + "ok=" + ok + ", err=" + err + '}';
    }
}
