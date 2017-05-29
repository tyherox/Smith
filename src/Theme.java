import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Theme {

    private static ArrayList<JButton> buttons = new ArrayList<>();
    private static ArrayList<JButton> mButtons = new ArrayList<>();
    private static ArrayList<JComponent> text = new ArrayList<>();

    float fontSize = 20f;

    static private Font defaultFont;
    static private Font boldFont;

    private Color primaryColor = new Color(48, 47, 47);
    private Color secondaryColor = new Color(139, 142, 144);
    private Color foreground = new Color(232, 232, 232);
    private Color defaultButton = new Color(28, 28, 28);
    private Color rolloverButton = new Color(96, 96, 96);
    private Color pressedButton = new Color(68, 78, 96);

    private BufferedImage menuBackground = null;


    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public Color getPrimaryColor(){
        return primaryColor;
    }

    public Color getSecondaryColor(){
        return secondaryColor;
    }

    public Color getDefaultButton(){
        return defaultButton;
    }

    public Color getRolloverButton(){
        return rolloverButton;
    }

    public Color getPressedButton(){
        return pressedButton;
    }

    public Color getForeground(){
        return foreground;
    }

    public Font getDefaultFont(){
        return defaultFont;
    }

    public Font getBoldFont(){
        return boldFont;
    }


    public Theme() {
        try {

            defaultFont = new Font("Monospaced", Font.PLAIN, 15);
            boldFont = new Font("Monospaced", Font.BOLD, 15);
            File check = new File(TextEngine.getScriptAddress()+"/Images/menu.jpg");
            if(check.exists()) {
                Debugger.log("Menu found");
                menuBackground = ImageIO.read(check);
            }

            Debugger.log("initialized fonts");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getMenuBackground(){
        return menuBackground;
    }

    public void registerButton(JButton k) {
        if(!buttons.contains(k))buttons.add(k);
        k.setForeground(foreground);
        k.setFont(defaultFont.deriveFont(fontSize));
    }

    public void registerThumb(JComponent k){
        k.setBackground(getSecondaryColor());
    }

    public void registerText(JComponent k){
        if(!text.contains(k))text.add(k);
        k.setForeground(foreground);
        k.setFont(defaultFont.deriveFont(fontSize*1.5f));
    }

    public void registerSmallerText(JComponent k){
        if(!text.contains(k))text.add(k);
        k.setForeground(foreground);
        k.setFont(defaultFont.deriveFont(fontSize));
    }

    public void registerMenuButton(JButton k) {
        if(!mButtons.contains(k))mButtons.add(k);
        k.setForeground(foreground);
        k.setFont(defaultFont.deriveFont(fontSize*1.5f));
    }

    public Font getMenuFont() {
        return defaultFont.deriveFont(fontSize*3);
    }
}
