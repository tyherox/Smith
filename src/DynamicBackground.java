import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by JohnBae on 9/21/15.
 */
public class DynamicBackground extends JPanel {

    BufferedImage oldImage;
    BufferedImage newImage;

    String newImageName = "null";
    String oldImageName = "null";

    Timer timer;

    private float alpha =0f;
    private long startTime = -1;
    private static final long RUNNING_TIME = 1000;

    public DynamicBackground(){
        setOpaque(false);
    }

    public void switchBackground(final String artwork){

        if(!artwork.equals("EMPTY")){
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Debugger.log("Switched Art");

                    oldImageName = newImageName;
                    newImageName = artwork;

                    oldImage = newImage;

                    if(oldImage==null) oldImage = TextEngine.gui.getTheme().getMenuBackground();
                    try {
                        newImage = ImageIO.read(new FileInputStream(TextEngine.getScriptAddress()+"/Images/" + artwork));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    timer = new Timer(40, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (startTime < 0) {
                                startTime = System.currentTimeMillis();

                            } else {

                                long time = System.currentTimeMillis();
                                long duration = time - startTime;
                                if (duration >= RUNNING_TIME) {
                                    startTime = -1;
                                    ((Timer) e.getSource()).stop();
                                    setBackground(Color.BLACK);
                                    setOpaque(true);
                                    alpha = 0f;
                                } else {
                                    alpha = 1f - ((float) duration / (float) RUNNING_TIME);
                                }
                                getParent().repaint();
                            }
                        }
                    });
                    if(!oldImageName.equals(newImageName)) {
                        System.out.println("CHANGING IMAGE");
                        timer.start();
                    }
                }
            });
        }
        else {
            oldImage = null;
            newImage = null;
            setOpaque(false);
            getParent().repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        if(newImage!=null&&oldImage!=null) {
            g2d.setComposite(AlphaComposite.SrcOver.derive(1f - alpha));
            g2d.drawImage(newImage, 0, 0, getWidth(), getHeight(), this);
            g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2d.drawImage(oldImage, 0, 0, getWidth(), getHeight(), this);
        }
        else {
            g2d.setComposite(AlphaComposite.SrcOver.derive(0f));
            g2d.fillRect(0,0,getWidth(),getHeight());
        }
        g2d.dispose();


    }

}
