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

import java.io.IOException;
import java.io.InputStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/sap")
@ApplicationScoped
public class SapXMLResource extends SapResource {
    @GET
    @Path("/xml/srfc/{name}")
    public String xmlSrfcTest(@PathParam("name") String name) {
        // create a customer using the idoc route
        producer.sendBody("direct:idocTest", createIDoc(name));
        return producer.requestBody("direct:getCustomers", loadXML("get-template.xml", name), String.class).trim();
    }

    @GET
    @Path("/xml/trfc/{name}")
    public void xmlTrfcTest(@PathParam("name") String name) {
        // create a customer using the idoc route
        producer.sendBody("direct:trfcTest", loadXML("create-template.xml", name));
    }

    @GET
    @Path("/xml/idoc/{name}")
    public void xmlIDocTest(@PathParam("name") String name) {
        // create a customer using the idoc route
        producer.sendBody("direct:idocTest", loadXML("idoc-create-template.xml", name));
    }

    @GET
    @Path("/xml/idoclist/{name}/{name2}")
    public void xmlIDocListTest(@PathParam("name") String name, @PathParam("name2") String name2) {
        // create a customer using the idoc route
        String content = loadXML("idoclist-create-template.xml", name);
        content = content.replace("${custname2}", name2);
        producer.sendBody("direct:idocListTest", content);
    }

    private String loadXML(String name, String customerName) {
        String content;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            content = new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Unable to load XML file " + name, e);
        }

        content = content.replace("${repo}", System.getProperty("sap.repository")).replace("${custname}", customerName);
        return content;
    }
}
