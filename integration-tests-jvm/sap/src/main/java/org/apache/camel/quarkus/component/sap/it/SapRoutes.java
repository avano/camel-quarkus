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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.BindToRegistry;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.Pair;
import org.fusesource.camel.component.sap.CurrentProcessorDefinitionInterceptStrategy;
import org.fusesource.camel.component.sap.SapConnectionConfiguration;
import org.fusesource.camel.component.sap.model.idoc.DocumentList;
import org.fusesource.camel.component.sap.model.rfc.*;
import org.fusesource.camel.component.sap.model.rfc.impl.DestinationDataImpl;
import org.fusesource.camel.component.sap.model.rfc.impl.ServerDataImpl;

public class SapRoutes extends RouteBuilder {
    private final String dest = System.getProperty("sap.destination");
    private final String fastDest = dest + "fast";

    @BindToRegistry("sap-configuration")
    public SapConnectionConfiguration sapConfiguration() {
        SapConnectionConfiguration configuration = new SapConnectionConfiguration();
        configuration.setDestinationDataStore(getDestinationData());
        configuration.setServerDataStore(getServerData());
        return configuration;
    }

    private Map<String, ServerData> getServerData() {
        Map<String, ServerData> serverDataMap = new HashMap<>();

        for (Pair<String> data : List.of(new Pair<>("srfcServer", "SRFC"), new Pair<>("trfcServer", "TRFC"),
                new Pair<>("idocServer", System.getProperty("sap.progId")))) {
            ServerData serverData = new ServerDataImpl();
            serverData.setGwhost(System.getProperty("sap.gwhost"));
            serverData.setGwserv(System.getProperty("sap.gwserv"));
            serverData.setProgid(data.getRight());
            serverData.setRepositoryDestination(dest);
            serverData.setConnectionCount("2");

            serverDataMap.put(data.getLeft(), serverData);
        }
        return serverDataMap;
    }

    private Map<String, DestinationData> getDestinationData() {
        Map<String, DestinationData> destinationDataMap = new HashMap<>();
        for (String destination : List.of(dest, fastDest)) {
            DestinationDataImpl destinationData = new DestinationDataImpl();
            destinationData.setAshost(System.getProperty("sap.ashost"));
            destinationData.setSysnr(System.getProperty("sap.sysnr"));
            destinationData.setClient(System.getProperty("sap.client"));
            destinationData.setUser(System.getProperty("sap.user"));
            destinationData.setPasswd(System.getProperty("sap.passwd"));
            destinationData.setLang("en");
            destinationData.setSerializationFormat(destination.contains("fast") ? "columnBased" : "rowBased");

            destinationDataMap.put(destination, destinationData);
        }
        return destinationDataMap;
    }

    @Override
    public void configure() throws Exception {
        ((DefaultCamelContext) getCamelContext()).addInterceptStrategy(new CurrentProcessorDefinitionInterceptStrategy());

        from("direct:idocTest").to("sap-idoc-destination:{{sap.destination}}:FLCUSTOMER_CREATEFROMDATA01");
        from("direct:qidocTest").to("sap-qidoc-destination:{{sap.destination}}:{{sap.queue}}:FLCUSTOMER_CREATEFROMDATA01");
        from("direct:idocListTest").to("sap-idoclist-destination:{{sap.destination}}:FLCUSTOMER_CREATEFROMDATA01");
        from("direct:qidocListTest")
                .to("sap-qidoclist-destination:{{sap.destination}}:{{sap.queue}}:FLCUSTOMER_CREATEFROMDATA01");

        from("direct:trfcTest").to("sap-trfc-destination:{{sap.destination}}:BAPI_FLCUST_CREATEFROMDATA");
        from("direct:qrfcTest").to("sap-qrfc-destination:{{sap.destination}}:{{sap.queue}}:BAPI_FLCUST_CREATEFROMDATA");
        from("direct:fastTrfcTest").to("sap-trfc-destination:{{sap.fastDestination}}:BAPI_FLCUST_CREATEFROMDATA");
        from("direct:fastQrfcTest").to("sap-qrfc-destination:{{sap.fastDestination}}:{{sap.queue}}:BAPI_FLCUST_CREATEFROMDATA");

        from("direct:invokeTrfcTest").to("sap-srfc-destination:{{sap.destination}}:ZFLCUST_INVOKE_TRFC");
        from("sap-trfc-server:trfcServer:BAPI_FLCUST_CREATEFROMDATA").process(ex -> ex.getIn().setBody(
                ex.getIn().getBody(Structure.class).get("CUSTOMER_DATA", Structure.class).get("CUSTNAME", String.class)))
                .to("mock:trfcServer");

        from("direct:invokeSrfcTest").to("sap-srfc-destination:{{sap.destination}}:ZFLCUST_INVOKE_SRFC");
        from("sap-srfc-server:srfcServer:BAPI_FLCUST_GETLIST").to("mock:srfcServer");

        from("direct:invokeIDocTest").to("sap-srfc-destination:{{sap.destination}}:ZFLCUST_INVOKE_IDOC");
        from("sap-idoclist-server:idocServer:FLCUSTOMER_CREATEFROMDATA01")
                .process(
                        ex -> ex.getIn().setBody("IDoc Server: " + ex.getIn().getBody(DocumentList.class).get(0).getIDocType()))
                .to("mock:idocServer");

        from("direct:getCustomers")
                .to("sap-srfc-destination:{{sap.destination}}:BAPI_FLCUST_GETLIST")
                .process(ex -> {
                    final Response body = ex.getIn().getBody(Response.class);
                    final Table<?> customerList = body.get("CUSTOMER_LIST", Table.class);
                    StringBuilder customerNames = new StringBuilder();
                    for (Structure row : customerList.getRows()) {
                        customerNames.append(row.get("CUSTNAME", String.class)).append("\n");
                    }
                    ex.getIn().setBody(customerNames.toString());
                });
    }
}
