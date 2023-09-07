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

import java.util.Map;

import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.apache.camel.ProducerTemplate;
import org.fusesource.camel.component.sap.model.idoc.Document;
import org.fusesource.camel.component.sap.model.idoc.Segment;
import org.fusesource.camel.component.sap.model.rfc.Structure;
import org.fusesource.camel.component.sap.util.IDocUtil;
import org.fusesource.camel.component.sap.util.RfcUtil;

@Path("/sap")
@ApplicationScoped
public class SapResource {
    @Inject
    ProducerTemplate producer;

    @GET
    @Path("/customers/{name}")
    public String getCustomers(@PathParam("name") String name) {
        return producer.requestBody("direct:getCustomers", listCustomersRequest(name), String.class).trim();
    }

    protected Structure createRequest(String functionModuleName) {
        try {
            return RfcUtil.getRequest(
                    JCoDestinationManager.getDestination(System.getProperty("sap.destination")).getRepository(),
                    functionModuleName);
        } catch (JCoException e) {
            throw new RuntimeException("Unable to create new request", e);
        }
    }

    protected Structure createCustomerRequest(String name) {
        Structure customer = createRequest("BAPI_FLCUST_CREATEFROMDATA");

        final Structure custData = customer.get("CUSTOMER_DATA", Structure.class);
        custData.putAll(customerData(name));
        return customer;
    }

    protected Map<String, String> customerData(String name) {
        return Map.ofEntries(
                Map.entry("CUSTNAME", name),
                Map.entry("FORM", "Mr."),
                Map.entry("STREET", "123 Rubble Lane"),
                Map.entry("POSTCODE", "01234"),
                Map.entry("CITY", "Bedrock"),
                Map.entry("COUNTR", "US"),
                Map.entry("PHONE", "800-555-1212"),
                Map.entry("EMAIL", "fred@bedrock.com"),
                Map.entry("CUSTTYPE", "P"),
                Map.entry("DISCOUNT", "005"),
                Map.entry("LANGU", "E"));
    }

    protected Structure listCustomersRequest(String name) {
        final Structure request = createRequest("BAPI_FLCUST_GETLIST");

        request.put("CUSTOMER_NAME", name);
        request.put("MAX_ROWS", 10);
        request.put("WEB_USER", "*");
        return request;
    }

    protected Document createIDoc(String name) {
        Document document = IDocUtil.createDocument(System.getProperty("sap.repository"), "FLCUSTOMER_CREATEFROMDATA01", null,
                null, null);
        document.setMessageType("FLCUSTOMER_CREATEFROMDATA");
        document.setRecipientPartnerNumber("QUICKCLNT");
        document.setRecipientPartnerType("LS");
        document.setSenderPartnerNumber(System.getProperty("sap.progId"));
        document.setSenderPartnerType("LS");

        // Retrieve document segments.
        final Segment rootSegment = document.getRootSegment();
        final Segment headerSegment = rootSegment.getChildren("E1SCU_CRE").add();
        final Segment newCustomerSegment = headerSegment.getChildren("E1BPSCUNEW").add();

        // Fill in New Customer Info
        newCustomerSegment.putAll(customerData(name));

        return document;
    }

}
