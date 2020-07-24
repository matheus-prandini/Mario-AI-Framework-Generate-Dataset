import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

import engine.core.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import playerAISystem.Dataset;
import playerAISystem.Example;

public class Main {

    /**
     * This method iterates over all levels in "levelsDirName". If the A star agent
     * wins the level, then data regarding its execution and that of all limited
     * agents will be generated.
     * 
     * @param datasetRootDir directory to save the results (dataset)
     * @param levelsDirName  the full path to the mario levels directory to be
     *                       executed
     * @param dataType the data type that will be generated (csv, image or both)
     */
    public static void startProcess(String datasetRootDir, String levelsDirName, String dataType) throws Exception {

        String levelRootDir = "../levels/" + levelsDirName + "/";
        File f = new File(levelRootDir);
        String[] pathnames = f.list();
        int count = 0;

        datasetRootDir = "../" + datasetRootDir + "/" + levelsDirName + "/";

        for (String pathname : pathnames) {

            String levelFullPath = levelRootDir + pathname;

            JSONObject json = new JSONObject();
            json.put("levelPathName", pathname);
            json.put("levelFullPath", levelFullPath);
            json.put("datasetRootDir", datasetRootDir);
            json.put("dataType", dataType);

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
                channel.queueDeclare("AStarQueue", false, false, false, null);
                String message = json.toString();
                channel.basicPublish("", "AStarQueue", null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }

            count += 1;
            System.out.print("\r Processed " + count + " files");
        }
    }

    /**
     * Read a conf.json file
     * 
     */
    public static Object readJson(String filename) throws Exception {
        FileReader reader = new FileReader(filename);
        JSONParser jsonParser = new JSONParser();
        return jsonParser.parse(reader);
    }

    public static void main(String[] args) throws IOException {
        try {
            JSONObject jsonObject = (JSONObject) readJson("conf.json");
            String datasetRootDir = String.valueOf(jsonObject.get("datasetRootDir"));
            String levelsDirName = String.valueOf(jsonObject.get("levelsDirName"));
            String dataType = String.valueOf(jsonObject.get("dataType"));
            startProcess(datasetRootDir, levelsDirName, dataType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}