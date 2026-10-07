package tudor.inchizatorul;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    static volatile boolean muted = false;

    static void main(String[] args) throws IOException, InterruptedException {
        while (true) {
            Thread.sleep(1000);
            long minutes = convertToMinutes(getIdleTimeMillis());

            if (muted && minutes >= 5) {
                shutdown();
            }

            if (minutes >= 55) {
                mute();
                Dialog.showDialog("Pc will shut down...");
            }
            logger.info("Millis since last input: {}", getIdleTimeMillis());
        }
    }

    public static void shutdown() throws IOException {
        logger.error("SHUT DOWN COMMAND");
        Runtime.getRuntime().exec(
                "shutdown /s /f /t 0"
        );
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
