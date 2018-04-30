package aeron.playground;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

import org.agrona.concurrent.SleepingIdleStrategy;

import java.util.concurrent.TimeUnit;

public class MediaDriverHolder {
  private static final MediaDriverHolder INSTANCE = new MediaDriverHolder();

  static {
    final io.aeron.driver.MediaDriver.Context ctx =
        new io.aeron.driver.MediaDriver.Context()
            .threadingMode(ThreadingMode.SHARED)
            .dirDeleteOnStart(true)
            .conductorIdleStrategy(new SleepingIdleStrategy(TimeUnit.MILLISECONDS.toNanos(1)))
            .receiverIdleStrategy(new SleepingIdleStrategy(TimeUnit.MILLISECONDS.toNanos(1)))
            .senderIdleStrategy(new SleepingIdleStrategy(TimeUnit.MILLISECONDS.toNanos(1)));

    ctx.driverTimeoutMs(TimeUnit.MINUTES.toMillis(10));
    MediaDriver.launch(ctx);
  }

  private MediaDriverHolder() {}

  public static MediaDriverHolder getInstance() {
    return INSTANCE;
  }
}