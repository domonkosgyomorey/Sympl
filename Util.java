import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
public class Util {

    public static String[] readFile(String fileName){
        if(fileName==null){
           System.err.println("No file name given");
           System.exit(1);
        }
        String[] file = null;
        try {
            List<String> words = Files.readAllLines(Paths.get(fileName));
            file = new String[words.size()];
            for(int i =0; i < words.size(); i++){
                file[i] = words.get(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return file;
    }
}
