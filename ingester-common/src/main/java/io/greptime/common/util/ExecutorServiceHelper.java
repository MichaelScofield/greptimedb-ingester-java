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

package io.greptime.common.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor service shutdown helper.
 */
public final class ExecutorServiceHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceHelper.class);

    /**
     * @see #shutdownAndAwaitTermination(ExecutorService, long)
     *
     * @param pool the executor service to shutdown
     * @return true if the executor service is shutdown successfully, false otherwise
     */
    public static boolean shutdownAndAwaitTermination(ExecutorService pool) {
        return shutdownAndAwaitTermination(pool, 1000);
    }

    /**
     * The following method shuts down an {@code ExecutorService} in two
     * phases, first by calling {@code shutdown} to reject incoming tasks,
     * and then calling {@code shutdownNow}, if necessary, to cancel any
     * lingering tasks.
     *
     * @param pool the executor service to shutdown
     * @param timeoutMillis the timeout in milliseconds
     * @return true if the executor service is shutdown successfully, false otherwise
     */
    public static boolean shutdownAndAwaitTermination(ExecutorService pool, long timeoutMillis) {
        if (pool == null) {
            return true;
        }

        LOG.info("Shutdown pool: {}.", pool);

        // disable new tasks from being submitted
        pool.shutdown();
        TimeUnit unit = TimeUnit.MILLISECONDS;
        long phaseOne = timeoutMillis / 5;
        try {
            // wait a while for existing tasks to terminate
            if (pool.awaitTermination(phaseOne, unit)) {
                return true;
            }
            pool.shutdownNow();
            // wait a while for tasks to respond to being cancelled
            if (pool.awaitTermination(timeoutMillis - phaseOne, unit)) {
                return true;
            }
            LOG.warn("Fail to shutdown pool: {}.", pool);
        } catch (InterruptedException e) {
            // (Re-)cancel if current thread also interrupted
            pool.shutdownNow();
            // preserve interrupt status
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
