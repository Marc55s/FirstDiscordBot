package musicbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler {
    InputStream is;
    BufferedReader reader;
    String line;
    private List<String> commmands;
    private List<String> special;

    public CommandHandler(){
        specialSetup();
        is  = ClassLoader.getSystemResourceAsStream("commands.txt");
        if (is == null) {
            throw new AssertionError();
        }
        reader = new BufferedReader(new InputStreamReader(is));
        commmands = new ArrayList<>();

        while (true) {
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            commmands.add(line);
        }
    }

    public void specialSetup(){
        special = new ArrayList<>();
        special.add("https://www.youtube.com/watch?v=rgAM1s2CRjM&t=5s");
        special.add("D:\\Development\\Java Projects\\FirstDiscordBot\\src\\main\\resources\\hassschrei.mp3");
    }

    public List<String> getCommmands() {
        return commmands;
    }

    public List<String> getSpecial() {
        return special;
    }
}
