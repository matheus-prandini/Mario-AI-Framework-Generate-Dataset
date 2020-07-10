package playerAISystem;

import java.lang.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

import engine.core.MarioAgentEvent;
import engine.core.MarioEvent;
import engine.helper.EventType;
import engine.helper.MarioActions;

public class Example {
    private int id;
    private int[][] observation;
    private float marioX;
    private float marioY;
    private ArrayList<MarioEvent> gameEvents;
    private MarioAgentEvent agentEvent;
    private BufferedImage imageObservation;

    /**
     * Create an example result object.
     *
     * @param id   the example identification (id).
     * @param observation   the current observation of the level world (complete screen observation).
     * @param marioX    the X position of the mario agent in the current observation.
     * @param marioY    the Y position of the mario agent in the current observation.
     * @param gameEvents    the high level event of the current observation. This class uses the MarioEvent object to get informations.
     * @param agentEvent    the low level event of the current observation. This class uses the MarioAgentEvent object to get informations.
     */
    public Example(int id, int[][] observation, float marioX, float marioY, 
            ArrayList<MarioEvent> gameEvents, MarioAgentEvent agentEvent, BufferedImage imageObservation) {
        this.id = id;
        this.observation = observation;
        this.marioX = marioX;
        this.marioY = marioY;
        this.agentEvent = agentEvent;
        this.gameEvents = gameEvents;
        this.imageObservation = imageObservation;
    }

    public void printExample() {
        System.out.println("\n\nExample " + this.id + ": \n");
        this.printObservation();
        this.printVectorizedObservation();
        this.printMarioPosition();
        this.printGameEvents();
        this.printAgentEvent();
    }

    public void printObservation() {
        System.out.println("\nCurrent Observation: ");
        for (int row=0; row < this.observation.length; row++)
        {
            for (int col=0; col < this.observation[row].length; col++)
            {
                int value = this.observation[col][row];
                if (String.valueOf(value).length() > 1)
                    System.out.print(value + " ");
                else
                    System.out.print(value + "  ");
            }
            System.out.println("");
        }
    }

    public void printMarioPosition() {
        System.out.println("\nMario X: " + this.marioX);
        System.out.println("Mario Y: " + this.marioY);
    }

    public void printGameEvents() {
        System.out.println(this.gameEvents.size());
        if (this.gameEvents.size() > 0) {
            for (MarioEvent gameEvent : this.gameEvents) {
                System.out.println("\nGame Event: \n");
                System.out.println("Event Type: " + gameEvent.getEventType());
                System.out.println("Event Param: " + gameEvent.getEventParam());
                System.out.println("Mario X: " + gameEvent.getMarioX());
                System.out.println("Mario Y: " + gameEvent.getMarioY());
                System.out.println("Mario State: " + gameEvent.getMarioState());
            }
        }
    }

    public void printAgentEvent() {
        if (this.agentEvent != null) {
            System.out.println("\nAgent Event: \n");
            System.out.print("Agent Actions: ");
            for (int i = 0; i < this.agentEvent.getActions().length; i++) {
                System.out.print(this.agentEvent.getActions()[i] + ", ");
            }   
            System.out.println("\nMario on Ground: " + this.agentEvent.getMarioOnGround());
            System.out.println("Mario X: " + this.agentEvent.getMarioX());
            System.out.println("Mario Y: " + this.agentEvent.getMarioY());
            System.out.println("Mario State: " + this.agentEvent.getMarioState());
            System.out.println("Time: " + this.agentEvent.getTime());
        }
    }

    public void printVectorizedObservation() {
        int[] vectorizedObservation = this.getVectorizedObservation();

        System.out.println("\n\nCurrent Observation (Vectorized): ");
        for (int i = 0; i < vectorizedObservation.length; i++)
            System.out.print(vectorizedObservation[i] + " ");
    }

    public int[] getGameEventsValues() {

        // Array[BUMP, STOM_KILL, FIRE_KILL, SHELL_KILL, FALL_KILL, JUMP, LAND, COLLECT, HURT, KICK, LOSE, WIN]
        int[] gameEvent = new int[]{0,0,0,0,0,0,0,0,0,0,0,0};

        if (this.gameEvents.size() > 0) {

            for (MarioEvent event : this.gameEvents)
                gameEvent[event.getEventType()-1] = 1;

        }

        return gameEvent;
    }

    public int[] getActionEvents() {
        int [] action = new int[5];
        int [] actionValues = new int[]{MarioActions.LEFT.getValue(), MarioActions.RIGHT.getValue(),
            MarioActions.DOWN.getValue(), MarioActions.SPEED.getValue(), MarioActions.JUMP.getValue()};

        for (int i=0; i<5; i++)
            action[i] = this.agentEvent.getActions()[actionValues[i]] ? 1 : 0;

        return action;
    }

    public int[] getVectorizedObservation() {
        int[] vector = new int[(int) Math.pow(this.observation.length, 2)];
        int k = 0;

        for (int row = 0; row < this.observation.length; row++) {
            for (int col = 0; col < this.observation[row].length; col++) {
                vector[k] = this.observation[col][row];
                k += 1;
            }
        }

        return vector;
    }

    public int getId() {
        return this.id;
    }

    public int[][] getObservation() {
        return this.observation;
    }

    public float getMarioX() {
        return this.marioX;
    }

    public float getMarioY() {
        return this.marioY;
    }

    public ArrayList<MarioEvent> getGameEvents() {
        return this.gameEvents;
    }

    public MarioAgentEvent getAgentEvent() {
        return this.agentEvent;
    }

    public BufferedImage getImageObservation() {
        return this.imageObservation;
    }
}