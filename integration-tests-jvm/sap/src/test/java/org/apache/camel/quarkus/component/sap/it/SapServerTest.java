/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.component.sap.it;

import java.util.concurrent.TimeUnit;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

@QuarkusTest
@EnabledIf("propertiesSet")
public class SapServerTest extends SapTest {

    @Inject
    CamelContext context;

    @Test
    public void invokeSrfcTest() throws InterruptedException {
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .until(() -> inMemoryLogHandler.getRecords().stream().anyMatch(r -> r.getMessage().contains("SRFC")));

        MockEndpoint endpoint = context.getEndpoint("mock:srfcServer", MockEndpoint.class);
        endpoint.expectedMessageCount(1);

        RestAssured
                .when()
                .get("/sap/server/srfc")
                .then()
                .statusCode(204);

        endpoint.assertIsSatisfied(5000L);
    }

    @Test
    public void invokeTrfcTest() throws InterruptedException {
        MockEndpoint endpoint = context.getEndpoint("mock:trfcServer", MockEndpoint.class);
        endpoint.expectedMessageCount(1);
        endpoint.expectedBodiesReceived(customer1);

        RestAssured
                .when()
                .get("/sap/server/trfc/{name}", customer1)
                .then()
                .statusCode(204);

        endpoint.assertIsSatisfied(5000L);
    }

    @Test
    public void invokeIDocServer() throws InterruptedException {
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .until(() -> inMemoryLogHandler.getRecords().stream()
                        .anyMatch(r -> r.getMessage().contains(System.getProperty("sap.progId"))));
        MockEndpoint endpoint = context.getEndpoint("mock:idocServer", MockEndpoint.class);
        endpoint.expectedMessageCount(1);

        RestAssured
                .when()
                .get("/sap/server/idoc/{name}", customer1)
                .then()
                .statusCode(204);

        endpoint.assertIsSatisfied(5000L);

    }
}
