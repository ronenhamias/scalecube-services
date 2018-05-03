package aeron.playground;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;

import org.agrona.concurrent.BusySpinIdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;

import java.util.concurrent.TimeUnit;

public class MediaDriverBuilder {

  public static MediaDriver create(String aeronDirectoryName){
    final io.aeron.driver.MediaDriver.Context ctx =
        new io.aeron.driver.MediaDriver.Context()
            .dirDeleteOnStart(true)
            .aeronDirectoryName(aeronDirectoryName)
            .threadingMode(ThreadingMode.DEDICATED)
            .conductorIdleStrategy(new BusySpinIdleStrategy())
            .receiverIdleStrategy(new BusySpinIdleStrategy())
            .senderIdleStrategy(new BusySpinIdleStrategy());

    ctx.driverTimeoutMs(TimeUnit.MINUTES.toMillis(10));
    return MediaDriver.launch(ctx);
  }

  private MediaDriverBuilder() {}

}