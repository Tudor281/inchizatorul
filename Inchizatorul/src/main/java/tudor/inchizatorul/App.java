package tudor.inchizatorul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Hello world!
 *
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    static boolean muted = false;

    static void main() throws IOException, InterruptedException {
        //noinspection InfiniteLoopStatement
        while (true) {
            //noinspection BusyWait
            Thread.sleep(10000);
            long minutes = convertToMinutes(getIdleTimeMillis());

            if (muted && minutes >= 5) {
                shutdown();
            }

            if (minutes >= 55) {
                mute();
                Dialog.showDialog("PC will shut down...");
            }
            logger.info("Millis since last input: {}", getIdleTimeMillis());
        }
    }

    public static void shutdown() throws IOException {
        logger.info("Logging shut down...");
        logShutdown();
        logger.error("SHUT DOWN COMMAND");
        Runtime.getRuntime().exec("shutdown /s /f /t 0".split(" "));
    }

    public static void logShutdown() throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String line = LocalDateTime.now().minusMinutes(30).format(fmt) + System.lineSeparator();

        Path path = Path.of("shutdown_log.csv");

        if (!Files.exists(path)) {
            Files.writeString(path, line);
        } else {
            String oldContent = Files.readString(path);
            Files.writeString(path,line + oldContent);
        }
    }

    public static void mute() throws IOException {
        Runtime.getRuntime().exec(
                new String[]{
                        "powershell",
                        "-Command",
                        "(New-Object -ComObject WScript.Shell).SendKeys([char]173)"
                }
        );

        muted = !muted;
    }

    private static long getIdleTimeMillis() {
        WinUser.LASTINPUTINFO info = new WinUser.LASTINPUTINFO();
        info.cbSize = info.size();

        User32.INSTANCE.GetLastInputInfo(info);

        long lastInputTick = Integer.toUnsignedLong(info.dwTime);
        long currentTick = Kernel32.INSTANCE.GetTickCount();

        return currentTick - lastInputTick;
    }

    private static long convertToMinutes(long millis) {
        return millis / 60000;
    }
}
