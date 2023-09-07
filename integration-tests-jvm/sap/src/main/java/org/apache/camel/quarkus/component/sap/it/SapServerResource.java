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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/sap")
@ApplicationScoped
public class SapServerResource extends SapResource {
    @GET
    @Path("/server/srfc")
    public void srfcServerTest() {
        producer.sendBody("direct:invokeSrfcTest", createRequest("BAPI_FLCUST_CREATEFROMDATA"));
    }

    @GET
    @Path("/server/trfc/{name}")
    public void trfcServerTest(@PathParam("name") String name) {
        producer.requestBody("direct:invokeTrfcTest", createCustomerRequest(name), String.class);
    }

    @GET
    @Path("/server/idoc/{name}")
    public void idocServerTest(@PathParam("name") String name) {
        producer.requestBody("direct:invokeIDocTest", createCustomerRequest(name), String.class);
    }
}
