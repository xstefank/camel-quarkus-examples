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
package org.apache.camel.example.saga;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;

public class TrainRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kafka:{{example.services.train}}?groupId=train")
                .saga()
                .propagation(SagaPropagation.MANDATORY)
                .option("id", header("id"))
                .compensation("direct:cancelPurchase")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .log("Buying train #${header.id}")
                .to("kafka:{{example.services.payment}}")
                .log("Payment for train #${header.id} done with transaction ${body}")
                .end();

        from("kafka:saga-payment-service-result")
                .log("Result received from payment service");

        from("direct:cancelPurchase")
                .log("Train purchase #${header.id} has been cancelled due to payment failure");
    }

}
