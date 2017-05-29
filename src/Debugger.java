import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by JohnBae on 8/28/15.
 */
public class Debugger {

    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private static Calendar cal = Calendar.getInstance();
    private static SimpleDateFormat sdf = new SimpleDateFormat("'d'yyyy-MM-dd't'HH.mm.ss", Locale.US);
    private static String time = sdf.format(cal.getTime());
    private static File logFile = new File("/Users/"+System.getProperty("user.name") +"/Documents/"+TextEngine.getGameName()+"/logReport_" + time +".txt");
    private static boolean showingGUI = false;
    static DebuggerWindow gui = new DebuggerWindow();


    public static void initialize() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Debugger.log("initialized Debugger");
    }

    public static void log(String report) {
        System.out.println("*DEBUG* "+ report);
        try {
            BufferedWriter printer = new BufferedWriter(new FileWriter(logFile,true));
            printer.write("\n" + report);
            printer.close();
            gui.appendText("\n" + report);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showGUI(boolean t){
        if(t){
            showingGUI = true;
            gui.setVisible(true);
        }
        else {
            showingGUI = false;
            gui.setVisible(false);
        }
    }

    private static class DebuggerWindow extends JDialog{

        JTextPane text;
        JScrollPane scrollPane;

        public DebuggerWindow(){
            //setModal(true);
            //setUndecorated(true);
            setLayout(new BorderLayout());
            setBounds(screenSize.width / 4, (screenSize.height / 10) * 3, screenSize.width / 2, (screenSize.height / 5)
                    *3);

            JPanel dialogPane = new JPanel();
            dialogPane.setBackground(new Color(73, 73, 73));
            dialogPane.setLayout(new BoxLayout(dialogPane, BoxLayout.Y_AXIS));
            add(dialogPane);

            scrollPane = new JScrollPane();
            dialogPane.add(scrollPane);

            text = new JTextPane();
            scrollPane.setViewportView(text);

        }

        public void setText(String t){
            text.setText(t);
        }

        public void appendText(String t){
            try {
                text.getDocument().insertString(text.getDocument().getLength(),t,null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        }

    }
}
