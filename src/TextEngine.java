import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class TextEngine{

    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    static private boolean finishedGame = false;

    private static String currentSave;
    private static int currentLine;

    static Database database;
    static Compiler compiler;
    static GUI gui;
    static TextRenderer textRenderer;
    static OptionRenderer optionRenderer;
    static ThemeRenderer themerenderer;
    static String scriptAddress;
    
    /**
     * Launch the application.
     */

    public static String getScriptAddress(){
        return scriptAddress;
    }
    
    public static void main(String[] args) {
        if(args.length>0){
            String path ="";
            for(String temp : args){
                path +=" " + temp;
            }
            path = path.trim();
            System.out.println("1: " + path);
            compiler = new Compiler(path);
            scriptAddress = new File(path).getParentFile().getAbsolutePath();
        }
        else compiler = new Compiler("Script/showcase.txt");
        database = new Database();
        gui = new GUI();
        textRenderer = new TextRenderer(gui.getTextPane());
        optionRenderer = new OptionRenderer(gui.getOptionPane(),gui.getOptionHolder());
        themerenderer = new ThemeRenderer();
        Debugger.initialize();
        Debugger.log("STARTING: " + getGameName() +" and OPENING: " + currentSave);
    }

    public static String getCurrentSave(){
        return currentSave;
    }

    public static int getCurrentLine(){
        return currentLine;
    }

    public static void save(){
        ArrayList<String> variables = compiler.getVar();
        database.save(currentSave,variables, currentLine-1);
    }

    public static String getGameName(){
        return compiler.getGameName();
    }

    /**
     * Progress game.
     */

    public static void startGame(String game){
        currentSave = game;
        currentLine = compiler.getFirstBlock();
        database.setRecent(currentSave);
        TextEngine.database.saveSystemFiles();

        runGame();

    }

    public static void startGame(String game, ArrayList<String> var) {
        clear();
        int pos = Integer.parseInt(var.get(0));
        if(pos!=-2){
            var.remove(0);
            compiler.setVar(var);
            currentLine = pos;
            currentSave = game;
            database.setRecent(currentSave);
            TextEngine.database.saveSystemFiles();

            runGame();
        }
        else{
            startGame(game);
        }
    }

    public static void restartGame(){
        compiler.setVar(null);
        currentLine=compiler.getFirstBlock();
        runGame();
    }

    public static void runGame(){
        if(!finishedGame) compiler.read(currentLine);
    }

    public static void finishGame() {
        finishedGame = true;
        clear();
        renderText("Thanks for playing *" + compiler.getGameName() + "*!");
        addOption("Back to Menu", -2);
        renderOption();
    }

    public static void forward(int i) {
        currentLine = i;
        runGame();
    }

    public static void forward() {
        currentLine++;
        runGame();
    }

    public static void clear(){
        textRenderer.clear();
        optionRenderer.clear();
    }

    public static void artUpdate(String update){
        themerenderer.update(update);
    }

    public static boolean isRunning(){
        return textRenderer.running();
    }

    /**
     * Call Renderers.
     */

    public static void renderText(String text){
        Debugger.log("Rendering text: " + text);
        textRenderer.animateText(text);
        //textRenderer.showText(text);
    }

    public static void addDynamicOption(String text, int dest,double t){
        optionRenderer.addDynamicOption(text, dest, t);
    }

    public static void addOption(String text, int dest){
        optionRenderer.addOption(text, dest);
    }

    public static void addUserOption(int dest){
        optionRenderer.addUserOption(dest);
    }

    public static void renderOption(){
        Debugger.log("Rendering option");
        optionRenderer.showOptions();
        gui.scrollToBottom();
    }

    /** Renderers **/

    private static class TextRenderer{
        static JTextPane textPane;
        static Timer timer;
        static SimpleAttributeSet attr = new SimpleAttributeSet();
        static SimpleAttributeSet boldFont = new SimpleAttributeSet();
        static SimpleAttributeSet italicFont = new SimpleAttributeSet();

        int insertPos = 0;
        int readPos = 0;
        boolean paintAttr = false;


        public TextRenderer(JTextPane text){
            textPane = text;

            StyleConstants.setFontFamily(boldFont,gui.getTheme().getBoldFont().getName());
            StyleConstants.setBold(boldFont, true);
            StyleConstants.setItalic(italicFont, true);
        }

        public void clear() {

            insertPos = 0;
            readPos = 0;
            textPane.setText("");

        }

        public void showText(String text){
            final char[] characters = text.toCharArray();
            boolean specialChar = false;

            while(readPos!=text.length()||gui.getGameState()){
                if(readPos==text.length()||!gui.getGameState()) {
                    forward();
                    break;
                }
                else{
                    String temp = String.valueOf(characters[readPos]);
                    if(!temp.equals("*")||specialChar==true) {
                        if(temp.equals(".")||temp.equals(",")||temp.equals(";")||temp.equals("!")||temp.equals
                                ("?")||temp.equals(":")||temp.equals("-")){
                        }
                        if(specialChar){
                            specialChar=false;
                        }
                        if(temp.equals("\\")){
                            specialChar = true;
                            readPos++;
                        }
                        else{
                            insertText(insertPos, temp, attr);
                            insertPos++;
                            readPos++;
                        }
                    }
                    else if(specialChar==false){
                        if(readPos+1<text.length()&&String.valueOf(characters[readPos+1]).equals("*")){
                            insertAttribute("**");
                            readPos++;
                            readPos++;
                        }
                        else {
                            insertAttribute("*");
                            readPos++;
                        }
                    }
                }
            }
        }

        public void animateText(String text) {

            final char[] characters = text.toCharArray();
            if(timer!=null&&timer.isRunning()) {
                Debugger.log("TEXT OVERFLOW: " + text);
                System.exit(0);
            }
            timer = new Timer(gui.getSpeed(),new ActionListener(){
            boolean specialChar = false;
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if(readPos==text.length()||!gui.getGameState()) {
                        timer.stop();
                        forward();
                    }
                    else{
                        String temp = String.valueOf(characters[readPos]);
                        if(!temp.equals("*")||specialChar==true) {
                            if(temp.equals(".")||temp.equals(",")||temp.equals(";")||temp.equals("!")||temp.equals
                                    ("?")||temp.equals(":")||temp.equals("-")){
                                int k;
                                if(timer.getDelay()<270){
                                    k = 50;
                                }
                                else k = timer.getDelay();
                                timer.setDelay(150);
                            }
                            else{
                                timer.setDelay(gui.getSpeed());
                            }
                            if(specialChar){
                                specialChar=false;
                            }
                            if(temp.equals("\\")){
                                specialChar = true;
                                readPos++;
                            }
                            else{
                                insertText(insertPos, temp, attr);
                                insertPos++;
                                readPos++;
                            }
                        }
                        else if(specialChar==false){
                            if(readPos+1<text.length()&&String.valueOf(characters[readPos+1]).equals("*")){
                                insertAttribute("**");
                                readPos++;
                                readPos++;
                            }
                            else {
                                insertAttribute("*");
                                readPos++;
                            }
                        }
                    }
                }

            });
            timer.setInitialDelay(500);
            timer.start();
        }

        public boolean running(){
            if(timer==null) return false;
            return timer.isRunning();
        }

        public void insertAttribute(String k){
            if(k.equals("*")){
                if(paintAttr){
                    attr  = null;
                    paintAttr=false;
                } else if (!paintAttr){
                    attr = boldFont;
                    paintAttr=true;
                }
            }
            else if(k.equals("**")){
                if(paintAttr){
                    attr = null;
                    paintAttr=false;
                }
                else if(!paintAttr){
                    attr = italicFont;
                    paintAttr=true;
                }
            }
        }

        public void insertText(int pos, String c,SimpleAttributeSet attr){
            try {
                textPane.getStyledDocument().insertString(pos, c, attr);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private static class OptionRenderer {

        ArrayList<JComponent> options = new ArrayList<>();
        JPanel optionPane;
        JPanel parent;

        public void clear(){
            options.clear();
            optionPane.removeAll();
            gui.hideGameOptions();
            parent.setVisible(false);
        }

        public void addOption(String text,int dest) {
            options.add(new OptionButton(text, dest));
        }

        public void addDynamicOption(String text,int dest, double t) {
            options.add(new DynamicButton(text,dest,t));
        }

        public void addUserOption(int destination){
            options.add(new UserButton(destination));
        }

        public OptionRenderer(JPanel pane, JPanel p) {
            optionPane = pane;
            parent = p;
            p.setVisible(false);
        }

        public void showOptions() {

            optionPane.add(Box.createVerticalStrut(20));
            for(JComponent temp : options) {
                optionPane.add(temp);
                if(temp instanceof DynamicButton) {
                    DynamicButton k = (DynamicButton) temp;
                    k.startTime();
                }
                optionPane.add(Box.createVerticalStrut(20));
            }
            parent.revalidate();
            parent.setVisible(true);
            parent.repaint();
            gui.showGameOptions();

        }

        private class OptionButton extends ImageButton{
            private String id;
            private JTextPane buttonText;
            int destination;

            public OptionButton(String text, int dest){

                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setAlignmentX(CENTER_ALIGNMENT);

                destination = dest;
                id = text;

                add(Box.createRigidArea(new Dimension(40, 40)));

                buttonText = new JTextPane();
                buttonText.addMouseListener(new MouseAdapter() {
                    boolean exited;
                    boolean entered;

                    @Override
                    public void mouseExited(MouseEvent e) {
                        redispatchToParent(e);
                        exited = true;
                        entered = false;
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        redispatchToParent(e);
                        entered = true;
                        exited = false;
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        redispatchToParent(e);
                        if (entered && !exited) {
                            if (isEnabled()) {
                                if(destination == -2) {
                                    currentLine = -1;
                                    compiler.setVar(null);
                                    save();
                                    gui.card.show(gui.contentPane,"MenuWindow");
                                    finishedGame=false;
                                }
                                else compiler.userAction(destination);
                            }
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        redispatchToParent(e);
                    }

                    private void redispatchToParent(MouseEvent e) {
                        Component source = (Component) e.getSource();
                        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, source.getParent());
                        source.getParent().dispatchEvent(parentEvent);
                    }
                });
                buttonText.setMargin(new Insets(10, 0, 10, 0));
                buttonText.setOpaque(false);
                buttonText.setEditable(false);
                buttonText.setEnabled(false);
                buttonText.setDragEnabled(false);
                buttonText.setFont(getFont());
                buttonText.setText(id);
                add(buttonText);

                //setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonText.getPreferredScrollableViewportSize().height * 3));

                add(Box.createRigidArea(new Dimension(40, 40)));

                gui.getTheme().registerButton(this);
            }

            @Override
            public void setEnabled(boolean t){
                super.setEnabled(t);
                buttonText.setDisabledTextColor(buttonText.getForeground().darker());
            }

            @Override
            public void setForeground(Color g){
                super.setForeground(g);
                if(buttonText!=null)buttonText.setDisabledTextColor(g);
            }
            @Override
            public void setFont(Font f){
                super.setFont(f);
                if(buttonText!=null) buttonText.setFont(f);
            }
        }

        private class DynamicButton extends ImageButton{

            private JTextPane buttonText;
            private JLabel timeText;
            String id;
            Timer timer;
            double time;
            int destination;

            public DynamicButton(String text, int dest, double t) {

                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setAlignmentX(CENTER_ALIGNMENT);

                destination = dest;
                id = text;
                time = t;

                timeText = new JLabel(" " +String.valueOf(t)+" ");
                timeText.setPreferredSize(new Dimension(screenSize.width / 17, screenSize.width / 17));
                add(timeText);

                buttonText = new JTextPane();
                buttonText.addMouseListener(new MouseAdapter() {
                    boolean exited;
                    boolean entered;

                    @Override
                    public void mouseExited(MouseEvent e) {
                        redispatchToParent(e);
                        exited = true;
                        entered = false;
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        redispatchToParent(e);
                        entered = true;
                        exited = false;
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        redispatchToParent(e);
                        if (entered && !exited) {
                            if (isEnabled()) compiler.userAction(destination);
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        redispatchToParent(e);
                    }

                    private void redispatchToParent(MouseEvent e) {
                        Component source = (Component) e.getSource();
                        MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, source.getParent());
                        source.getParent().dispatchEvent(parentEvent);
                    }
                });
                buttonText.setOpaque(false);
                buttonText.setMargin(new Insets(10, 0, 10, 0));
                buttonText.setEditable(false);
                buttonText.setEnabled(false);
                buttonText.setDragEnabled(false);
                buttonText.setFont(getFont());
                buttonText.setText(id);
                add(buttonText);

                //setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonText.getPreferredScrollableViewportSize().height * 3));

                add(Box.createRigidArea(new Dimension(40, 40)));
                gui.getTheme().registerButton(this);
            }

            @Override
            public void setEnabled(boolean t){
                super.setEnabled(t);
                if(t==false){
                    buttonText.setDisabledTextColor(new Color(116, 116, 116));
                    timeText.setForeground(new Color(116, 116, 116));
                }
            }

            @Override
            public void setForeground(Color g){
                super.setForeground(g);
                if(buttonText!=null) {
                    timeText.setForeground(g);
                    buttonText.setDisabledTextColor(g);
                }
            }

            @Override
            public void setFont(Font f){
                super.setFont(f);
                if(buttonText!=null){
                    timeText.setFont(f);
                    buttonText.setFont(f);
                }

            }

            public void startTime() {
                timer = new Timer(100, arg0 -> {
                    time-=.1;
                    time = Math.round(time * 100.0) / 100.0;
                    timeText.setText(" " +time+" ");
                    if(time<=0) {
                        timeText.setText(" X");
                        timer.stop();
                        setEnabled(false);
                    }
                });
                timer.setInitialDelay(100);
                timer.start();
            }
        }

        private class UserButton extends ImageButton{
            private JTextField textField;
            private ImageButton confirmInput;
            String id;
            int destination;

            public UserButton(int dest) {

                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setAlignmentX(CENTER_ALIGNMENT);
                setText(id);

                destination = dest;

                confirmInput = new ImageButton();
                confirmInput.setText("OK");
                confirmInput.setEnabled(false);
                confirmInput.setPreferredSize(new Dimension(80, 80));
                confirmInput.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (isEnabled()) compiler.customUserAction(destination, textField.getText());
                    }
                });
                add(confirmInput);

                add(Box.createRigidArea(new Dimension(10,10)));

                textField = new JTextField();
                textField.setOpaque(false);
                textField.setDragEnabled(false);
                textField.setFont(getFont());
                textField.setText(id);
                textField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        super.keyReleased(e);
                        if (textField.getText().length() == 0) {
                            confirmInput.setEnabled(false);
                        } else {
                            confirmInput.setEnabled(true);
                            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                                confirmInput.doClick();
                                e.consume();
                            }
                        }
                    }
                });
                textField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));
                textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
                add(textField);

                //setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredScrollableViewportSize().height));

                add(Box.createRigidArea(new Dimension(40, 40)));
                gui.getTheme().registerButton(this);
            }
            @Override
            public void setEnabled(boolean t){
                super.setEnabled(t);
                textField.setDisabledTextColor(getForeground().darker());
            }

            @Override
            public void setForeground(Color g){
                super.setForeground(g);
                if(textField !=null) {
                    confirmInput.setForeground(g);
                    textField.setDisabledTextColor(g);
                }
            }

            @Override
            public void setFont(Font f){
                super.setFont(f);
                if(textField!=null){
                    confirmInput.setFont(f);
                    textField.setFont(f);
                }

            }
        }
    }

    private static class ThemeRenderer extends Thread{

        DynamicBackground art;

        public ThemeRenderer(){
            art = gui.getArtWork();
        }

        public void update(String artwork){
            artwork = artwork.trim();
            art.switchBackground(artwork);
        }
    }

}
