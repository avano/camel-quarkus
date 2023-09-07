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

import java.util.List;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

@QuarkusTest
@EnabledIf("propertiesSet")
public class SapIDocTest extends SapTest {
    @Test
    public void idocTest() {
        RestAssured
                .when()
                .get("/sap/idoc/{name}", customer1)
                .then()
                .statusCode(204);
        verifyCustomer(customer1);
    }

    @Test
    public void qidocTest() {
        RestAssured
                .when()
                .get("/sap/qidoc/{name}", customer1)
                .then()
                .statusCode(204);
        verifyCustomer(customer1);
    }

    @Test
    public void idocListTest() {
        RestAssured
                .when()
                .get("/sap/idocList/{names}", customer1 + "," + customer2)
                .then()
                .statusCode(204);
        for (String customer : List.of(customer1, customer2)) {
            verifyCustomer(customer);
        }
    }

    @Test
    public void qidocListTest() {
        RestAssured
                .when()
                .get("/sap/qidocList/{names}", customer1 + "," + customer2)
                .then()
                .statusCode(204);
        for (String customer : List.of(customer1, customer2)) {
            verifyCustomer(customer);
        }
    }
}
