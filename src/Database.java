import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by JohnBae on 8/20/15.
 */
public class Database {

    private static File systemFiles = new File("/Users/" + System.getProperty("user.name") +"/Documents/" + TextEngine.getGameName());
    private static File saveFiles = new File(systemFiles.getAbsolutePath()+"/saves");
    private static File systemSettings = new File(systemFiles+"/system");
    private static ArrayList<File> saves = new ArrayList<>();
    private static boolean recentStat = false;

    private static File recent = null;
    private static int speed;
    private static String size="";

    ArrayList<String> system = new ArrayList<>();

    public Database(){

        initializeData();

    }

    public void initializeData(){
        try {
            systemFiles.mkdir();
            systemFiles.createNewFile();
            saveFiles.mkdir();
            saveFiles.createNewFile();

            if (systemSettings.exists() && !systemSettings.isDirectory()) {

                BufferedReader br = new BufferedReader(new FileReader(systemSettings));

                String line;

                while((line =  br.readLine())!=null) {
                    String[] temp = line.split(":");
                    if(temp[0].equals("recent")&&!temp[1].equals("NEW")) {
                        recent = new File(temp[1]);
                        if(recent.exists()) recentStat = true;
                        else {
                            recentStat = false;
                            recent = null;
                        }
                    }
                    if(temp[0].equals("speed")&&temp.length>1) {
                        speed = Integer.parseInt(temp[1]);
                    }

                    if(temp[0].equals("size")&&temp.length>1) {
                        size = temp[1];
                    }
                }
                if(speed==-1) speed = 50;
                if(size.equals("")) size.equals("medium");

            }
            else {
                BufferedWriter writer = null;
                try
                {
                    writer = new BufferedWriter(new FileWriter(systemSettings));
                    writer.write("recent:NEW");
                    writer.newLine();
                    writer.write("speed:50");
                    writer.newLine();
                    writer.write("size:medium");
                    writer.close( );
                }
                catch ( IOException e)
                {
                    System.out.println("not found");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        findSaves();
    }

    public File getRecent(){
        return recent;
    }

    public void findSaves(){
        saves.clear();
        try {

            Files.walk(Paths.get(saveFiles.getAbsolutePath()), 1, FileVisitOption.FOLLOW_LINKS).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    File temp = filePath.toFile();
                    if (!temp.isHidden()) {
                        System.out.println("Loading " + temp.getName());
                        saves.add(temp);
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getSaveData(int i ){
        return saves.get(i);
    }

    public File getSaveData(String name ){
        File temp = new File(saveFiles.getAbsolutePath()+"/"+name);
        if(temp.exists()&&saves.contains(temp)) return temp;
        else return null;
    }

    public ArrayList<String> getSaveData(File f){
        ArrayList<String> result = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String text;
            while((text = br.readLine()) != null) {
                result.add(text);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getSaves(){
        return saves.size();
    }

    public void setRecent(String k){
        recent = new File(saveFiles.getAbsolutePath()+"/"+k);
    }

    public void setSpeed(int i){
        speed = i;
    }

    public int getSpeed(){
        return speed;
    }

    public void setSize(String s){
        if(s.equals("small")||s.equals("medium")||s.equals("large")){
            size = s;
        }
    }

    public String getSize(){
        return size;
    }

    public void saveSystemFiles(){
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(systemSettings));
            if(getRecent()!=null) writer.write("recent:"+ getRecent().getAbsolutePath());
            else writer.write("recent:NEW");
            writer.newLine();
            writer.write("speed:" + getSpeed());
            System.out.println(getSpeed());
            writer.newLine();
            writer.write("size:" + getSize());
            System.out.println(getSize());
            writer.close();
        }
        catch ( IOException e)
        {
            System.out.println("not found");
        }
    }

    public void save(String name, ArrayList<String> var, int pos){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter(saveFiles +"/"+name));

            writer.write(String.valueOf(pos));
            writer.newLine();
            for(String k : var){
                writer.write(k);
                writer.newLine();
            }
            writer.close();
        }
        catch ( IOException e)
        {

        }
    }

}