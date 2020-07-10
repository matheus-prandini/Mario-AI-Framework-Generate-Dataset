package playerAISystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CsvHandler {

    private String name;
    private String path;
    private ArrayList<Example> data;

    /**
     * Create a csvHandler result object.
     *
     * @param name the name of the csv file.
     * @param path the path to the csv file (without filename).
     * @param data the list of examples.
     */
    public CsvHandler(String name, String path, ArrayList<Example> data) {
        this.name = name;
        this.path = path;
        this.data = new ArrayList<>(data);
        this.createDirectory();
    }

    public void createDirectory() {
        File file = new File(this.getFullCsvPath());
        file.getParentFile().mkdirs();
    }

    public void writeHeader(FileWriter csvWriter) {
        try {
            //System.out.println("Header...");

            // Level features (Scene and Enemies)
            for (int pos=0; pos < this.data.get(0).getVectorizedObservation().length; pos++) 
                csvWriter.append(String.valueOf("Obs" + pos + ","));

            // Mario Position (X and Y)
            csvWriter.append("MarioX,MarioY,");

            // Mario State (Small, Big or Fire)
            //csvWriter.append("MarioState,");

            // Action Events (Left, Right, Down, Speed, Jump)
            String[] actions = new String[]{"Left", "Right", "Down", "Speed", "Jump"};
            
            for (String action : actions)
                csvWriter.append("Action" + action + ",");

            // Game Event
            String[] events = new String[]{"Bump", "StompKill", "FireKill", "ShellKill", "FallKill",
            "Jump", "Land", "Collect", "Hurt", "Kick", "Lose", "Win"};

            for (String event : events)
                csvWriter.append("Event" + event + ",");

            // End of Header
            csvWriter.append("\n");

        } catch (IOException e) {
            System.out.println("Error processing CSV file...");
            e.printStackTrace();
        }
    }

    public void writeData(FileWriter csvWriter) {
        try {
            //System.out.println("Data...");

            for (Example example : data) {

                // Level features (Scene and Enemies)
                for (int elem : example.getVectorizedObservation())
                    csvWriter.append(String.valueOf(elem) + ",");

                // Mario Position (X and Y)
                csvWriter.append(String.valueOf(example.getMarioX() + ","));
                csvWriter.append(String.valueOf(example.getMarioY() + ","));

                // Action Events (Left, Right, Down, Speed, Jump)
                if (example.getAgentEvent() != null) {

                    for (int action : example.getActionEvents())
                        csvWriter.append(String.valueOf(action) + ",");
                }

                // Game Event
                for (int gameEvent : example.getGameEventsValues())
                    csvWriter.append(String.valueOf(gameEvent) + ",");

                csvWriter.append("\n");

            }
            
            // End of Data

        } catch (IOException e) {
            System.out.println("Error processing CSV file...");
            e.printStackTrace();
        }
    }

    public void writeFile() {
        try {
            //System.out.println("Writing CSV file...");
            FileWriter csvWriter = new FileWriter(new File(this.getFullCsvPath()));
            this.writeHeader(csvWriter);
            this.writeData(csvWriter);
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            System.out.println("Error processing CSV file...");
            e.printStackTrace();
        }
    }

    public String getFullCsvPath() {
        return this.path + this.name + ".csv";
    }
    
}