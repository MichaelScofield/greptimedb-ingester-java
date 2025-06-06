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

package io.greptime.metrics;

import io.greptime.common.Copiable;
import io.greptime.common.Endpoint;

/**
 * Exporter options.
 */
public class ExporterOptions implements Copiable<ExporterOptions> {
    private Endpoint bindAddr;
    private boolean deamon;

    public static ExporterOptions newDefault() {
        ExporterOptions opts = new ExporterOptions();
        opts.bindAddr = new Endpoint("0.0.0.0", 8090);
        opts.deamon = true;
        return opts;
    }

    public Endpoint getBindAddr() {
        return bindAddr;
    }

    public void setBindAddr(Endpoint bindAddr) {
        this.bindAddr = bindAddr;
    }

    public boolean isDeamon() {
        return deamon;
    }

    public void setDeamon(boolean deamon) {
        this.deamon = deamon;
    }

    @Override
    public ExporterOptions copy() {
        ExporterOptions opts = new ExporterOptions();
        opts.bindAddr = this.bindAddr;
        opts.deamon = this.deamon;
        return opts;
    }

    @Override
    public String toString() {
        return "ExporterOptions{" + "bindAddr=" + bindAddr + ", deamon=" + deamon + '}';
    }
}
