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
package org.apache.camel.quarkus.component.jq.it;

import java.util.Map;

import org.apache.camel.builder.RouteBuilder;

public class JqRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // TODO: Revert back to using config properties
        // https://github.com/apache/camel-quarkus/issues/4011
        Map<String, String> globalOptions = getContext().getGlobalOptions();
        globalOptions.put("CamelJacksonEnableTypeConverter", "true");
        globalOptions.put("CamelJacksonTypeConverterToPojo", "true");

        from("direct:expression")
                .transform().jq(".foo")
                .to("mock:expression");

        from("direct:expressionHeader")
                .transform().jq(".foo", "Content")
                .to("mock:expressionHeader");

        from("direct:expressionHeaderFunction")
                .transform().jq(".foo = header(\"MyHeader\")")
                .to("mock:expressionHeaderFunction");

        from("direct:expressionHeaderString")
                .transform().jq(".foo", String.class, "Content")
                .to("mock:expressionHeaderString");

        from("direct:expressionPojo")
                .transform().jq(".book", Book.class)
                .to("mock:expressionPojo");

        from("direct:expressionProperty")
                .transform().jq(".foo", "Content")
                .to("mock:expressionProperty");

        from("direct:expressionPropertyFunction")
                .transform().jq(".foo = property(\"MyProperty\")")
                .to("mock:expressionPropertyFunction");

        from("direct:filter")
                .filter().jq(".value == \"valid\"")
                .to("mock:filter");

    }
}
