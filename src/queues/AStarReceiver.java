package queues;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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

public class AStarReceiver {

    private final static String QUEUE_NAME = "AStarQueue";

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
     * Send a json object to a specific queue
     *
     * @param queueName     the name of the destination queue
     * @param json      json object sent to the destination queue
    */
    public static void sendToQueue(String queueName, JSONObject json) throws Exception {
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            channel.queueDeclare(queueName, false, false, false, null);
            String message = json.toString();
            channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

    }

    /**
     * Execute A Star agent in a specific level.
     * If it wins the level, then all limited agents are executed in the same level.
     *
     * @param message     json object with all information needed to run the mario agent
    */
    public static void executeAstar(String message) throws IOException {

        try {

            // Load Json Object
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(message);
            String levelPathName = String.valueOf(jsonObject.get("levelPathName"));
            String levelFullPath = String.valueOf(jsonObject.get("levelFullPath"));
            String datasetRootDir = String.valueOf(jsonObject.get("datasetRootDir"));
            String dataType = String.valueOf(jsonObject.get("dataType"));

            // Show or not execution
            boolean visual = true;
            if (dataType.toLowerCase().equals("csv"))
                visual = false;

            // Start A* agent on the given level
            ArrayList<Example> examplesAstar = new ArrayList<>();
            MarioGame gameAstar = new MarioGame();
            MarioResult resultAstar = gameAstar.runGame(examplesAstar, new agents.robinBaumgarten.Agent(), 
                                                getLevel(levelFullPath), 30, 0, visual, 0);

            // If A* wins the level
            if (resultAstar.getGameStatus().toString() == "WIN") {
                String[] levelFile = levelPathName.split("/");
                String levelName = levelFile[levelFile.length - 1].replace(".txt", "");

                // Create Json Object
                JSONObject json = new JSONObject();
                json.put("levelName", levelName);
                json.put("levelFullPath", levelFullPath);
                json.put("datasetRootDir", datasetRootDir);
                json.put("dataType", dataType);

                // Run all limited agents and save their data
                sendToQueue("AstarNoRunQueue", json);
                sendToQueue("AstarLimitedJumpQueue", json);
                sendToQueue("AstarEnemyGapBlindQueue", json);

                // Save A* data
                Dataset dataset = new Dataset(datasetRootDir, "AStar", levelName);
                dataset.setData(examplesAstar);
                dataset.createData(dataType);
            }
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
            executeAstar(message);
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}