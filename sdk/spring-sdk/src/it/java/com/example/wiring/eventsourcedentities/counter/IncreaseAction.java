/*
 * Copyright 2021 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.wiring.eventsourcedentities.counter;

import kalix.javasdk.action.Action;
import kalix.javasdk.action.ActionCreationContext;
import kalix.springsdk.KalixClient;
import kalix.springsdk.KalixConfigurationTest;
import kalix.springsdk.annotations.Subscribe;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Import(KalixConfigurationTest.class)
public class IncreaseAction extends Action {

  private KalixClient kalixClient;

  private ActionCreationContext context;

  public IncreaseAction(KalixClient kalixClient, ActionCreationContext context) {
    this.kalixClient = kalixClient;
    this.context = context;
  }

  @Subscribe.EventSourcedEntity(value = CounterEntity.class)
  public Effect<ValueMultiplied> printMultiply(ValueMultiplied event) {
    return effects().reply(event);
  }

  // FIXME how to deal with multiple ignores?
  @Subscribe.EventSourcedEntity(value = CounterEntity.class)
  public Effect<Integer> printIncrease(ValueIncreased event) {
    String entityId = this.actionContext().metadata().asCloudEvent().subject().get();
    if (event.value == 42) {
      CompletionStage<Integer> res =
          kalixClient.post("/counter/" + entityId + "/increase/1", "", Integer.class).execute();
      return effects().asyncReply(res);
    }
    return effects().reply(event.value);
  }
}
