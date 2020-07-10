import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import engine.core.MarioAgent;
import engine.core.MarioGame;
import engine.core.MarioResult;
import playerAISystem.Dataset;
import playerAISystem.Example;

public class GenerateDataset {

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
     * Run a certain mario level with a certain limited agent. This method saves the
     * dataset generated (images and csv file).
     * 
     * @param datasetRootDir directory to save the results (dataset)
     * @param agentName      the name of the agent (for example, "AStar",
     *                       "AStarNoRun", ...)
     * @param levelFullPath  the full path to the mario level
     * @param levelName      the name of the mario level
     */
    public static void executeLimitedAgent(String datasetRootDir, String agentName, 
                                            String levelFullPath, String levelName) {

        Dataset dataset = new Dataset(datasetRootDir, agentName, levelName);
        ArrayList<Example> examples = new ArrayList<>();
        MarioGame game = new MarioGame();
        MarioAgent agent;

        if (agentName.toLowerCase().equals("astarnorun"))
            agent = new agents.robinBaumgartenNoRun.Agent();
        else if (agentName.toLowerCase().equals("astarlimitedjump"))
            agent = new agents.robinBaumgartenLimitedJump.Agent();
        else
            agent = new agents.robinBaumgartenEnemyGapBlind.Agent();

        game.runGame(examples, agent, getLevel(levelFullPath), 30, 0, true);

        dataset.setData(examples);
        dataset.createData();

    }

    /**
     * This method iterates over all levels in "levelsDirName". If the A star agent
     * wins the level, then data regarding its execution and that of all limited
     * agents will be generated.
     * 
     * @param datasetRootDir directory to save the results (dataset)
     * @param levelsDirName  the full path to the mario levels directory to be
     *                       executed
     */
    public static void generateAllDatasets(String datasetRootDir, String levelsDirName) throws IOException {

        String levelRootDir = "../levels/" + levelsDirName + "/";
        File f = new File(levelRootDir);
        String[] pathnames = f.list();
        int count = 0;

        datasetRootDir = "../" + datasetRootDir + "/" + levelsDirName + "/";

        for (String pathname : pathnames) {

            ArrayList<Example> examples = new ArrayList<>();
            String levelFullPath = levelRootDir + pathname;
            MarioGame game = new MarioGame();
            MarioResult result = game.runGame(examples, new agents.robinBaumgarten.Agent(), 
                                                getLevel(levelFullPath), 30, 0, true);

            // If A* wins the level
            if (result.getGameStatus().toString() == "WIN") {
                String[] levelFile = pathname.split("/");
                String levelName = levelFile[levelFile.length - 1].replace(".txt", "");

                // Save A Star data
                Dataset dataset = new Dataset(datasetRootDir, "Astar", levelName);
                dataset.setData(examples);
                dataset.createData();

                // Run all limited agents and save their data
                executeLimitedAgent(datasetRootDir, "AstarNoRun", levelFullPath, levelName);
                executeLimitedAgent(datasetRootDir, "AstarLimitedJump", levelFullPath, levelName);
                executeLimitedAgent(datasetRootDir, "AstarEnemyGapBlind", levelFullPath, levelName);
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
            generateAllDatasets(datasetRootDir, levelsDirName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}