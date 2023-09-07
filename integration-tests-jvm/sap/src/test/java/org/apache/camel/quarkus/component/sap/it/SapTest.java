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
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.quarkus.test.InMemoryLogHandler;
import io.restassured.RestAssured;
import org.apache.commons.lang3.RandomStringUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class SapTest {
    private static final Logger rootLogger = LogManager.getLogManager()
            .getLogger("org.fusesource.camel.component.sap.ServerStateChangedListener");
    protected static final InMemoryLogHandler inMemoryLogHandler = new InMemoryLogHandler(
            record -> record.getMessage().contains("Server state changed from STARTED to ALIVE"));

    protected String customer1;
    protected String customer2;

    @BeforeAll
    public static void addLogHandler() {
        rootLogger.addHandler(inMemoryLogHandler);
    }

    @BeforeEach
    public void init() {
        customer1 = RandomStringUtils.randomAlphabetic(8);
        customer2 = RandomStringUtils.randomAlphabetic(8);
    }

    protected void verifyCustomer(String name) {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> RestAssured
                .when()
                .get("/sap/customers/{name}", name)
                .then()
                .statusCode(200)
                .extract().body().asString().contains(name));
    }

    public static boolean propertiesSet() {
        return System.getProperty("sap.gwhost") != null
                && System.getProperty("sap.gwserv") != null
                && System.getProperty("sap.gwserv") != null
                && System.getProperty("sap.ashost") != null
                && System.getProperty("sap.sysnr") != null
                && System.getProperty("sap.client") != null
                && System.getProperty("sap.user") != null
                && System.getProperty("sap.passwd") != null
                && System.getProperty("sap.progId") != null
                && System.getProperty("sap.destination") != null
                && System.getProperty("sap.fastDestination") != null
                && System.getProperty("sap.repository") != null
                && System.getProperty("sap.queue") != null;
    }
}
