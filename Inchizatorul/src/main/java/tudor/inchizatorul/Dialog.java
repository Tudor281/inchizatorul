package tudor.inchizatorul;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Dialog {

    private static JDialog dialog;

    /*
    Usage:

    App.openConfirmationDialog2("Is this going to work?");

    synchronized (WaitDialog.class) {
        WaitDialog.class.wait();
    }

    And you can close it from OK button, or from another thread with close() when you detected the condition worked
     */

    public static void showDialog(String message) {
        dialog = new JDialog((Frame) null, "Waiting", false); // non-modal
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JLabel label = new JLabel(
                "<html>"+message+"</html>",
                SwingConstants.CENTER
        );

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            try {
                close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        dialog.setAlwaysOnTop(true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(label, BorderLayout.CENTER);
        dialog.add(ok, BorderLayout.SOUTH);

        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static void close() throws IOException {
        if (dialog != null && dialog.isDisplayable()) {
            SwingUtilities.invokeLater(dialog::dispose);
        }
        synchronized (Dialog.class) {
            Dialog.class.notifyAll();
        }

        App.mute();
    }

    public static void waitClose() throws InterruptedException {
        synchronized (Dialog.class) {
            Dialog.class.wait();
        }
    }
}
