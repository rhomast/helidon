/*
 * Copyright (c) 2018, 2025 Oracle and/or its affiliates.
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

package io.helidon.health.checks;

import java.lang.management.ThreadMXBean;

import io.helidon.health.HealthCheckResponse;
import io.helidon.health.HealthCheckType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DeadlockHealthCheckTest {
    private ThreadMXBean threadBean;

    @BeforeEach
    void init() {
        threadBean = Mockito.mock(ThreadMXBean.class);
    }

    @Test
    void testThatHealthCheckNameDoesNotChange() {
        DeadlockHealthCheck check = new DeadlockHealthCheck(threadBean);
        HealthCheckResponse response = check.call();

        assertThat(check.type(), is(HealthCheckType.LIVENESS));
        assertThat(check.name(), is("deadlock"));
        assertThat(check.path(), is("deadlock"));
        assertThat(response.status(), is(HealthCheckResponse.Status.UP));
    }

    @Test
    void deadlockDetected() {
        Mockito.when(threadBean.findDeadlockedThreads()).thenReturn(new long[] {123, 456}); // Deadlocked!
        DeadlockHealthCheck check = new DeadlockHealthCheck(threadBean);
        HealthCheckResponse response = check.call();

        assertThat(response.status(), is(HealthCheckResponse.Status.DOWN));
    }

    @Test
    void noDeadlockDetected() {
        Mockito.when(threadBean.findDeadlockedThreads()).thenReturn(null); // no deadlock
        DeadlockHealthCheck check = new DeadlockHealthCheck(threadBean);
        HealthCheckResponse response = check.call();
        assertThat(response.status(), is(HealthCheckResponse.Status.UP));
    }

    @Test

    void errorInvokingMBean() {
        Mockito.when(threadBean.findDeadlockedThreads()).thenThrow(new RuntimeException("Simulated error invoking MBean"));
        DeadlockHealthCheck check = new DeadlockHealthCheck(threadBean);
        HealthCheckResponse response = check.call();
        assertThat(response.status(), is(HealthCheckResponse.Status.ERROR));
    }
}
