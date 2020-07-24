package playerAISystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Dataset {

    private String rootDirName;
    private String agentName;
    private String levelName;
    private ArrayList<Example> data;

    /**
     * Create a dataset result object from a certain agent and a certain mario level.
     *
     * @param rootDirName the name of the dataset directory.
     * @param agentName the name of the agent.
     * @param levelName the name of the mario level.
     */

    public Dataset(String rootDirName, String agentName, String levelName) {
        this.rootDirName = rootDirName;
        this.agentName = agentName;
        this.levelName = levelName;
    }

    public Dataset(String rootDirName, String agentName, String levelName, ArrayList<Example> data) {
        this.rootDirName = rootDirName;
        this.agentName = agentName;
        this.levelName = levelName;
        this.data = new ArrayList<>(data);
    }
    //

    public void createData(String dataType) {
        try {
            if (dataType.toLowerCase().equals("csv"))
                this.createCsv();
            else if (dataType.toLowerCase().equals("image"))
                this.createImages();
            else if (dataType.toLowerCase().equals("both")) {
                this.createCsv();
                this.createImages();
            }
            else
                System.out.println("Try one of the following options: 'csv', 'image' or 'both'");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createCsv() {
        CsvHandler csvFile = new CsvHandler(this.levelName,
                                            this.rootDirName + this.agentName + "/" + this.levelName + "/", 
                                            this.data);
        csvFile.writeFile();
    }

    public void createImages() throws IOException {
        File file = new File(this.rootDirName + this.agentName + "/" + this.levelName + "/" + "Images/");
        file.mkdirs();
        for (Example example : data)
            ImageIO.write(example.getImageObservation(), 
                        "png", 
                        new File(this.rootDirName + this.agentName + "/" + 
                            this.levelName + "/" + "/Images/Mario" + example.getId() + ".png"));
    }

    public void setData(ArrayList<Example> data) {
        this.data = new ArrayList<>(data);
    }

}