package io.scalecube.services.transport.rsocket;

import java.time.Duration;
import java.util.function.Function;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

public class EitherPublisher<T> {

  public static void main(String[] args) throws InterruptedException {
    Function<String, Flux<Long>> function = str -> Flux.interval(Duration.ofSeconds(1));
    EmitterProcessor<String> processor = EmitterProcessor.create();

    Flux<String> socket = Flux.from(processor);
    Flux<Long> result = socket.flatMap(str -> {
      return function.apply(str).or(socket.then().cast(Long.class));
    });

    result.subscribe(System.out::println, System.err::println, () -> System.out.println("COMPLETED"));

    processor.onNext("first");

    Thread.sleep(3000);

    processor.onError(new RuntimeException("CLOSE"));

    Thread.sleep(3000);
  }

}
