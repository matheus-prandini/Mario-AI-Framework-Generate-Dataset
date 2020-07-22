package queues;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import engine.core.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import playerAISystem.Dataset;
import playerAISystem.Example;

public class AStarNoRunReceiver {
    
    private final static String QUEUE_NAME = "AstarNoRunQueue";

    /**
     * Read a certain mario level
     * 
     * @param filepath full path to the mario level
     */
    public static String getLevel(String filepath) {
        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
        }
        return content;
    }

    /**
     * Execute A Star No Run agent in a specific level and store the generated data.
     *
     * @param message     json object with all information needed to run the mario agent
    */
    public static void executeAstarNoRun(String message) throws IOException {

        try {

            // Load Json Object
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(message);
            String levelName = String.valueOf(jsonObject.get("levelName"));
            String levelFullPath = String.valueOf(jsonObject.get("levelFullPath"));
            String datasetRootDir = String.valueOf(jsonObject.get("datasetRootDir"));
            String dataType = String.valueOf(jsonObject.get("dataType"));

            // Start A* No Run agent on the given level
            ArrayList<Example> examplesAstarNoRun = new ArrayList<>();
            MarioGame gameAstarNoRun = new MarioGame();
            MarioResult resultAstarNoRun = gameAstarNoRun.runGame(examplesAstarNoRun, 
                                                new agents.robinBaumgartenNoRun.Agent(), 
                                                getLevel(levelFullPath), 30, 0, true, 300);

            // Save A* No Run data
            Dataset dataset = new Dataset(datasetRootDir, "AStarNoRun", levelName);
            dataset.setData(examplesAstarNoRun);
            dataset.createData(dataType);

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

    }

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            executeAstarNoRun(message);
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });

    }

}