import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;

public class GUI extends JFrame {

    JPanel glass;

    boolean retina = false;
    boolean gamestate = false;

    public static Theme theme = new Theme();
    private GameWindow gamewindow = new GameWindow();
    private OptionWindow optionWindow = new OptionWindow();
    private FileWindow fileWindow = new FileWindow();
    private MenuWindow menuWindow = new MenuWindow();
    private InGameMenuWindow inGameMenuWindow = new InGameMenuWindow();
    private DialogWindow dialogWindow = new DialogWindow();

    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    private String current = "";

    JPanel contentPane;

    CardLayout card = new CardLayout(){
        @Override
        public void show(Container parent, String name) {
            if(current!=null&&current.equals("OptionWindow")) TextEngine.database.saveSystemFiles();
            super.show(parent,name);
            current = name;
        }
    };

    /** GUI Creation **/

    public GUI(){

        glass = new JPanel() {
            public void paintComponent(Graphics g) {
                g.setColor(new Color(63, 59, 59, 190));
                g.fillRect(0,0,getWidth(),getHeight());
                //super.paintComponents(g);
            }
        };
        glass.setOpaque(false);
        glass.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                me.consume();
            }

        });
        glass.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                e.consume();
            }
        });
        glass.setLayout(null);
        setGlassPane(glass);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(0, 0, screenSize.width, screenSize.height);

        contentPane = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g.create();
                if(theme.getMenuBackground()!=null){
                    graphics.drawImage(theme.getMenuBackground(), 0, 0, getWidth(),
                            getHeight(),null);
                }
                graphics.dispose();
            }
        };
        contentPane.setBorder(null);
        contentPane.setLayout(card);
        contentPane.add(optionWindow, "OptionWindow");
        contentPane.add(gamewindow, "GameWindow");
        contentPane.add(menuWindow, "MenuWindow");
        contentPane.add(fileWindow, "FileWindow");
        setContentPane(contentPane);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if(System.getProperty("os.name").equals("Mac OS X")) {
            retina = (isRetina())?  true : false;
            Fullscreen();
            requestToggleFullScreen(this);
            setVisible(true);
        }
        else if (gd.isFullScreenSupported()) {
            dispose();
            setUndecorated(true);
            setExtendedState(MAXIMIZED_BOTH);
            setVisible(true);
        } else {
            System.err.println("Full screen not supported");
            setBounds(0,0,screenSize.width, screenSize.height);
            setVisible(true);
        }

        card.show(contentPane, "MenuWindow");
    }

    private class GameWindow extends DynamicBackground{

        JScrollPane scrollPane;
        JTextPane textPane;
        JPanel optionPane;
        JPanel optionHolder;

        JPanel topFiller;
        JPanel midFiller;

        public GameWindow(){

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(100, 100, 100, 100));

            scrollPane = new JScrollPane();
            scrollPane.getVerticalScrollBar().setUnitIncrement(30);
            scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(null);
            scrollPane.setHorizontalScrollBar(null);
            add(scrollPane, BorderLayout.CENTER);
            scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "MENU");
            scrollPane.getActionMap().put("MENU", new callInGameMenu());

            ImageButton hide = new ImageButton("Hide");
            hide.addMouseListener(new MouseAdapter() {
                boolean toggle = false;
                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    if(toggle){
                        scrollPane.setVisible(true);
                        toggle = false;
                    }
                    else{
                        scrollPane.setVisible(false);
                        toggle = true;
                    }
                }
            });
            theme.registerButton(hide);
            hide.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(hide);

            JPanel panel = new JPanel(){
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(getBackground());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    //super.paintComponent(g);
                }
            };
            panel.setBackground(new Color(31, 31, 31, 184));
            panel.setOpaque(false);
            panel.addMouseListener(new MouseAdapter() {
                boolean toggle = false;

                @Override
                public void mouseClicked(MouseEvent e) {
                    e.consume();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    e.consume();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    e.consume();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    e.consume();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    e.consume();
                }
            });
            panel.setBorder(null);
            scrollPane.setViewportView(panel);
            panel.setLayout(new FormLayout(new ColumnSpec[]{
                    ColumnSpec.decode("min(50dlu;default):grow"),},
                    new RowSpec[]{
                            RowSpec.decode("default:grow"),
                            RowSpec.decode("default:grow"),
                            RowSpec.decode("20dlu"),
                            RowSpec.decode("default:grow"),
                            RowSpec.decode("20dlu"),}));


            textPane = new JTextPane();
            Caret caret = textPane.getCaret();
            caret.deinstall(textPane);
            theme.registerSmallerText(textPane);
            textPane.setFocusable(false);
            textPane.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    e.consume();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    e.consume();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    e.consume();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    e.consume();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    e.consume();
                }
            });
            textPane.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent e) {
                    scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                }
            });
            textPane.setEditable(false);
            textPane.setDisabledTextColor(textPane.getForeground());
            textPane.setEnabled(false);
            textPane.setOpaque(false);
            textPane.setMargin(new Insets(50, 50, 50, 50));
            panel.add(textPane, "1, 2, fill, fill");

            midFiller = new JPanel();
            midFiller.addMouseListener(new MouseAdapter() {
                boolean toggle = false;
                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    if(toggle) {
                        toggle = false;
                        panel.setBackground(new Color(31, 31, 31, 184));
                    }
                    else {
                        toggle = true;
                        panel.setBackground(new Color(31, 31, 31, 33));
                    }
                }
            });
            midFiller.add(Box.createRigidArea(new Dimension(Integer.MAX_VALUE, 30)));
            midFiller.setBackground(theme.getSecondaryColor());
            panel.add(midFiller, "1,3,fill, fill");

            optionHolder = new JPanel();
            optionHolder.setOpaque(false);
            optionHolder.setLayout(new BoxLayout(optionHolder, BoxLayout.X_AXIS));
            panel.add(optionHolder, "1, 4, fill, fill");

            optionHolder.add(Box.createRigidArea(new Dimension(50, 50)));

            optionPane = new JPanel();
            optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.Y_AXIS));
            optionPane.setOpaque(false);
            optionHolder.add(optionPane);

            topFiller = new JPanel();
            topFiller.add(Box.createRigidArea(new Dimension(Integer.MAX_VALUE, 30)));
            topFiller.setBackground(theme.getSecondaryColor());
            panel.add(topFiller, "1,5,fill, fill");

            optionHolder.add(Box.createRigidArea(new Dimension(50, 50)));

        }

        private class callInGameMenu extends AbstractAction{

            @Override
            public void actionPerformed(ActionEvent e) {

                if(!TextEngine.isRunning()){
                    inGameMenuWindow.setVisible(true);
                }
            }
        }

        @Override
        public void setVisible(boolean t){
            super.setVisible(t);
            if(t){
                gamestate=true;
            }
            else {
                gamestate=false;
            }
        }

        /** return components **/

        public void scrollToBottom(){
            scrollPane.getViewport().setViewPosition(new Point(0, scrollPane.getHeight()));
        }

        public void hideComponent(){
            topFiller.setVisible(false);
            midFiller.setVisible(false);
        }

        public void showComponent(){
            topFiller.setVisible(true);
            midFiller.setVisible(true);
        }

        public JTextPane returnTextPane(){
            return textPane;
        }

        public JPanel returnOptionPane(){
            return optionPane;
        }

        public JPanel returnOptionHolder(){
            return optionHolder;
        }
    }

    private class OptionWindow extends JPanel{

        int speed = 50;
        ImageButton returnBttn;
        JSlider slider;

        ImageButton sizeSmall;
        ImageButton sizeNormal;
        ImageButton sizeLarge;

        public OptionWindow(){

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            add(Box.createRigidArea(new Dimension(50, 200)));

            JPanel optionHolders = new JPanel(){
                @Override
                public void paintComponent(Graphics g) {
                    g.setColor(new Color(34, 31, 31, 240));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            optionHolders.setLayout(new BoxLayout(optionHolders, BoxLayout.Y_AXIS));
            optionHolders.setOpaque(false);
            optionHolders.setMaximumSize(new Dimension((int) (screenSize.width * .8), screenSize.height / 2));
            add(optionHolders);

            optionHolders.add(Box.createRigidArea(new Dimension(50, 60)));

            JPanel fontHolder = new JPanel();
            fontHolder.setBackground(theme.getPrimaryColor());
            fontHolder.setBorder(BorderFactory.createLineBorder(theme.getSecondaryColor(), 5));
            fontHolder.setAlignmentX(Component.CENTER_ALIGNMENT);
            /**optionHolders.add(fontHolder);**/
            fontHolder.setLayout(new BoxLayout(fontHolder, BoxLayout.X_AXIS));

            fontHolder.add(Box.createRigidArea(new Dimension(20, 50)));

            JLabel fontLbl = new JLabel("Font Style:");
            theme.registerText(fontLbl);
            fontLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            fontHolder.add(fontLbl);

            fontHolder.add(Box.createRigidArea(new Dimension(10, 10)));

            ImageButton fontOne = new ImageButton("Font One");
            theme.registerButton(fontOne);
            fontOne.setFont(new Font("SansSerif", Font.PLAIN, 20).deriveFont(15f));
            fontHolder.add(fontOne);

            fontHolder.add(Box.createRigidArea(new Dimension(20, 20)));

            ImageButton fontTwo = new ImageButton("Font Two");
            theme.registerButton(fontTwo);
            fontTwo.setFont(new Font("Monospaced", Font.PLAIN, 20).deriveFont(15f));
            fontHolder.add(fontTwo);

            fontHolder.add(Box.createRigidArea(new Dimension(20, 20)));

            ImageButton fontThree = new ImageButton("Font Three");
            theme.registerButton(fontThree);
            fontThree.setFont(new Font("Serif", Font.PLAIN, 20).deriveFont(15f));
            fontHolder.add(fontThree);

            fontHolder.add(Box.createRigidArea(new Dimension(20, 50)));

            optionHolders.add(Box.createRigidArea(new Dimension(50, 30)));

            JPanel sizeHolder = new JPanel();
            sizeHolder.setBackground(theme.getPrimaryColor());
            sizeHolder.setBorder(BorderFactory.createLineBorder(theme.getSecondaryColor(), 5));
            optionHolders.add(sizeHolder);
            sizeHolder.setLayout(new BoxLayout(sizeHolder, BoxLayout.X_AXIS));

            sizeHolder.add(Box.createRigidArea(new Dimension(20, 50)));

            JLabel sizeLbl = new JLabel("Font Size:");
            theme.registerText(sizeLbl);
            sizeLbl.setAlignmentX(0.5f);
            sizeHolder.add(sizeLbl);

            sizeHolder.add(Box.createRigidArea(new Dimension(10, 10)));

            sizeSmall = new ImageButton("Small Size");
            sizeSmall.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getTextPane().setFont(theme.getDefaultFont().deriveFont(15f));
                    TextEngine.database.setSize("small");
                    sizeSmall.select();
                    sizeNormal.deselect();
                    sizeLarge.deselect();
                }
            });
            theme.registerButton(sizeSmall);
            sizeHolder.add(sizeSmall);

            sizeHolder.add(Box.createRigidArea(new Dimension(20, 20)));

            sizeNormal = new ImageButton("Normal Size");
            sizeNormal.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getTextPane().setFont(theme.getDefaultFont().deriveFont(20f));
                    TextEngine.database.setSize("medium");
                    sizeSmall.deselect();
                    sizeNormal.select();
                    sizeLarge.deselect();
                }
            });
            theme.registerButton(sizeNormal);
            sizeHolder.add(sizeNormal);

            sizeHolder.add(Box.createRigidArea(new Dimension(20, 20)));

            sizeLarge = new ImageButton("Large Size");
            sizeLarge.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getTextPane().setFont(theme.getDefaultFont().deriveFont(30f));
                    TextEngine.database.setSize("large");
                    sizeSmall.deselect();
                    sizeNormal.deselect();
                    sizeLarge.select();
                }
            });
            theme.registerButton(sizeLarge);
            sizeHolder.add(sizeLarge);

            sizeHolder.add(Box.createRigidArea(new Dimension(20, 50)));

            optionHolders.add(Box.createRigidArea(new Dimension(50, 30)));

            JPanel speedHolder = new JPanel();
            speedHolder.setBackground(theme.getPrimaryColor());
            speedHolder.setBorder(BorderFactory.createLineBorder(theme.getSecondaryColor(), 5));
            optionHolders.add(speedHolder);
            speedHolder.setLayout(new BoxLayout(speedHolder, BoxLayout.X_AXIS));

            speedHolder.add(Box.createRigidArea(new Dimension(20, 50)));

            JLabel speedLbl = new JLabel("Read Speed:");
            theme.registerText(speedLbl);
            speedLbl.setAlignmentX(0.5f);
            speedHolder.add(speedLbl);

            speedHolder.add(Box.createRigidArea(new Dimension(10, 10)));

            slider = new JSlider();
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    speed = slider.getValue();
                    TextEngine.database.setSpeed(speed);
                }
            });
            slider.setMajorTickSpacing(10);
            slider.setMinorTickSpacing(5);
            slider.setPaintTicks(true);
            slider.setSnapToTicks(true);
            slider.setMaximumSize(new Dimension(400, 50));
            speedHolder.add(slider);

            Hashtable labelTable = new Hashtable();
            JLabel slow = new JLabel("Slow");
            theme.registerSmallerText(slow);
            JLabel normal = new JLabel("Normal");
            theme.registerSmallerText(normal);
            JLabel fast = new JLabel("Fast");
            theme.registerSmallerText(fast);
            labelTable.put(new Integer(0), slow);
            labelTable.put(new Integer(50), normal);
            labelTable.put(new Integer(100), fast);
            slider.setLabelTable(labelTable);
            slider.setPaintLabels(true);

            speedHolder.add(Box.createRigidArea(new Dimension(20, 50)));

            optionHolders.add(Box.createRigidArea(new Dimension(50, 30)));

            returnBttn = new ImageButton("Back to Menu");
            returnBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            returnBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    card.show(contentPane, "MenuWindow");
                }
            });
            theme.registerButton(returnBttn);
            optionHolders.add(returnBttn);

            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "MENU");
            getActionMap().put("MENU", new goBacktoMenu());

            String size = TextEngine.database.getSize();

            switch(size){
                case "small" :
                    sizeSmall.doClick();
                    sizeNormal.deselect();
                    sizeLarge.deselect();
                    break;
                case "medium":
                    sizeSmall.deselect();
                    sizeNormal.doClick();
                    sizeLarge.deselect();
                    break;
                case "large" :
                    sizeSmall.deselect();
                    sizeNormal.deselect();
                    sizeLarge.doClick();
                    break;
            }
            repaint();

        }
        String dest;

        @Override
        public void setVisible(boolean k) {

            ActionListener[] listeners = returnBttn.getActionListeners();
            for(ActionListener temp : listeners){
                returnBttn.removeActionListener(temp);
            }

            System.out.println(current);
            if(current.equals("GameWindow")){
                dest = "GameWindow";
                returnBttn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        card.show(contentPane, dest);
                    }
                });
                returnBttn.setText("Back to Game");
            }
            else {
                dest = "MenuWindow";
                returnBttn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        card.show(contentPane, dest);
                    }
                });
                returnBttn.setText("Back to Menu");
            }

            String size = TextEngine.database.getSize();

            switch(size){
                case "small" :
                    sizeSmall.doClick();
                    sizeNormal.deselect();
                    sizeLarge.deselect();
                    break;
                case "medium":
                    sizeSmall.deselect();
                    sizeNormal.doClick();
                    sizeLarge.deselect();
                    break;
                case "large" :
                    sizeSmall.deselect();
                    sizeNormal.deselect();
                    sizeLarge.doClick();
                    break;
            }

            slider.setValue(TextEngine.database.getSpeed());

            super.setVisible(k);
        }

        private class goBacktoMenu extends AbstractAction{

            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(contentPane,dest);
            }
        }

        public int getSpeed(){
            return 105-speed;
        }

        public void setSpeed(int i){
            slider.setValue(i);
        }

    }

    private class FileWindow extends JPanel{

        ArrayList<ImageButton> list = new ArrayList<ImageButton>();

        public FileWindow(){

            setOpaque(false);
            setBorder(new EmptyBorder(5, 5, 5, 5));
            setLayout(new FormLayout(new ColumnSpec[]{
                    ColumnSpec.decode("default:grow"),},
                    new RowSpec[]{
                            RowSpec.decode("default:grow"),
                            FormFactory.RELATED_GAP_ROWSPEC,
                            RowSpec.decode("default:grow(3)"),
                            FormFactory.RELATED_GAP_ROWSPEC,
                            FormFactory.DEFAULT_ROWSPEC,
                            FormFactory.RELATED_GAP_ROWSPEC,
                            RowSpec.decode("default:grow"),
                            FormFactory.RELATED_GAP_ROWSPEC,
                            FormFactory.DEFAULT_ROWSPEC,}));

            JPanel topFiller = new JPanel();
            topFiller.setOpaque(false);
            add(topFiller, "1, 1, fill, fill");

            JPanel holder = new JPanel();
            holder.setOpaque(false);
            holder.setLayout(new BoxLayout(holder, BoxLayout.X_AXIS));
            holder.add(Box.createRigidArea(new Dimension(20, 20)));
            add(holder, "1, 3, fill, fill");

            JPanel buttonHolder = new JPanel();
            buttonHolder.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
            buttonHolder.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonHolder.setOpaque(false);

            add(buttonHolder, "1, 7, fill, fill");

            ImageButton createBttn = new ImageButton("Create");
            createBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialogWindow.setVisible(true);
                }
            });
            createBttn.setPreferredSize(new Dimension(screenSize.width / 6, screenSize.width / 20));
            theme.registerButton(createBttn);
            buttonHolder.add(createBttn);

            buttonHolder.add(Box.createRigidArea(new Dimension(30, 20)));

            ImageButton loadBttn = new ImageButton("Load");
            loadBttn.setEnabled(false);
            loadBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    card.show(contentPane, "GameWindow");
                    TextEngine.startGame(list.get(0).getText(), TextEngine.database.getSaveData(TextEngine.database.getSaveData(list.get(0).getText())));
                }
            });
            loadBttn.setPreferredSize(new Dimension(screenSize.width / 6, screenSize.width / 20));
            theme.registerButton(loadBttn);
            buttonHolder.add(loadBttn);

            buttonHolder.add(Box.createRigidArea(new Dimension(30, 20)));

            ImageButton deleteBttn = new ImageButton("Delete");
            deleteBttn.setEnabled(false);
            deleteBttn.setPreferredSize(new Dimension(screenSize.width / 6, screenSize.width / 20));
            theme.registerButton(deleteBttn);
            buttonHolder.add(deleteBttn);

            buttonHolder.add(Box.createRigidArea(new Dimension(30, 20)));

            ImageButton cancelBttn = new ImageButton("Cancel");
            cancelBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TextEngine.database.initializeData();
                    card.show(contentPane, "MenuWindow");
                }
            });
            cancelBttn.setPreferredSize(new Dimension(screenSize.width / 6, screenSize.width / 20));
            theme.registerButton(cancelBttn);
            buttonHolder.add(cancelBttn);

            list.add(null);

            JPanel FileOne = new JPanel();
            FileOne.setOpaque(false);
            FileOne.setForeground(Color.GRAY);
            FileOne.setLayout(new BorderLayout());
            ImageButton FileOneBttn = new ImageButton();
            theme.registerButton(FileOneBttn);
            redoButton(FileOneBttn, 0);
            FileOneBttn.addMouseListener(new MouseAdapter() {
                boolean entered = false;

                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    if (entered) {
                        if (FileOneBttn.isEnabled()) {
                            FileOneBttn.select();
                            loadBttn.setEnabled(true);
                            deleteBttn.setEnabled(true);
                            list.set(0, FileOneBttn);
                            for (ImageButton temp : list) {
                                if (temp != null && !temp.equals(FileOneBttn)) temp.deselect();
                            }
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    entered = true;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    entered = false;
                    if (FileOneBttn.isPressed()) FileOneBttn.select();
                }
            });
            FileOne.add(FileOneBttn);
            holder.add(FileOne);

            holder.add(Box.createRigidArea(new Dimension(20, 20)));

            JPanel FileTwo = new JPanel();
            FileTwo.setOpaque(false);
            FileTwo.setForeground(Color.GRAY);
            FileTwo.setLayout(new BorderLayout());
            ImageButton FileTwoBttn = new ImageButton();
            theme.registerButton(FileTwoBttn);
            redoButton(FileTwoBttn, 1);
            FileTwoBttn.addMouseListener(new MouseAdapter() {
                boolean entered = false;

                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    if (entered) {
                        if (FileTwoBttn.isEnabled()) {
                            FileTwoBttn.select();
                            loadBttn.setEnabled(true);
                            deleteBttn.setEnabled(true);
                            list.set(0, FileTwoBttn);
                            for (ImageButton temp : list) {
                                if (temp != null && !temp.equals(FileTwoBttn)) temp.deselect();
                            }
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    entered = true;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    entered = false;
                    if (FileTwoBttn.isPressed()) FileTwoBttn.select();
                }
            });
            FileTwo.add(FileTwoBttn);
            holder.add(FileTwo);

            holder.add(Box.createRigidArea(new Dimension(20, 20)));

            JPanel FileThree = new JPanel();
            FileThree.setOpaque(false);
            FileThree.setForeground(Color.GRAY);
            FileThree.setLayout(new BorderLayout());
            ImageButton FileThreeBttn = new ImageButton();
            redoButton(FileThreeBttn, 2);
            theme.registerButton(FileThreeBttn);
            FileThreeBttn.addMouseListener(new MouseAdapter() {
                boolean entered = false;

                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    if (entered) {
                        if (FileThreeBttn.isEnabled()) {
                            FileThreeBttn.select();
                            loadBttn.setEnabled(true);
                            deleteBttn.setEnabled(true);
                            list.set(0, FileThreeBttn);
                            for (ImageButton temp : list) {
                                if (temp != null && !temp.equals(FileThreeBttn)) temp.deselect();
                            }
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    entered = true;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    entered = false;
                    if (FileThreeBttn.isPressed()) FileThreeBttn.select();
                }
            });
            FileThree.add(FileThreeBttn);
            holder.add(FileThree);

            holder.add(Box.createRigidArea(new Dimension(20, 20)));

            holder.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    redoButton(FileOneBttn, 0);
                    redoButton(FileTwoBttn, 1);
                    redoButton(FileThreeBttn, 2);
                }
            });
            deleteBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    File temp = TextEngine.database.getSaveData(list.get(0).getText());
                    if (temp.equals(TextEngine.database.getRecent())) TextEngine.database.setRecent("NULL");
                    TextEngine.database.getSaveData(list.get(0).getText()).delete();
                    TextEngine.database.initializeData();
                    redoButton(FileOneBttn, 0);
                    redoButton(FileTwoBttn, 1);
                    redoButton(FileThreeBttn, 2);
                    loadBttn.setEnabled(false);
                    deleteBttn.setEnabled(false);
                    list.set(0,null);
                    holder.revalidate();
                    holder.repaint();
                }
            });

            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "MENU");
            getActionMap().put("MENU", new goBacktoMenu());
        }

        private class goBacktoMenu extends AbstractAction{

            @Override
            public void actionPerformed(ActionEvent e) {
                card.show(contentPane,"MenuWindow");
            }
        }

        public void redoButton(ImageButton temp, int i){
            if (TextEngine.database.getSaves() > i) {
                temp.setText(TextEngine.database.getSaveData(i).getName());
                temp.deselect();
                if(!list.contains(temp))list.add(temp);
            } else {
                temp.setText(String.valueOf("EMPTY"));
                temp.select();
                if(list.contains(temp)) list.remove(temp);
            }
            temp.repaint();
        }
    }

    private class MenuWindow extends JPanel{

        ImageButton continueBttn;

        public MenuWindow(){

            setOpaque(false);
            setLayout(new FormLayout(new ColumnSpec[]{
                    ColumnSpec.decode("default:grow"),
                    FormFactory.RELATED_GAP_COLSPEC,
                    ColumnSpec.decode("default:grow(3)"),
                    FormFactory.RELATED_GAP_COLSPEC,
                    ColumnSpec.decode("default:grow(15)"),},
                    new RowSpec[]{
                            RowSpec.decode("default:grow"),
                            FormFactory.RELATED_GAP_ROWSPEC,
                            RowSpec.decode("default:grow"),
                            FormFactory.RELATED_GAP_ROWSPEC,
                            RowSpec.decode("default:grow"),}));

            JPanel bttnHolder = new JPanel(){
                @Override
                protected void paintComponent(Graphics g)
                {
                    g.setColor(new Color(0, 0, 0, 192));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

            };
            bttnHolder.setOpaque(false);
            add(bttnHolder, "3, 3, fill, fill");
            bttnHolder.setLayout(new BoxLayout(bttnHolder, BoxLayout.Y_AXIS));

            bttnHolder.add(Box.createRigidArea(new Dimension(screenSize.height / 50, screenSize.height / 50)));

            continueBttn = new ImageButton("Continue");
            continueBttn.setMaximumSize(new Dimension(screenSize.width / 5, screenSize.width / 30));
            continueBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            continueBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    card.show(contentPane, "GameWindow");
                    TextEngine.startGame(TextEngine.database.getRecent().getName(), TextEngine.database.getSaveData(TextEngine.database.getRecent()));
                }
            });
            theme.registerButton(continueBttn);
            bttnHolder.add(continueBttn);

            bttnHolder.add(Box.createRigidArea(new Dimension(screenSize.height / 50, screenSize.height / 50)));

            ImageButton gameFileBttn = new ImageButton("Game Files");
            gameFileBttn.setMaximumSize(new Dimension(screenSize.width / 5, screenSize.width / 30));
            gameFileBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            gameFileBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    card.show(contentPane, "FileWindow");
                }
            });
            theme.registerButton(gameFileBttn);
            bttnHolder.add(gameFileBttn);

            bttnHolder.add(Box.createRigidArea(new Dimension(screenSize.height / 50, screenSize.height / 50)));

            ImageButton optionBttn = new ImageButton("Option");
            optionBttn.setMaximumSize(new Dimension(screenSize.width / 5, screenSize.width / 30));
            optionBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            optionBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    card.show(contentPane, "OptionWindow");
                }
            });
            theme.registerButton(optionBttn);
            bttnHolder.add(optionBttn);

            bttnHolder.add(Box.createRigidArea(new Dimension(screenSize.height / 50, screenSize.height / 50)));

            ImageButton quitBttn = new ImageButton("Quit");
            quitBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            quitBttn.setMaximumSize(new Dimension(screenSize.width / 5, screenSize.width / 30));
            quitBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            theme.registerButton(quitBttn);
            bttnHolder.add(quitBttn);

        }

        @Override
        public void setVisible(boolean k){
            super.setVisible(k);
            if (TextEngine.database.getRecent()!=null) continueBttn.setEnabled(true);
            else continueBttn.setEnabled(false);
        }

    }

    private class InGameMenuWindow extends JDialog{

        JLabel title;

        public InGameMenuWindow(){

            setModal(true);
            setUndecorated(true);
            setBounds(screenSize.width / 4, (screenSize.height / 10)*3, screenSize.width / 2, (screenSize.height / 5)
                    *3);
            setLayout(new BorderLayout());

            JPanel dialogPane = new JPanel();
            dialogPane.setBorder(BorderFactory.createLineBorder(new Color(73, 73, 73), 50));
            dialogPane.setBackground(new Color(73, 73, 73));
            dialogPane.setLayout(new BoxLayout(dialogPane, BoxLayout.Y_AXIS));
            add(dialogPane);

            dialogPane.add(Box.createRigidArea(new Dimension(20, 20)));

            title = new JLabel("EMPTY",JLabel.CENTER);
            theme.registerText(title);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            title.setMaximumSize(new Dimension(getWidth() / 2, getWidth() / 10));
            dialogPane.add(title);

            dialogPane.add(Box.createRigidArea(new Dimension(20, 20)));

            ImageButton continueBttn = new ImageButton("Continue");
            continueBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            continueBttn.setMaximumSize(new Dimension(getWidth() / 2, getWidth() / 10));
            theme.registerButton(continueBttn);
            continueBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });
            dialogPane.add(continueBttn);

            dialogPane.add(Box.createRigidArea(new Dimension(20, 20)));

            ImageButton statBttn = new ImageButton("Debug");
            statBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            statBttn.setMaximumSize(new Dimension(getWidth() / 2, getWidth() / 10));
            theme.registerButton(statBttn);
            statBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ArrayList<String> stats = TextEngine.compiler.getVar();
                    for(String k : stats){
                        //System.out.println(k);
                    }
                    Debugger.showGUI(true);
                    setVisible(false);
                }
            });
            dialogPane.add(statBttn);

            dialogPane.add(Box.createRigidArea(new Dimension(20, 20)));

            ImageButton menuBttn = new ImageButton("Menu");
            menuBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    TextEngine.database.initializeData();
                    card.show(contentPane, "MenuWindow");
                }
            });
            menuBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuBttn.setMaximumSize(new Dimension(getWidth() / 2, getWidth() / 10));
            theme.registerButton(menuBttn);
            dialogPane.add(menuBttn);

            dialogPane.add(Box.createRigidArea(new Dimension(20, 20)));

            ImageButton optionBttn = new ImageButton("Option");
            optionBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    card.show(contentPane, "OptionWindow");
                }
            });
            optionBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            optionBttn.setMaximumSize(new Dimension(getWidth() / 2, getWidth() / 10));
            theme.registerButton(optionBttn);
            dialogPane.add(optionBttn);

            dialogPane.add(Box.createRigidArea(new Dimension(20, 20)));

            ImageButton restartBttn = new ImageButton("Restart");
            restartBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int n = JOptionPane.showConfirmDialog(
                            dialogPane,
                            "Would you like to Restart the Game?", "Restart",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (n == JOptionPane.YES_OPTION) {
                        TextEngine.restartGame();
                        setVisible(false);
                    }
                }
            });
            restartBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            restartBttn.setMaximumSize(new Dimension(getWidth() / 2, getWidth() / 10));
            theme.registerButton(restartBttn);
            dialogPane.add(restartBttn);

            dialogPane.add(Box.createRigidArea(new Dimension(20, 20)));

            ImageButton exitBttn = new ImageButton("Quit");
            exitBttn.setAlignmentX(Component.CENTER_ALIGNMENT);
            exitBttn.setMaximumSize(new Dimension(getWidth() / 2, getWidth() / 10));
            theme.registerButton(exitBttn);
            exitBttn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            dialogPane.add(exitBttn);

            dialogPane.add(Box.createRigidArea(new Dimension(20, 20)));

            setBounds(screenSize.width / 2 - dialogPane.getPreferredSize().width, screenSize.height / 2 - dialogPane
                            .getPreferredSize().height / 2,
                    dialogPane.getPreferredSize().width * 2, dialogPane.getPreferredSize().height);

            dialogPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
                            0),
                    "MENU");
            dialogPane.getActionMap().put("MENU", new closeDialog());
        }

        private class closeDialog extends AbstractAction{

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }

        @Override
        public void setVisible(boolean k){
            title.setText(TextEngine.getCurrentSave());
            if(k)glass.setVisible(true);
            else glass.setVisible(false);
            super.setVisible(k);
            title.repaint();
        }

    }

    private class DialogWindow extends JDialog{

        JTextField text;

        public DialogWindow(){
            setModal(true);
            setUndecorated(true);
            setBounds(screenSize.width / 2 - screenSize.width / 8, screenSize.height / 2 - screenSize.width / 16, screenSize.width / 3, screenSize.width / 7);

            JPanel dialogPane = new JPanel();
            dialogPane.setBackground(theme.getPrimaryColor());
            dialogPane.setBounds(screenSize.width / 2 - screenSize.width / 8, screenSize.height / 2 - screenSize.width / 16, screenSize.width / 3, screenSize.width / 7);
            dialogPane.setLayout(null);
            add(dialogPane);

            ImageButton approve = new ImageButton();
            theme.registerButton(approve);
            approve.setEnabled(false);
            approve.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = text.getText();
                    text.setText("");
                    text.repaint();
                    setVisible(false);
                    card.show(contentPane, "GameWindow");
                    TextEngine.startGame(name);
                }
            });
            approve.setText("OK");
            approve.setBounds(dialogPane.getWidth() / 7, dialogPane.getHeight() - dialogPane.getHeight() / 3, dialogPane.getWidth() / 3, dialogPane.getHeight() / 5);
            approve.setBackground(Color.RED);
            dialogPane.add(approve);

            JPanel textHolder = new JPanel();
            textHolder.setBounds(dialogPane.getWidth() / 10, (dialogPane.getHeight() / 8) * 3, dialogPane.getWidth() - dialogPane.getWidth() / 5, dialogPane.getHeight() / 5);
            textHolder.setBackground(Color.WHITE);
            textHolder.setOpaque(false);
            dialogPane.add(textHolder);
            textHolder.setLayout(new BoxLayout(textHolder, BoxLayout.LINE_AXIS));
            text = new JTextField();
            theme.registerSmallerText(text);
            text.setForeground(Color.BLACK);
            text.setHorizontalAlignment(JTextField.CENTER);
            text.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    super.keyReleased(e);
                    if(text.getText()==""){
                        approve.setEnabled(false);
                    }
                    else{
                        approve.setEnabled(true);
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            e.consume();
                            approve.doClick();
                        }
                    }
                }
            });
            textHolder.add(text);

            approve.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            });

            JLabel title = new JLabel("Name Game File",JLabel.CENTER);
            theme.registerText(title);
            title.setBounds(0, dialogPane.getY() / 15, dialogPane.getWidth(), dialogPane.getHeight() / 7);
            title.setBackground(Color.ORANGE);
            dialogPane.add(title);

            title.setText("Game Name");

            ImageButton cancel = new ImageButton();
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    text.setText("");
                    text.repaint();
                    setVisible(false);
                }
            });
            theme.registerButton(cancel);
            cancel.setText("Cancel");
            cancel.setBounds(dialogPane.getWidth() - dialogPane.getWidth() / 7 - dialogPane.getWidth() / 3, dialogPane.getHeight() - dialogPane.getHeight() / 3, dialogPane.getWidth() / 3, dialogPane.getHeight() / 5);
            cancel.setBackground(Color.RED);
            dialogPane.add(cancel);

            dialogPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
                            0),
                    "MENU");
            dialogPane.getActionMap().put("MENU", new closeDialog());
        }

        private class closeDialog extends AbstractAction{

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        }
    }

    /** Getters **/

    public DynamicBackground getArtWork(){
        return gamewindow;
    }

    public JTextPane getTextPane(){
        return gamewindow.returnTextPane();
    }

    public JPanel getOptionPane(){
        return gamewindow.returnOptionPane();
    }

    public JPanel getOptionHolder(){
        return gamewindow.returnOptionHolder();
    }

    public Theme getTheme(){
        return theme;
    }

    public void scrollToBottom(){
        gamewindow.scrollToBottom();
    }

    public void hideGameOptions(){
        gamewindow.hideComponent();
    }

    public void showGameOptions(){
        gamewindow.showComponent();
    }

    public boolean getGameState(){
        return gamestate;
    }

    public int getSpeed(){

        return optionWindow.getSpeed();
    }

    public void setSpeed(int i){
        optionWindow.setSpeed(i);
    }

    /** Shortcuts**/



    /** Screen display settings **/

    private void Fullscreen() {
        System.setProperty("apple.awt.fullscreenusefade", "true");
        enableOSXFullscreen(this);
        setVisible(true);
    }

    private void enableOSXFullscreen(Window window) {
        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class params[] = new Class[]{Window.class, Boolean.TYPE};
            Method method = util.getMethod("setWindowCanFullScreen", params);
            method.invoke(util, window, true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void requestToggleFullScreen(Window window) {
        try {
            Class appClass = Class.forName("com.apple.eawt.Application");
            Class params[] = new Class[]{};

            Method getApplication = appClass.getMethod("getApplication", params);
            Object application = getApplication.invoke(appClass);
            Method requestToggleFulLScreen = application.getClass().getMethod("requestToggleFullScreen", Window.class);

            requestToggleFulLScreen.invoke(application, window);
        } catch (Exception e) {
            System.out.println("An exception occurred while trying to toggle full screen mode");
        }
    }

    public boolean isRetina() {

        boolean isRetina = false;

        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        try {
            Field field = graphicsDevice.getClass().getDeclaredField("scale");
            if (field != null) {
                field.setAccessible(true);
                if((field.get(graphicsDevice).toString().equals("2"))) {
                    isRetina = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isRetina;
    }
}