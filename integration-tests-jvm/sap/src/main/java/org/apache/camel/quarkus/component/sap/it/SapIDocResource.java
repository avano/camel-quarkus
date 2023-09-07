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
import org.fusesource.camel.component.sap.model.idoc.DocumentList;
import org.fusesource.camel.component.sap.util.IDocUtil;

@Path("/sap")
@ApplicationScoped
public class SapIDocResource extends SapResource {
    @GET
    @Path("/idoc/{name}")
    public void idocTest(@PathParam("name") String name) {
        producer.sendBody("direct:idocTest", createIDoc(name));
    }

    @GET
    @Path("/qidoc/{name}")
    public void qidocTest(@PathParam("name") String name) {
        producer.sendBody("direct:qidocTest", createIDoc(name));
    }

    @GET
    @Path("/idocList/{names}")
    public void idocListTest(@PathParam("names") String names) {
        producer.sendBody("direct:idocListTest", createIDocList(names));
    }

    @GET
    @Path("/qidocList/{names}")
    public void qidocListTest(@PathParam("names") String names) {
        producer.sendBody("direct:qidocListTest", createIDocList(names));
    }

    private DocumentList createIDocList(String names) {
        DocumentList list = IDocUtil.createDocumentList(System.getProperty("sap.repository"), "FLCUSTOMER_CREATEFROMDATA01",
                null, null, null);
        for (String name : names.split(",")) {
            list.add(createIDoc(name));
        }
        return list;
    }
}
