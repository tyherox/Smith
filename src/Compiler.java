import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by JohnBae on 8/20/15.
 */
public class Compiler{

    private static Map<String,String> Variables = new HashMap<>();
    private static ArrayList<String> story = new ArrayList<>();
    private static ArrayList<String> options = new ArrayList<>();
    private static ArrayList<String> operatorStack = new ArrayList<>();

    private static String gameName = "Blank Game";
    private static String customInput = "";

    private int current;

    private static String renderableText ="";

    private static BufferedReader br;
    
    boolean passedConditional = false;
    
    private int firstBlock;

    public String getGameName(){
        return gameName;
    }

    public void setVar(ArrayList<String > var){
        Variables.clear();
        if(var!=null){
            for(String k : var){
                String[] temp = k.split(":");
                Variables.put(temp[0],temp[1]);
            }
        }
    }

    public ArrayList getVar() {
        ArrayList<String> k = new ArrayList<>();
        for(Map.Entry<String, String> entry: Variables.entrySet()) {
            String e = entry.getKey() + ":" + entry.getValue();
            k.add(e);
        }
        return k;
    }


    public Compiler(String path){
        int first = -2;
        try {
            br = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            Debugger.log("FILE NOT FOUND: " + path);
            e.printStackTrace();
        }
        String text;
        int i = 0;
        try {
            while((text = br.readLine()) != null) {
                int end = text.indexOf('>');
                String caller = text.substring(0, end + 1);
                if(caller.equals("<start>")) {
                    first = -1;
                    gameName = text.substring(end+2);
                }
                if(caller.equals("<b>")) {
                    
                    if(story.contains(text)) {
                        Debugger.log("SAME BLOCK IDENTIFICATION: " + text + " at " + i);
                        System.out.println(story.size());
                        System.exit(0);
                        break;
                    }
                    else {
                        if(first==-1){
                            first = i;
                            firstBlock = 1;
                        }
                        story.add(text);
                        i++;
                    }

                }
                else {
                    story.add(text);
                    i++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getFirstBlock(){
        return firstBlock;
    }

    /** Expression Readers**/


    public void read(int i){
        current = i;
        if(story.size()<=i) {
            Debugger.log("OUT OF BOUNDS EXCEPTION");
            System.exit(0);
        }
        String text = story.get(i);
        Debugger.log("\n-----------------------------------------------------------" + 
                "\nReading line :" + i + ", Text: " + text);
        int start = text.indexOf('<');
        int end = text.indexOf('>');

        if(text.length()>0&&end!=-1&&start!=-1){
            String caller = text.substring(start, end+1);
            String content;
            if(text.replace(" ","").equals(caller)) {
                content = null;
            }
            else {
                content = text.split(caller)[1];
                content = replaceVar(content);
            }

            if(operatorStack.isEmpty()){

                if(caller!=null)caller = caller.trim();
                if(content!=null)content = content.trim();
                
                if(caller.equals("<v>")) {
                    Debugger.log("At <v>");
                    vOperator(content);
                }
                else if(caller.equals("<if>")) {
                    Debugger.log("At <if>");
                    passedConditional = false;
                    if(ifOperator(content)) {
                        Debugger.log("IF passed");
                        TextEngine.forward();
                    }
                    else {
                        operatorStack.add("if");
                        Debugger.log("IF failed");
                        TextEngine.forward();
                    }
                }
                else if(caller.equals("<u>")) {
                    operatorStack.add("userInput");
                    Debugger.log("At <u>");
                    uOperator(i);
                }
                else if(caller.equals("<d>")) {
                    operatorStack.add("dynamicInput");
                    Debugger.log("At <d>");
                    String[] t = content != null ? content.split(":") : new String[0];
                    dOperator(t[1], i, Double.parseDouble(t[0].replace(" ", "")));
                }
                else if(caller.equals("<o>")) {
                    operatorStack.add("optionInput");
                    Debugger.log("At <o>");
                    oOperator(content, i);
                }
                else if(caller.equals("<b>")) {
                    Debugger.log("At <b>");
                    bOperator();
                }
                else if(caller.equals("</>")) {
                    Debugger.log("At </>");
                    endOperator();
                }
                else if(caller.equals("<t>")) {
                    operatorStack.add("text");
                    Debugger.log("At <t>");
                    TextEngine.forward();
                }
                else if(caller.equals("<r>")) {
                    Debugger.log("At <r>");
                    rOperator(content);
                }
                else if(caller.equals("<c>")) {
                    Debugger.log("At <c>");
                    cOperator(content);
                }
                else if(caller.equals("<f>")) {
                    Debugger.log("At <f>");
                    fOperator(content);
                }
                else if(caller.equals("<i>")){
                    Debugger.log("At <i>");
                    iOperator(content);
                }
                else {
                    Debugger.log("Forwarded from compiler: 1");
                    TextEngine.forward();
                }

            }
            else{
                if(!operatorStack.isEmpty()){
                    Debugger.log("stack filled - ");
                    String operator = operatorStack.get(operatorStack.size()-1);

                    if(operator.equals("if")&&caller.equals("</if>")) {
                        Debugger.log("At </if>");
                        operatorStack.clear();
                        operatorStack.add("callElse");
                        passedConditional = true;
                        TextEngine.forward();
                    }
                    else if(operator.equals("callElse")&&caller.equals("<else>")) {
                        passedConditional = false;
                        Debugger.log("At <else>");
                        operatorStack.clear();
                        TextEngine.forward();
                    }
                    else if(operator.equals("else")&&caller.equals("</else>")) {
                        Debugger.log("At </else>");
                        operatorStack.clear();
                        TextEngine.forward();
                    }
                    else if (operator.equals("callElse")&&caller.equals("<elseIf>")){
                        passedConditional = false;
                        Debugger.log("At <elseIf>");
                        if(elseIfOperator(content)){
                            Debugger.log("elseIf passed");
                            operatorStack.clear();
                            TextEngine.forward();
                        }
                        else{
                            Debugger.log("elseIf failed");
                            operatorStack.clear();
                            operatorStack.add("callElse");
                            TextEngine.forward();
                        }
                    }
                    else if(operator.equals("callElse")&&caller.equals("</elseIf>")){
                        passedConditional = true;
                        Debugger.log("At </elseIf>");
                        operatorStack.clear();
                        TextEngine.forward();
                    }
                    else if(operator.equals("userInput")&&caller.equals("</u>")) {
                        Debugger.log("At </u>");
                        operatorStack.clear();
                        TextEngine.forward();
                    }
                    else if(operator.equals("dynamicInput")&&caller.equals("</d>")) {
                        Debugger.log("At </d>");
                        operatorStack.clear();
                        TextEngine.forward();
                    }
                    else if(operator.equals("optionInput")&&caller.equals("</o>")) {
                        Debugger.log("At </o>");
                        operatorStack.clear();
                        TextEngine.forward();
                    }
                    else if(operator.equals("text")&&caller.equals("</t>")){
                        Debugger.log("At </t>");
                        operatorStack.clear();
                        tOperator(replaceVar(renderableText));
                        renderableText = "";
                    }
                    else if (operator.equals("callElse")){
                        Debugger.log("hanging statement");
                        operatorStack.clear();
                        if(passedConditional)TextEngine.forward(current);
                        else TextEngine.forward();
                    }
                    else{
                        Debugger.log("Skipping lines due to: " + operator +" command");
                        TextEngine.forward();
                    }
                }
            }
        }
        else {

            if(operatorStack.size()>0) {
                String operator = operatorStack.get(operatorStack.size()-1);

                if(operator.equals("text")){
                    Debugger.log("Building text: \"" + text + "\"");
                    if(text.length()==0) {
                        renderableText += System.getProperty("line.separator");
                    }
                    else renderableText += text + System.getProperty("line.separator");
                    TextEngine.forward();
                }
                else{
                    Debugger.log(operator + " operator");
                    TextEngine.forward();
                }
            }
            else{
                Debugger.log("Forwarded from compiler 2: " + text);
                TextEngine.forward();
            }
        }
    }

    private boolean elseIfOperator(String content) {
        return ifOperator(content);
    }

    public void iOperator(String content){
        TextEngine.artUpdate(content);
        TextEngine.forward();
    }

    public void uOperator(int i) {
        TextEngine.addUserOption(i);
        TextEngine.forward();
    }

    public void dOperator(String text, int placeHolder, double time) {
        TextEngine.addDynamicOption(text, placeHolder, time);
        TextEngine.forward();
    }

    public boolean ifOperator(String text) {
        char[] operators = "<=>&".toCharArray();
        boolean number = true;
        String[] temp;
        String var = null;
        String value  = null;
        boolean found = false;
        text = replaceVar(text);
        if(text.contains("<u>")) text = text.replace("<u>",customInput);
        for(char t : operators) {
            if(text.contains(Character.toString(t))){
                found = true;
                temp = text.split(Character.toString(t));
                var = temp[0].replace(" ","");
                value = temp[1].replace(" ","");
                break;
            }
        }
        if((var.length()==0||value.length()==0)&&!text.contains("&")) {
            Debugger.log("Error with ifOperator at :" + current + ", " + var + ", " + value +", " + text + ", " +
                    found + ", " +
                    !Variables
                    .containsKey(var) + ", " + !text.contains("&"));
            System.exit(0);
        }
        if(text.contains("&")) {
            Random rand = new Random();
            int k = Integer.parseInt(value);
            if(k==0) return false;
            double randomNum = rand.nextDouble();
            System.out.println("!" + (Double.parseDouble(value)/100.000) + ": " + randomNum);
            if(randomNum<=(Double.parseDouble(value)/100.000)) return true;
        }

        else if(!isNumeric(value)){
            number = false;
        }

        if(text.contains("=")) {
            if(number) {
                if(Integer.parseInt(var)==Integer.parseInt(value)) return true;
                else return false;
            }
            else{
                if(var.contentEquals(value)) return true;
                else return false;
            }

        }

        else if(text.contains("<")) {
            if(number) {
                if(Integer.parseInt(var)<Integer.parseInt(value)) return true;
                else return false;
            }
            else{
                Debugger.log("Invalid operation with String");
                System.exit(0);
            }
        }
        else if(text.contains(">")) {
            if(number) {
                if(Integer.parseInt(var)>Integer.parseInt(value)) return true;
                else return false;
            }
            else{
                Debugger.log("Invalid operation with String");
                System.exit(0);
            }
        }
        return false;
    }

    public void vOperator(String text) {
        String[]temp = text.split(":");
        String variable  = temp[0].replace(" ", "");
        String value  = temp[1].replace(" ", "");
        Variables.put(variable, value);
        TextEngine.forward();
    }

    public void oOperator(String text, int placeHolder) {
        TextEngine.addOption(text, placeHolder);
        TextEngine.forward();
    }

    public void endOperator() {
        TextEngine.renderOption();
    }

    public void bOperator() {
        TextEngine.clear();
        TextEngine.save();
        TextEngine.forward();
    }

    public void tOperator(String text) {
        TextEngine.renderText(text);
    }

    public void rOperator(String text) {
        goTo("<b>" + text);
    }

    public void cOperator(String text) {
        char[] operators = "+-=*/".toCharArray();
        String[] temp;
        String var = null;
        String value  = null;
        boolean found = false;
        for(char t : operators) {
            if(text.contains(Character.toString(t))){
                found = true;
                if(t == '+') temp = text.split("\\+");
                else temp = text.split(Character.toString(t));
                var = temp[0].replace(" ","");
                value = temp[1].replace(" ","");
                break;
            }
        }
        if(value.equals("<u>")) value = value.replace("<u>",customInput);
        if(!found ||!Variables.containsKey(var)) {
            Debugger.log("Error with cOperator at :" + current + ", " + var + ", " + value +", " + text + ", " +
                    found + ", " + !Variables.containsKey(var));
            Iterator it = Variables.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
            System.exit(0);
        }

        if(text.contains("=")) {
            Variables.replace(var, value);
        }
        else if(!isNumeric(Variables.get(var))){
            Debugger.log("Illegal operation with String at " + current);
            System.exit(0);
        }

        else if(text.contains("-")) {
            System.out.println(value);
            int result = Integer.parseInt(Variables.get(var))-Integer.parseInt(value);
            Variables.replace(var,String.valueOf(result));
        }
        else if(text.contains("/")) {
            int result = Integer.parseInt(Variables.get(var))/Integer.parseInt(value);
            Variables.replace(var,String.valueOf(result));
        }
        else if(text.contains("*")) {
            int result = Integer.parseInt(Variables.get(var))*Integer.parseInt(value);
            Variables.replace(var,String.valueOf(result));
        }
        else if(text.contains("+")) {
            int result = Integer.parseInt(Variables.get(var))+Integer.parseInt(value);
            Variables.replace(var,String.valueOf(result));
        }
        TextEngine.forward();
    }

    public void fOperator(String text) {
        TextEngine.addOption(text,-1);
        TextEngine.renderOption();
    }

    public void userAction(int dest){
        options.clear();
        if(dest!=-1&&dest!=current){
            Debugger.log("user forward");
            TextEngine.forward(dest + 1);
        }
        else {
            TextEngine.finishGame();
        }
    }

    public void goTo(String dest) {
        boolean found = false;

        for(int i = 0; i<story.size();i++) {
            String name = dest.replace("<b>","");
            String compare = story.get(i);
            if(compare.contains(name)&&compare.contains("<b>")&&compare.indexOf(name)>compare.indexOf("<b>")){
                found = true;
                options.clear();
                Debugger.log("goto forward :" + dest);
                TextEngine.forward(i);
                break;
            }
        }
        if(!found) {
            Debugger.log("WRONG DESTINATION " + dest + ": " + current);
            System.exit(0);
        }
    }

    public String replaceVar(String text){
        int s = text.indexOf("{");
        int f = text.indexOf("}");
        while(s!=-1&&f!=-1) {
            String temp = text.substring(s+1,f);
            if(Variables.containsKey(temp)) {
                text = text.replace("{"+temp+"}",Variables.get(temp));
            }
            s = text.indexOf("{");
            f = text.indexOf("}");
        }
        Debugger.log("Replaced variables");
        return text;
    }

    public static boolean isNumeric(String str) {
        try
        {
            double dv = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public void customUserAction(int dest, String content) {
        customInput = content;
        options.clear();
        Debugger.log("custom forward");
        TextEngine.forward(dest + 1);
    }


}
