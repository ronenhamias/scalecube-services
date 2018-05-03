package io.aeron.samples;

import io.aeron.Aeron;
import io.aeron.FragmentAssembler;
import io.aeron.Image;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;

import org.agrona.BitUtil;
import org.agrona.BufferUtil;
import org.agrona.CloseHelper;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.console.ContinueBarrier;

import java.util.concurrent.CountDownLatch;

/**
 * Ping component of Ping-Pong latency test.
 * <p>
 * Initiates and records times.
 */
public class Ping {
  private static final int PING_STREAM_ID = SampleConfiguration.PING_STREAM_ID;
  private static final int PONG_STREAM_ID = SampleConfiguration.PONG_STREAM_ID;
  private static final long NUMBER_OF_MESSAGES = SampleConfiguration.NUMBER_OF_MESSAGES;
  private static final long WARMUP_NUMBER_OF_MESSAGES = SampleConfiguration.WARMUP_NUMBER_OF_MESSAGES;
  private static final int WARMUP_NUMBER_OF_ITERATIONS = SampleConfiguration.WARMUP_NUMBER_OF_ITERATIONS;
  private static final int MESSAGE_LENGTH = SampleConfiguration.MESSAGE_LENGTH;
  private static final int FRAGMENT_COUNT_LIMIT = SampleConfiguration.FRAGMENT_COUNT_LIMIT;

  private static final String PING_CHANNEL = SampleConfiguration.PING_CHANNEL;
  private static final String PONG_CHANNEL = SampleConfiguration.PONG_CHANNEL;

  private static final UnsafeBuffer ATOMIC_BUFFER = new UnsafeBuffer(
      BufferUtil.allocateDirectAligned(MESSAGE_LENGTH, BitUtil.CACHE_LINE_LENGTH));
  // private static final Histogram HISTOGRAM = new Histogram(TimeUnit.SECONDS.toNanos(10), 3);
  private static final CountDownLatch LATCH = new CountDownLatch(1);
  private static final IdleStrategy POLLING_IDLE_STRATEGY = new BusySpinIdleStrategy();

  public static void main(final String[] args) throws Exception {
    final MediaDriver.Context driverctx = new MediaDriver.Context()
        .dirDeleteOnStart(true)
        .aeronDirectoryName("/aeron/media")
        .termBufferSparseFile(false)
        .threadingMode(ThreadingMode.DEDICATED)
        .conductorIdleStrategy(new BusySpinIdleStrategy())
        .receiverIdleStrategy(new BusySpinIdleStrategy())
        .senderIdleStrategy(new BusySpinIdleStrategy());

    MediaDriver driver = MediaDriver.launch(driverctx);

    final Aeron.Context ctx = new Aeron.Context().availableImageHandler(Ping::availablePongImageHandler);
    final FragmentHandler fragmentHandler = new FragmentAssembler(Ping::pongHandler);


    ctx.aeronDirectoryName(driver.aeronDirectoryName());


    System.out.println("Publishing Ping at " + PING_CHANNEL + " on stream Id " + PING_STREAM_ID);
    System.out.println("Subscribing Pong at " + PONG_CHANNEL + " on stream Id " + PONG_STREAM_ID);
    System.out.println("Message length of " + MESSAGE_LENGTH + " bytes");

    try (Aeron aeron = Aeron.connect(ctx)) {
      System.out.println(
          "Warming up... " + WARMUP_NUMBER_OF_ITERATIONS +
              " iterations of " + WARMUP_NUMBER_OF_MESSAGES + " messages");

      try (Publication publication = aeron.addPublication(PING_CHANNEL, PING_STREAM_ID);
          Subscription subscription = aeron.addSubscription(PONG_CHANNEL, PONG_STREAM_ID)) {
        LATCH.await();

        for (int i = 0; i < WARMUP_NUMBER_OF_ITERATIONS; i++) {
          roundTripMessages(fragmentHandler, publication, subscription, WARMUP_NUMBER_OF_MESSAGES);
        }

        Thread.sleep(100);
        final ContinueBarrier barrier = new ContinueBarrier("Execute again?");

        do {
          // HISTOGRAM.reset();
          System.out.println("Pinging " + NUMBER_OF_MESSAGES + " messages");

          roundTripMessages(fragmentHandler, publication, subscription, NUMBER_OF_MESSAGES);
          System.out.println("Histogram of RTT latencies in microseconds.");

          // HISTOGRAM.outputPercentileDistribution(System.out, 1000.0);
        } while (barrier.await());
      }
    }

    CloseHelper.quietClose(driver);
  }

  private static void roundTripMessages(
      final FragmentHandler fragmentHandler,
      final Publication publication,
      final Subscription subscription,
      final long count) {
    while (!subscription.isConnected()) {
      Thread.yield();
    }

    final Image image = subscription.imageAtIndex(0);

    for (long i = 0; i < count; i++) {
      long offeredPosition;

      do {
        ATOMIC_BUFFER.putLong(0, System.nanoTime());
      } while ((offeredPosition = publication.offer(ATOMIC_BUFFER, 0, MESSAGE_LENGTH)) < 0L);

      POLLING_IDLE_STRATEGY.reset();

      do {
        while (image.poll(fragmentHandler, FRAGMENT_COUNT_LIMIT) <= 0) {
          POLLING_IDLE_STRATEGY.idle();
        }
      } while (image.position() < offeredPosition);
    }
  }

  private static void pongHandler(final DirectBuffer buffer, final int offset, final int length, final Header header) {
    final long pingTimestamp = buffer.getLong(offset);
    final long rttNs = System.nanoTime() - pingTimestamp;

    // HISTOGRAM.recordValue(rttNs);
  }

  private static void availablePongImageHandler(final Image image) {
    final Subscription subscription = image.subscription();
    System.out.format(
        "Available image: channel=%s streamId=%d session=%d%n",
        subscription.channel(), subscription.streamId(), image.sessionId());

    if (PONG_STREAM_ID == subscription.streamId() && PONG_CHANNEL.equals(subscription.channel())) {
      LATCH.countDown();
    }
  }
}
