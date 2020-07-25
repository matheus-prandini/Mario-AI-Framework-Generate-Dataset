# Mario-AI-Framework-Generate-Dataset

This project aims to automate the data generation process in the Super Mario Bros game implemented in the Mario AI Framework. The process has the flow shown below:

<img src="https://github.com/matheusprandini/Mario-AI-Framework-Generate-Dataset/blob/master/readmeImages/DataGenerationProcess.png" alt="alt text" width="600" height="600">

The program records the data collected from each frame of each level considered (when agent A* Star wins the level) for all agents, including A*.

**Limited Agents:**

- A* No Run: difficulty in running (using speed button).
- A* Limited Jump: difficulty making higher jumps (using jump button).
- A* Enemy/Gap Blind: difficulty avoiding obstacles (enemies and gap).

**Generated Data**

Data can be stored in the following ways:

- Csv.
- Image.
- Both.

## Csv

Features:

- 256 values representing the 16x16 grid level.
- Mario Position (X and Y coordinates).
- Actions Perfomed in the frame (Move Left, Move Right, Down, Jump, Speed).
- Game Events (Bump, Stomp Kill, Fire Kill, Shell Kill, Fall Kill, Jump, Land, Collect, Hurt, Kick, Lose, Win).

## Image

Screenshot of each frame. Size: 256x256.

## Dependencies

Java and the following dependencies must be installed to run this project:

[RabbitMQ](https://www.rabbitmq.com/download.html)

## Execution

Run the commands from src/ directory.

**Terminal 1:**

```
javac -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" .\queues\AStarReceiver.java

java -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" queues/AStarReceiver
```

**Terminal 2:**

```
javac -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" .\queues\AStarNoRunReceiver.java

java -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" queues/AStarNoRunReceiver
```

**Terminal 3:**

```
javac -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" .\queues\AStarLimitedJumpReceiver.java

java -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" queues/AStarLimitedJumpReceiver
```

**Terminal 4:**

```
javac -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" .\queues\AStarEnemyGapBlindReceiver.java

java -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" queues/AStarEnemyGapBlindReceiver
```

**Terminal 5:**

```
javac -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" Main.java

java -cp "jars/json-simple-1.1.1.jar;jars/amqp-client-5.7.1.jar;jars/slf4j-api-1.7.26.jar;jars/slf4j-simple-1.7.26.jar;" Main
```

Note: minimize all terminals after running commands.

## Configuration

The `conf.json` configuration file has the following structure:

```
{
    "datasetRootDir": "dataset", 
    "levelsDirName": "test",
    "dataType": "both"
}
```

- datasetRootDir: the name of the directory where the data will be stored.
- levelsDirName: the name of the directory under "levels/" where the data will be collected.
- dataType: type of data to be stored.

## Future Improvements

- Increase the number of threads running asynchronously in each agent queue.
- Implement a queue to only store data.
- Develop a class with the execution logs.
