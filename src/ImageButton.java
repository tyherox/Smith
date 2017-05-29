import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class ImageButton extends JButton {

    boolean selected = false;

    Color defaultButton;
    Color rolloverButton;
    Color pressedButton;
    Color current = null;

    AffineTransform at = new AffineTransform();
    Theme theme = GUI.theme;


    public ImageButton(String text) {
        super.setText(text);
        super.setHorizontalTextPosition(SwingConstants.CENTER);
        super.setVerticalTextPosition(SwingConstants.CENTER);
        setContentAreaFilled(false);
        //setOpaque(false);

        defaultButton = theme.getDefaultButton();
        rolloverButton = theme.getRolloverButton();
        pressedButton = theme.getPressedButton();
        current = defaultButton;

        setBorderPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = defaultButton;
                    getParent().repaint();
                }
                super.mouseReleased(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = pressedButton;
                    getParent().repaint();
                }
                super.mousePressed(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = rolloverButton;
                    getParent().repaint();
                }
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = defaultButton;
                    getParent().repaint();
                }
                super.mouseExited(e);
            }
        });
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(!isEnabled()){
                    current = pressedButton;
                    if(getParent()!=null) getParent().repaint();
                }
            }
        });
        revalidate();

        repaint();
        other();
    }

    public ImageButton() {

        super.setHorizontalTextPosition(SwingConstants.CENTER);
        super.setVerticalTextPosition(SwingConstants.CENTER);
        setContentAreaFilled(false);
        //setOpaque(false);

        defaultButton = theme.getDefaultButton();
        rolloverButton = theme.getRolloverButton();
        pressedButton = theme.getPressedButton();

        current = defaultButton;
        setBorderPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = defaultButton;
                    getParent().repaint();
                }
                super.mouseReleased(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = pressedButton;
                    getParent().repaint();
                }
                super.mousePressed(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = rolloverButton;
                    getParent().repaint();
                }
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()&&!selected) {
                    current = defaultButton;
                    getParent().repaint();
                }
                super.mouseExited(e);
            }
        });
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(!isEnabled()){
                    current = pressedButton;
                    //if(getParent()!=null) getParent().repaint();
                }
            }
        });
        revalidate();

        repaint();
        other();
    }

    public void deselect(){
        selected = false;
        current = defaultButton;
        repaint();
    }

    public void select(){
        selected = true;
        current = pressedButton;
        repaint();
    }

    @Override
    public void setEnabled(boolean k){
        super.setEnabled(k);
        if(k) deselect();
        else select();
    }

    public boolean isPressed(){
        if (selected)return true;
        else return false;
    }

    public void other(){

    }

    @Override
    protected void paintComponent(Graphics g) {
        Dimension DIMENSION = new Dimension(getWidth(),getHeight());
        int border = DIMENSION.width/300;
        if(border<2) border = 2;

        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(rolloverButton);
        graphics.fillRoundRect(0, 0, DIMENSION.width, DIMENSION.height, border * 3, border * 3);
        graphics.setColor(current);
        graphics.fillRoundRect(border, border, DIMENSION.width - border * 2, DIMENSION.height - border * 2, border *
                3, border * 3);
        graphics.dispose();

        super.paintComponent(g);
    }
}
