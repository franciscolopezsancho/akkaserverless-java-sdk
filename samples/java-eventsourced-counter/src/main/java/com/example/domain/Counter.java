/* This code was generated by Akka Serverless tooling.
 * As long as this file exists it will not be re-generated.
 * You are free to make changes to this file.
 */
package com.example.domain;

import com.akkaserverless.javasdk.SideEffect;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity.Effect;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.example.CounterApi;
import com.google.protobuf.Empty;
import java.util.Optional;

public class Counter extends AbstractCounter {

  @SuppressWarnings("unused")
  private final String entityId;

  public Counter(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public CounterDomain.CounterState emptyState() {
    return CounterDomain.CounterState.getDefaultInstance();
  }

  private CounterDomain.CounterState updateState(CounterDomain.CounterState currentState, int value) {
    int newValue = currentState.getValue() + value;
    return currentState.toBuilder().setValue(newValue).build();
  }

  @Override
  public Effect<Empty> increase(CounterDomain.CounterState currentState, CounterApi.IncreaseValue increaseValue) {
    if (increaseValue.getValue() < 0)
      return effects().error("Value must be a zero or a positive number");
    else
      return effects()
              .emitEvent(CounterDomain.ValueIncreased.newBuilder().setValue(increaseValue.getValue()).build())
              .thenReply(__ -> Empty.getDefaultInstance());
  }

  @Override
  public Effect<Empty> increaseWithSideEffect(CounterDomain.CounterState currentState, CounterApi.IncreaseValue increaseValue) {
     int doubled = increaseValue.getValue() * 2;
    CounterApi.IncreaseValue increaseValueDoubled =
        increaseValue.toBuilder().setValue(doubled).build(); 

    if (increaseValue.getValue() < 0)
      return effects().error("Value must be a zero or a positive number");
    else
      return effects()
              .emitEvent(CounterDomain.ValueIncreased.newBuilder().setValue(increaseValue.getValue()).build())
              .thenAddSideEffect(__ -> SideEffect.of(components().counter().increase(increaseValueDoubled)))
              .thenReply(__ -> Empty.getDefaultInstance());
  }

  @Override
  public Effect<Empty> increaseWithConditional(CounterDomain.CounterState currentState, CounterApi.IncreaseValue increaseValue) {
    int doubled = increaseValue.getValue() * 2;

    if (increaseValue.getValue() < 0)
      return effects().error("Value must be a zero or a positive number");
    else if(commandContext().metadata().get("myKey").equals(Optional.of("myValue"))){ 
      return effects()
              .emitEvent(CounterDomain.ValueIncreased.newBuilder().setValue(doubled).build())
              .thenReply(__ -> Empty.getDefaultInstance());
    } else
      return effects()
              .emitEvent(CounterDomain.ValueIncreased.newBuilder().setValue(increaseValue.getValue()).build())
              .thenReply(__ -> Empty.getDefaultInstance());
  }


  @Override
  public Effect<Empty> decrease(CounterDomain.CounterState currentState, CounterApi.DecreaseValue decreaseValue) {
    if (decreaseValue.getValue() > 0){
      return effects().error("Value must be a zero or a negative number");
    } else if(updateState(currentState, decreaseValue.getValue()).getValue() < 0){
      return effects().error("Decrease value is too high. Counter cannot become negative");
    } else
      return effects()
          .emitEvent(CounterDomain.ValueDecreased.newBuilder().setValue(decreaseValue.getValue()).build())
          .thenReply(__ -> Empty.getDefaultInstance());
  }

  @Override
  public Effect<Empty> reset(CounterDomain.CounterState currentState, CounterApi.ResetValue resetValue) {
    return effects()
            .emitEvent(CounterDomain.ValueReset.getDefaultInstance())
            .thenReply(__ -> Empty.getDefaultInstance());
  }

  @Override
  public Effect<CounterApi.CurrentCounter> getCurrentCounter(CounterDomain.CounterState currentState, CounterApi.GetCounter getCounter) {
    CounterApi.CurrentCounter counter =
        CounterApi.CurrentCounter.newBuilder()
            .setValue(currentState.getValue())
            .build();
    return effects().reply(counter);
  }

  @Override
  public CounterDomain.CounterState valueIncreased(CounterDomain.CounterState currentState, CounterDomain.ValueIncreased valueIncreased) {
    return updateState(currentState, valueIncreased.getValue());

  }
  @Override
  public CounterDomain.CounterState valueDecreased(CounterDomain.CounterState currentState, CounterDomain.ValueDecreased valueDecreased) {
    return updateState(currentState, valueDecreased.getValue());
  }
  @Override
  public CounterDomain.CounterState valueReset(CounterDomain.CounterState currentState, CounterDomain.ValueReset valueReset) {
    return emptyState();
  }

}