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

package io.greptime;

import io.greptime.common.Display;
import io.greptime.common.Endpoint;
import io.greptime.common.Lifecycle;
import io.greptime.common.util.Ensures;
import io.greptime.common.util.SharedScheduledPool;
import io.greptime.options.RouterOptions;
import io.greptime.rpc.Context;
import io.greptime.rpc.Observer;
import io.greptime.rpc.RpcClient;
import io.greptime.v1.Health.HealthCheckRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A route rpc client which cached the routing table information locally
 * and will auto refresh.
 */
public class RouterClient implements Lifecycle<RouterOptions>, Health, Display {

    private static final Logger LOG = LoggerFactory.getLogger(RouterClient.class);

    private static final SharedScheduledPool REFRESHER_POOL = Util.getSharedScheduledPool("route_cache_refresher", 1);

    private final AtomicLong refreshSequencer = new AtomicLong(0);

    private ScheduledExecutorService refresher;
    private RouterOptions opts;
    private RpcClient rpcClient;
    private Router<Void, Endpoint> router;

    @Override
    public boolean init(RouterOptions opts) {
        this.opts = Ensures.ensureNonNull(opts, "null `RouterClient.opts`").copy();
        this.rpcClient = this.opts.getRpcClient();

        List<Endpoint> endpoints = Ensures.ensureNonNull(this.opts.getEndpoints(), "null `endpoints`");

        this.router = new DefaultRouter();
        this.router.onRefresh(endpoints, null);

        long refreshPeriod = this.opts.getRefreshPeriodSeconds();
        if (refreshPeriod > 0) {
            this.refresher = REFRESHER_POOL.getObject();
            this.refresher.scheduleWithFixedDelay(
                    () -> {
                        long thisSequence = this.refreshSequencer.incrementAndGet();
                        checkHealth().whenComplete((r, t) -> {
                            if (t != null) {
                                LOG.warn("Failed to check health", t);
                                return;
                            }

                            synchronized (this.refreshSequencer) {
                                // If the next task has started, we will ignore the current result.
                                //
                                // I don't want to worry about the overflow issue with long anymore,
                                // because assuming one increment per second, it will take 292 years
                                // to overflow. I think that's sufficient.
                                if (thisSequence < this.refreshSequencer.get()) {
                                    LOG.warn("Skip outdated health check result, sequence: {}", thisSequence);
                                    return;
                                }

                                List<Endpoint> activities = new ArrayList<>();
                                List<Endpoint> inactivities = new ArrayList<>();
                                for (Map.Entry<Endpoint, Boolean> entry : r.entrySet()) {
                                    if (entry.getValue()) {
                                        activities.add(entry.getKey());
                                    } else {
                                        inactivities.add(entry.getKey());
                                    }
                                }
                                this.router.onRefresh(activities, inactivities);
                            }
                        });
                    },
                    Util.randomInitialDelay(180),
                    refreshPeriod,
                    TimeUnit.SECONDS);

            LOG.info("Router cache refresher started.");
        }

        return true;
    }

    @Override
    public void shutdownGracefully() {
        if (this.rpcClient != null) {
            this.rpcClient.shutdownGracefully();
        }

        if (this.refresher != null) {
            REFRESHER_POOL.returnObject(this.refresher);
            this.refresher = null;
        }
    }

    /**
     * Gets the current routing table.
     *
     * @return the current routing table
     */
    public CompletableFuture<Endpoint> route() {
        return this.router.routeFor(null);
    }

    /**
     * @see #invoke(Endpoint, Object, Context, long)
     *
     * @param endpoint the endpoint to invoke
     * @param request the request to send
     * @param ctx the context
     * @return the response future
     * @param <Req> request type
     * @param <Resp> response type
     */
    public <Req, Resp> CompletableFuture<Resp> invoke(Endpoint endpoint, Req request, Context ctx) {
        return invoke(endpoint, request, ctx, -1 /* use default rpc timeout */);
    }

    /**
     * Invoke the rpc request to the given endpoint.
     *
     * @param endpoint the endpoint to invoke
     * @param request the request to send
     * @param ctx the context
     * @param timeoutMs timeout in milliseconds
     * @return the response future
     * @param <Req> request type
     * @param <Resp> response type
     */
    public <Req, Resp> CompletableFuture<Resp> invoke(Endpoint endpoint, Req request, Context ctx, long timeoutMs) {
        CompletableFuture<Resp> future = new CompletableFuture<>();

        try {
            this.rpcClient.invokeAsync(
                    endpoint,
                    request,
                    ctx,
                    new Observer<Resp>() {

                        @Override
                        public void onNext(Resp value) {
                            future.complete(value);
                        }

                        @Override
                        public void onError(Throwable err) {
                            future.completeExceptionally(err);
                        }
                    },
                    timeoutMs);

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Invoke a server streaming rpc request to the given endpoint.
     *
     * @param endpoint the endpoint to invoke
     * @param request the request to send
     * @param ctx the context
     * @param observer the observer to receive the response
     * @param <Req> the request type
     * @param <Resp> the response type
     */
    public <Req, Resp> void invokeServerStreaming(
            Endpoint endpoint, Req request, Context ctx, Observer<Resp> observer) {
        try {
            this.rpcClient.invokeServerStreaming(endpoint, request, ctx, observer);
        } catch (Exception e) {
            observer.onError(e);
        }
    }

    /**
     * Invoke a client streaming rpc request to the given endpoint.
     *
     * @param endpoint the endpoint to invoke
     * @param defaultReqIns the default request instance
     * @param ctx the context
     * @param respObserver the observer to receive the response
     * @return the observer to send the request
     * @param <Req> the request type
     * @param <Resp> the response type
     */
    public <Req, Resp> Observer<Req> invokeClientStreaming(
            Endpoint endpoint, Req defaultReqIns, Context ctx, Observer<Resp> respObserver) {
        try {
            return this.rpcClient.invokeClientStreaming(endpoint, defaultReqIns, ctx, respObserver);
        } catch (Exception e) {
            respObserver.onError(e);
            return new Observer.RejectedObserver<>(e);
        }
    }

    @Override
    public void display(Printer out) {
        out.println("--- RouterClient ---").print("opts=").println(this.opts);

        if (this.rpcClient != null) {
            out.println("");
            this.rpcClient.display(out);
        }

        out.println("");
    }

    @Override
    public String toString() {
        return "RouterClient{" + "refresher=" + refresher + ", opts=" + opts + ", rpcClient=" + rpcClient + '}';
    }

    @Override
    public CompletableFuture<Map<Endpoint, Boolean>> checkHealth() {
        Map<Endpoint, CompletableFuture<Boolean>> futures =
                this.opts.getEndpoints().stream().collect(Collectors.toMap(Function.identity(), this::doCheckHealth));

        CompletableFuture<Void> all = CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]));

        return all.thenApply(ok -> futures.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().join())));
    }

    private CompletableFuture<Boolean> doCheckHealth(Endpoint endpoint) {
        HealthCheckRequest req = HealthCheckRequest.newBuilder().build();
        return invoke(endpoint, req, Context.newDefault(), this.opts.getCheckHealthTimeoutMs())
                .thenApply(resp -> true)
                .exceptionally(
                        t -> { // Handle failure and return false
                            LOG.warn("Failed to check health for endpoint: {}", endpoint, t);
                            return false;
                        });
    }

    /**
     * Request to a `frontend` server, which needs to return all members(frontend server),
     * or it can return only one domain address, it is also possible to return no address
     * at all, depending on the specific policy:
     * <p>
     * 1. The client configures only one static address and sends requests to that address
     * permanently. In other words: the router doesn't do anything, and this policy can't
     * dynamically switch addresses.
     * <p>
     * 2. The client configures some addresses (the address list of the frontend server),
     * the client send request using a rr or random policy, and frontend server needs to
     * be able to return the member list for the purpose of frontend server members change.
     */
    private static class DefaultRouter implements Router<Void, Endpoint> {

        private final AtomicReference<Endpoints> endpointsRef = new AtomicReference<>();

        @Override
        public CompletableFuture<Endpoint> routeFor(Void request) {
            Endpoints endpoints = this.endpointsRef.get();

            if (endpoints == null) {
                return Util.errorCf(new IllegalStateException("null `endpoints`"));
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();

            if (!endpoints.activities.isEmpty()) {
                int i = random.nextInt(0, endpoints.activities.size());
                return Util.completedCf(endpoints.activities.get(i));
            }

            if (!endpoints.inactivities.isEmpty()) {
                int i = random.nextInt(0, endpoints.inactivities.size());
                Endpoint goodLuck = endpoints.inactivities.get(i);
                LOG.warn("No active endpoint, return an inactive one: {}", goodLuck);
                return Util.completedCf(goodLuck);
            }

            return Util.errorCf(new IllegalStateException("empty `endpoints`"));
        }

        @Override
        public void onRefresh(List<Endpoint> activities, List<Endpoint> inactivities) {
            LOG.info("Router cache refreshed, activities: {}, inactivities: {}", activities, inactivities);
            this.endpointsRef.set(new Endpoints(activities, inactivities));
        }
    }

    static class Endpoints {
        final List<Endpoint> activities;
        final List<Endpoint> inactivities;

        Endpoints(List<Endpoint> activities, List<Endpoint> inactivities) {
            this.activities = activities == null ? new ArrayList<>() : activities;
            this.inactivities = inactivities == null ? new ArrayList<>() : inactivities;
        }
    }
}
