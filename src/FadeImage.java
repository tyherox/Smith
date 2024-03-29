import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class FadeImage {

    public static void main(String[] args) {
        new FadeImage();
    }

    public FadeImage() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFrame frame = new JFrame("Testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new TestPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public static class TestPane extends JPanel {

        public static final long RUNNING_TIME = 1000;

        private BufferedImage inImage;
        private BufferedImage outImage;

        private float alpha = 0f;
        private long startTime = -1;

        public TestPane() {
            //setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
            setLayout(null);
            try {
                InputStream in = getClass().getResourceAsStream("/Graphics/Pictures/artwork1.jpg");
                inImage = ImageIO.read(in);
                in = getClass().getResourceAsStream("/Graphics/Pictures/artwork2.jpg");
                outImage = ImageIO.read(in);
            } catch (IOException exp) {
                exp.printStackTrace();
            }

            final Timer timer = new Timer(40, new ActionListener() {
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
                            alpha = 0f;
                        } else {
                            alpha = 1f - ((float) duration / (float) RUNNING_TIME);
                        }
                        repaint();
                    }
                }
            });

            JPanel panel = new JPanel(){
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(Color.ORANGE);
                    g2d.setComposite(AlphaComposite.SrcOver.derive(.6f));
                    g2d.fillRect(0,0,getWidth(),getHeight());
                    super.paintComponent(g);
                }
            };
            panel.setOpaque(false);
            panel.setBorder(null);
            panel.setBounds(500, 500, 500, 500);
            add(panel);

            JTextPane test = new JTextPane();
            test.setOpaque(false);
            test.setIgnoreRepaint(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(test);

            addMouseListener(new MouseAdapter() {
                boolean k = false;
                @Override
                public void mouseClicked(MouseEvent e) {
                    alpha = 0f;
                    BufferedImage tmp = inImage;
                    inImage = outImage;
                    outImage = tmp;
                    timer.start();
                }

            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                    Math.max(inImage.getWidth(), outImage.getWidth()),
                    Math.max(inImage.getHeight(), outImage.getHeight()));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
            int x = (getWidth() - inImage.getWidth()) / 2;
            int y = (getHeight()- inImage.getHeight()) / 2;
            g2d.drawImage(inImage, x, y, this);

            g2d.setComposite(AlphaComposite.SrcOver.derive(1f - alpha));
            x = (getWidth() - outImage.getWidth()) / 2;
            y = (getHeight()- outImage.getHeight()) / 2;
            g2d.drawImage(outImage, x, y, this);
            g2d.dispose();
        }

    }

}