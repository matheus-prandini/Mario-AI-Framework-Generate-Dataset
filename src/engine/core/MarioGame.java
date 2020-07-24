package engine.core;

import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.awt.*;
import java.awt.event.KeyAdapter;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import engine.helper.GameStatus;
import engine.helper.MarioActions;
import playerAISystem.Example;
import engine.core.MarioAgentEvent;
import engine.core.MarioEvent;

public class MarioGame {
    /**
     * the maximum time that agent takes for each step
     */
    public static final long maxTime = 40;
    /**
     * extra time before reporting that the agent is taking more time that it should
     */
    public static final long graceTime = 10;
    /**
     * Screen width
     */
    public static final int width = 256;
    /**
     * Screen height
     */
    public static final int height = 256;
    /**
     * Screen width in tiles
     */
    public static final int tileWidth = width / 16;
    /**
     * Screen height in tiles
     */
    public static final int tileHeight = height / 16;
    /**
     * print debug details
     */
    public static final boolean verbose = false;

    /**
     * pauses the whole game at any moment
     */
    public boolean pause = false;

    /**
     * events that kills the player when it happens only care about type and param
     */
    private MarioEvent[] killEvents;

    // visualization
    private JFrame window = null;
    private MarioRender render = null;
    private MarioAgent agent = null;
    private MarioWorld world = null;

    /**
     * Create a mario game to be played
     */
    public MarioGame() {

    }

    /**
     * Create a mario game with a different forward model where the player on
     * certain event
     *
     * @param killPlayer events that will kill the player
     */
    public MarioGame(MarioEvent[] killEvents) {
        this.killEvents = killEvents;
    }

    private int getDelay(int fps) {
        if (fps <= 0) {
            return 0;
        }
        return 1000 / fps;
    }

    private void setAgent(MarioAgent agent) {
        this.agent = agent;
        if (agent instanceof KeyAdapter) {
            this.render.addKeyListener((KeyAdapter) this.agent);
        }
    }

    /**
     * Run a certain mario level with a certain agent
     *
     * @param agent      the current AI agent used to play the game
     * @param level      a string that constitutes the mario level, it uses the same
     *                   representation as the VGLC but with more details. for more
     *                   details about each symbol check the json file in the levels
     *                   folder.
     * @param timer      number of ticks for that level to be played. Setting timer
     *                   to anything <=0 will make the time infinite
     * @param marioState the initial state that mario appears in. 0 small mario, 1
     *                   large mario, and 2 fire mario.
     * @param visuals    show the game visuals if it is true and false otherwise
     * @return statistics about the current game
     */
    public MarioResult runGame(ArrayList<Example> examples, MarioAgent agent, String level, int timer, int marioState,
            boolean visuals, int xPositionJFrame) {
        return this.runGame(examples, agent, level, timer, marioState, visuals, visuals ? 30 : 0, 2, xPositionJFrame);
    }

    /**
     * Run a certain mario level with a certain agent
     *
     * @param agent      the current AI agent used to play the game
     * @param level      a string that constitutes the mario level, it uses the same
     *                   representation as the VGLC but with more details. for more
     *                   details about each symbol check the json file in the levels
     *                   folder.
     * @param timer      number of ticks for that level to be played. Setting timer
     *                   to anything <=0 will make the time infinite
     * @param marioState the initial state that mario appears in. 0 small mario, 1
     *                   large mario, and 2 fire mario.
     * @param visuals    show the game visuals if it is true and false otherwise
     * @param fps        the number of frames per second that the update function is
     *                   following
     * @param scale      the screen scale, that scale value is multiplied by the
     *                   actual width and height
     * @return statistics about the current game
     */
    public MarioResult runGame(ArrayList<Example> examples, MarioAgent agent, String level, int timer, int marioState,
            boolean visuals, int fps, float scale, int xPositionJFrame) {
        if (visuals) {
            this.window = new JFrame("Mario AI Framework");
            this.window.setBounds(xPositionJFrame, 0, 272, 279);
            this.render = new MarioRender(1);
            this.window.setContentPane(this.render);
            this.window.pack();
            this.window.setResizable(false);
            this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.render.init();
            this.window.setVisible(true);

        }
        this.setAgent(agent);
        return this.gameLoop(examples, level, timer, marioState, visuals, fps, scale, xPositionJFrame);
    }

    public void promptEnterKey() {
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    public BufferedImage captureScreen(int xPositionJFrame) {
        try {
            // Define current screen size
            Rectangle oScreen = new Rectangle(8+xPositionJFrame, 30, 272-16, 279 - 22);

            // Create screen shot
            Robot robot = new Robot();
            BufferedImage oImage = robot.createScreenCapture(oScreen);

            return oImage;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private MarioResult gameLoop(ArrayList<Example> examples, String level, int timer, int marioState, boolean visual,
            int fps, float scale, int xPositionJFrame) {
        this.world = new MarioWorld(this.killEvents);
        this.world.visuals = visual;
        this.world.initializeLevel(level, 1000 * timer);
        if (visual) {
            this.world.initializeVisuals(this.render.getGraphicsConfiguration());
        }
        this.world.mario.isLarge = marioState > 0;
        this.world.mario.isFire = marioState > 1;
        this.world.update(new boolean[MarioActions.numberOfActions()]);
        long currentTime = System.currentTimeMillis();

        // initialize graphics
        VolatileImage renderTarget = null;
        Graphics backBuffer = null;
        Graphics currentBuffer = null;
        if (visual) {
            renderTarget = this.render.createVolatileImage(MarioGame.width, MarioGame.height);
            backBuffer = this.render.getGraphics();
            currentBuffer = renderTarget.getGraphics();
            this.render.addFocusListener(this.render);
        }

        MarioTimer agentTimer = new MarioTimer(MarioGame.maxTime);
        this.agent.initialize(new MarioForwardModel(this.world.clone()), agentTimer);

        ArrayList<MarioEvent> gameEvents = new ArrayList<>();
        ArrayList<MarioAgentEvent> agentEvents = new ArrayList<>();

        MarioAgentEvent lastAgentEvent = null;
        ArrayList<MarioEvent> lastGameEvents = new ArrayList<>();
        int i = 0;
        try
        {
            Thread.sleep(1000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

        while (this.world.gameStatus == GameStatus.RUNNING) {
            
            // Get Current Screen Complete Observation (Level and Enemies)
            int[][] currentObservation = new MarioForwardModel(this.world.clone()).getScreenCompleteObservation(0,0);
            
            BufferedImage currentImageObservation = null;
            if (visual) 
                currentImageObservation = captureScreen(xPositionJFrame);

            // Get Mario Position (x, y) and State (Small, Big or Fire)
            float marioX = (int) this.world.mario.x;
            float marioY = (int) this.world.mario.y;
            int lastMarioState = this.world.getMarioState();

            if (!this.pause) {
                //get actions
                agentTimer = new MarioTimer(MarioGame.maxTime);
                boolean[] actions = this.agent.getActions(new MarioForwardModel(this.world.clone()), agentTimer);
                if (MarioGame.verbose) {
                    if (agentTimer.getRemainingTime() < 0 && Math.abs(agentTimer.getRemainingTime()) > MarioGame.graceTime) {
                        System.out.println("The Agent is slowing down the game by: "
                                + Math.abs(agentTimer.getRemainingTime()) + " msec.");
                    }
                }
                // update world
                this.world.update(actions);

                // Get last actions (low level log) and last game event (high level log)
                lastGameEvents = this.world.lastFrameEvents;
                lastAgentEvent = new MarioAgentEvent(actions, this.world.mario.x, this.world.mario.y, 
                (this.world.mario.isLarge ? 1 : 0) + (this.world.mario.isFire ? 1 : 0), this.world.mario.onGround, 
                this.world.currentTick);

                gameEvents.addAll(lastGameEvents);
                agentEvents.add(lastAgentEvent);
            }

            //render world
            if (visual) {
                this.render.renderWorld(this.world, renderTarget, backBuffer, currentBuffer);
            }
            //check if delay needed
            if (this.getDelay(fps) > 0) {
                try {
                    currentTime += this.getDelay(fps);
                    Thread.sleep(Math.max(0, currentTime - System.currentTimeMillis()));
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            if (i > 0) {
                Example example = new Example(i, currentObservation, marioX, marioY, 
                                            new ArrayList<MarioEvent>(lastGameEvents), 
                                            lastAgentEvent, currentImageObservation);
                examples.add(example);
            }

            lastGameEvents.clear();
            agentEvents.clear();

            i += 1;
        }
        if (visual)
            this.window.dispose();
        return new MarioResult(this.world, gameEvents, agentEvents);
    }
}
