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

import com.codahale.metrics.MetricRegistry;
import io.greptime.common.Endpoint;
import io.greptime.common.Lifecycle;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsExporter implements Lifecycle<ExporterOptions> {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsExporter.class);

    private final CollectorRegistry prometheusMetricRegistry;

    private HTTPServer server;
    private ExporterOptions opts;

    private final AtomicBoolean started = new AtomicBoolean(false);

    public MetricsExporter(MetricRegistry dropwizardMetricRegistry) {
        this.prometheusMetricRegistry = new CollectorRegistry();
        this.prometheusMetricRegistry.register(new DropwizardExports(dropwizardMetricRegistry));
    }

    @Override
    public boolean init(ExporterOptions opts) {
        if (this.started.compareAndSet(false, true)) {
            this.opts = opts;
            try {
                Endpoint bindAddr = opts.getBindAddr();
                InetSocketAddress socketAddress = new InetSocketAddress(bindAddr.getAddr(), bindAddr.getPort());
                this.server = new HTTPServer(socketAddress, this.prometheusMetricRegistry, opts.isDeamon());
                LOG.info("Metrics exporter started at `http://{}/metrics`", bindAddr.toString());
                return true;
            } catch (IOException e) {
                this.started.set(false);
                LOG.error("Failed to start metrics exporter", e);
                throw new RuntimeException("Failed to start metrics exporter", e);
            }
        }
        return false;
    }

    @Override
    public void shutdownGracefully() {
        if (this.started.compareAndSet(true, false)) {
            if (this.server != null) {
                this.server.close();
                LOG.info("Metrics exporter stopped");
            }
        }
    }

    @Override
    public String toString() {
        return "MetricsExporter{" + "opts=" + opts + '}';
    }
}
