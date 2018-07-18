import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class TestParallel {

  public static void main(String[] args) throws InterruptedException {
    Flux<String> unit = Flux.range(0, 10).map(i -> "Item: " + i + ", thread: " + Thread.currentThread().getName());
    Flux.range(0, 5)
        .parallel()
        .runOn(Schedulers.parallel())
        // .doOnNext(i -> System.out.println( i + ":"+Thread.currentThread().getName()))
        .flatMap(l -> unit.map(s -> "Task: " + l + ", " + s))
        .doOnNext(System.out::println)
        .sequential()
        .subscribe();
    Thread.currentThread().join();
  }
}
