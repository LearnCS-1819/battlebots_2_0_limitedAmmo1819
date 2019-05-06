package bots;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

import java.awt.*;

public class QirongBot extends Bot {
    private boolean debug = true;
    private boolean bulletDetectionDebug = false;
    BotHelper botHelper = new BotHelper();
    private static double DANGERZONE = 50;
    private static double bulletDodgeZone = 100;
    private int moveTimer = 0;
    private int bulletTrackTimer = 0;
    private double bulletInitialX;
    private double bulletInitialY;
    private double bulletFinalX;
    private double bulletFinalY;
    private double bulletDeltaX;
    private double bulletDeltaY;
    private boolean firstRound;
    int j=0;
    BotInfo[] enemyBots = new BotInfo[6];//declares an array for enemy bots

    /**
     * This method is called at the beginning of each round. Use it to perform
     * any initialization that you require when starting a new round.
     */
    @Override
    public void newRound() {
        firstRound = true;
    }

    /**
     * This method is called at every time step to find out what you want your
     * Bot to do. The legal moves are defined in constants in the BattleBotArena
     * class (UP, DOWN, LEFT, RIGHT, FIREUP, FIREDOWN, FIRELEFT, FIRERIGHT, STAY,
     * SEND_MESSAGE). <br><br>
     * <p>
     * The <b>FIRE</b> moves cause a bullet to be created (if there are
     * not too many of your bullets on the screen at the moment). Each bullet
     * moves at speed set by the BULLET_SPEED constant in BattleBotArena. <br><br>
     * <p>
     * The <b>UP</b>, <b>DOWN</b>, <b>LEFT</b>, and <b>RIGHT</b> moves cause the
     * bot to move BOT_SPEED
     * pixels in the requested direction (BOT_SPEED is a constant in
     * BattleBotArena). However, if this would cause a
     * collision with any live or dead bot, or would move the Bot outside the
     * playing area defined by TOP_EDGE, BOTTOM_EDGE, LEFT_EDGE, and RIGHT_EDGE,
     * the move will not be allowed by the Arena.<br><Br>
     * <p>
     * The <b>SEND_MESSAGE</b> move (if allowed by the Arena) will cause a call-back
     * to this Bot's <i>outgoingMessage()</i> method, which should return the message
     * you want the Bot to broadcast. This will be followed with a call to
     * <i>incomingMessage(String)</i> which will be the echo of the broadcast message
     * coming back to the Bot.
     *
     * @param me       A BotInfo object with all publicly available info about this Bot
     * @param shotOK   True iff a FIRE move is currently allowed
     * @param liveBots An array of BotInfo objects for the other Bots currently in play
     * @param deadBots An array of BotInfo objects for the dead Bots littering the arena
     * @param bullets  An array of all Bullet objects currently in play
     * @return A legal move (use the constants defined in BattleBotArena)
     */
    @Override

    public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {
        BotInfo[] enemyBots = liveBots;
        for (int i = 0; i < enemyBots.length; i++) {
            BotInfo tempBot = enemyBots[i];
            if (tempBot.getTeamName().equals("Team2")) {
                enemyBots = editArray(enemyBots, i);
                i--;
            }
        }
        BotInfo closestBot = botHelper.findClosest(me, enemyBots);//creates a bothelper that will detect the closest live bot that is an enemy
        moveTimer++;
        if (bullets != null) {
            Bullet closestBullet = botHelper.findClosest(me, bullets);//creates a bothelper to track the nearest bullet

            //System.out.println("liveBots Length : " + liveBots.length);


            //System.out.println("is it the first round? " + firstRound);
            //BotInfo closestDeadBot = botHelper.findClosest(me, deadBots);//creates a bothelper that will detect the closest dead bot
            if (botHelper.manhattanDist(me.getX(), me.getY(), closestBullet.getX(), closestBullet.getY()) < bulletDodgeZone) {//calculates the velocity of the nearby bullet
                if (bulletTrackTimer == 1) {
                    bulletFinalX = closestBullet.getX();
                    bulletFinalY = closestBullet.getY();
                    bulletDeltaX = bulletFinalX - bulletInitialX;//positive deltaX means bullet is moving to the right, negative deltaX means bullet is moving to the left
                    bulletDeltaY = bulletFinalY - bulletInitialY;//positive deltaY means bullet is moving down, negative deltaY means bullet is moving up
                    if (bulletDetectionDebug) {
                        System.out.println("deltaX " + bulletDeltaX);
                        System.out.println("deltaY " + bulletDeltaY);
                    }
                    bulletTrackTimer = 0;
                }
                if (bulletTrackTimer == 0) {
                    bulletInitialX = closestBullet.getX();
                    bulletInitialY = closestBullet.getY();
                    bulletTrackTimer++;

                }
            }
            if (botHelper.manhattanDist(me.getX() + 1, me.getY(), closestBullet.getX(), closestBullet.getY()) < bulletDodgeZone) {//code for bullet dodge
                if (bulletDeltaX != 0) {//if bulletDeltaX is not zero, the bullet is moving horizontally
                /*if (moveTimer == 10 && bulletDeltaX > 0) {//in cases where enemy bots are rapidly firing, occassionally fires a shot back to disrupt them
                    if (shotOK) {
                        return BattleBotArena.FIRELEFT;
                    }
                } else if (moveTimer == 10 && bulletDeltaX < 0) {
                    if (shotOK) {
                        return BattleBotArena.FIRERIGHT;
                    }
                }*/
                    if (me.getY() + 1 <= closestBullet.getY()) {//move up if bot is already above the bullet
                        if (bulletDetectionDebug) {
                            System.out.println("dodging UP " + moveTimer);
                        }
                        if (me.getY() < Bot.RADIUS * 2) {//if there is not enough space to go up, dodge down instead
                            return BattleBotArena.DOWN;
                        }
                        return BattleBotArena.UP;
                    } else if (me.getY() + 1 > closestBullet.getY()) {//move down if bot is below bullet{
                        if (bulletDetectionDebug) {
                            System.out.println("dodging DOWN " + moveTimer);
                        }
                        if (me.getY() > BattleBotArena.BOTTOM_EDGE - Bot.RADIUS) {//if there is not enough space to move down, dodge up
                            return BattleBotArena.UP;
                        }
                        return BattleBotArena.DOWN;
                    }
                }
                if (bulletDeltaY != 0) {//if bulletDeltaY is not zero, the bullet is moving vertically
                /*if (moveTimer == 10 && bulletDeltaY > 0) {//in cases where enemy bots are rapidly firing and this bot is constantly dodging, occassionally fires a shot back to disrupt them
                    if (shotOK) {
                        return BattleBotArena.FIREUP;
                    }
                } else if (moveTimer == 10 && bulletDeltaY < 0) {
                    if (shotOK) {
                        return BattleBotArena.FIREDOWN;
                    }
                }*/
                    if (me.getX() + 1 <= closestBullet.getX()) {
                        if (bulletDetectionDebug) {
                            System.out.println("dodging LEFT " + moveTimer);
                        }
                        if (me.getX() < Bot.RADIUS) {//if there is not enough room to dodge left, dodge right instead
                            return BattleBotArena.RIGHT;
                        }
                        return BattleBotArena.LEFT;
                    } else if (me.getX() + 1 > closestBullet.getX()) {
                        if (bulletDetectionDebug) {
                            System.out.println("dodging RIGHT " + moveTimer);
                        }
                        if (me.getX() > BattleBotArena.RIGHT_EDGE - Bot.RADIUS) {//if there is not enough room to dodge right, dodge left instead
                            return BattleBotArena.LEFT;
                        }
                        return BattleBotArena.RIGHT;
                    }
                }
            }
        }
        if (me.getBulletsLeft() == 0){//checks if ammo empty and scavenges for some if so
            BotInfo[] deadBotsAmmo = deadBots;
            for (int i = 0; i < deadBotsAmmo.length; i++) {
                BotInfo tempBot = deadBotsAmmo[i];
                if (tempBot.getBulletsLeft()==0) {
                    deadBotsAmmo = editArray(deadBotsAmmo, i);
                    i--;
                }
            }

        }
        if (moveTimer > 10) {//resets the timer that determines whether the bot moves up or down
            moveTimer = 0;
        }
        if ((me.getX() - closestBot.getX() < 5) && (me.getX() - closestBot.getX() > -5) && !(closestBot.getTeamName().equals("Team2"))) {//if the enemy bot's x cordinates are close, this makes it fire up or down. also checks for teamname before firing
            if (me.getY() < closestBot.getY()) {
                if (debug) {
                    System.out.println("firingDown " + moveTimer);
                }
                if (shotOK) {
                    return BattleBotArena.FIREDOWN;
                }
            } else {
                if (debug) {
                    System.out.println("firingUp " + moveTimer);
                }
                if (shotOK) {
                    return BattleBotArena.FIREUP;
                }
            }
        }
        if ((me.getY() - closestBot.getY() < 5) && (me.getY() - closestBot.getY() > -5) && !(closestBot.getTeamName().equals("Team2"))) {//if the enemy's bot's y coordinates are close, this makes the bot shoot left or right. also checks for teamname before firing
            if (me.getX() < closestBot.getX()) {
                if (debug) {
                    System.out.println("firingRight " + moveTimer);
                }
                if (shotOK) {
                    return BattleBotArena.FIRERIGHT;
                }
            } else {
                if (debug) {
                    System.out.println("firingLeft " + moveTimer);
                }
                if (shotOK) {
                    return BattleBotArena.FIRELEFT;
                }
            }
        }
        if (botHelper.manhattanDist(me.getX(), me.getY(), closestBot.getX(), closestBot.getY()) < DANGERZONE) {//Code to flee from very close bots
            if (moveTimer <= 10) {
                if (me.getX() < closestBot.getX()) {//fleeing left or right to keep a distance from other robots
                    if (debug) {
                        System.out.println("fleeingLeft " + moveTimer);
                    }
                    return BattleBotArena.LEFT;
                } else {
                    if (debug) {
                        System.out.println("fleeingRight " + moveTimer);
                    }
                    return BattleBotArena.RIGHT;
                }

            }
            if (moveTimer > 10) {
                if (me.getY() < closestBot.getY()) {//fleeing up or down to keep a distance from other robots
                    if (debug) {
                        System.out.println("fleeingUp " + moveTimer);
                    }
                    return BattleBotArena.UP;
                } else {
                    if (debug) {
                        System.out.println("fleeingDown " + moveTimer);
                    }
                    return BattleBotArena.DOWN;
                }
            }
        }
        if (moveTimer <= 5) {

            if (me.getX() < closestBot.getX()) {//moving left or right depending on the enemy robot's X coordinates
                    /*if(botHelper.manhattanDist(me.getX(),me.getY(), closestDeadBot.getX(),closestDeadBot.getY()) <50 && closestDeadBot.getX() > me.getX()) {//checks if a dead bot is to the right of the bot
                        if (me.getY() - closestDeadBot.getY() > Bot.RADIUS && me.getY() - closestDeadBot.getY() > Bot.RADIUS*-1) {//checks if the dead bot is actually in the way
                            if (me.getY() < BattleBotArena.BOTTOM_EDGE / 2) {//checks if the bot is in the upper half of the arena
                                return BattleBotArena.DOWN;//the bot moves down if a dead bot is next to it and the bot is in the top half of the arena
                            }
                            else{
                                return BattleBotArena.UP;//bot moves up if a dead bot is next to it and the bot is in the bottom half of the screen
                            }
                        }
                    }*/
                if (debug) {
                    System.out.println("movingRight " + moveTimer);
                }
                return BattleBotArena.RIGHT;
            } else {
                //if(botHelper.manhattanDist(me.getX(),me.getY(), closestDeadBot.getX(),closestDeadBot.getY()) <50) {
                //return BattleBotArena.UP;

                if (debug) {
                    System.out.println("movingLeft " + moveTimer);
                }
                return BattleBotArena.LEFT;
            }
        }


        if (moveTimer > 5) {
                    /*if(botHelper.manhattanDist(me.getX(),me.getY(), closestDeadBot.getX(),closestDeadBot.getY()) <50 && closestDeadBot.getX() > me.getX()) {//checks if a dead bot is to the right of the bot
                        if (me.getX() - closestDeadBot.getX() > Bot.RADIUS && me.getX() - closestDeadBot.getX() > Bot.RADIUS*-1) {//checks of the dead bot is actually in the way
                            if (me.getX() < BattleBotArena.RIGHT_EDGE / 2) {//checks if the bot is the left part of the arena
                                return BattleBotArena.RIGHT;//moves right if there is a dead bot in the way
                            }
                            else{
                                return BattleBotArena.LEFT;//moves left if there is a dead bot in the way
                            }
                        }
                    }*/
            if (me.getY() < closestBot.getY()) {//moving up or down to match the enemy robot's Y coordinates
                if (debug) {
                    System.out.println("movingDown " + moveTimer);
                }
                return BattleBotArena.DOWN;
            } else {
                if (debug) {
                    System.out.println("movingUp " + moveTimer);
                }
                return BattleBotArena.UP;
            }
        }

System.out.println("doing nothing");
        return 0;
    }


    public static BotInfo[] editArray (BotInfo[] inputBotArray, int inputIndexPosition){
        BotInfo[] tempArray = new BotInfo[inputBotArray.length - 1];
        int j = 0;

        for (int i = 0; i < inputBotArray.length; i++) {
            if (i == inputIndexPosition) {
                continue;
            }
            tempArray[j] = inputBotArray[i];
            j++;
        }
        return tempArray; // an array
    }

    /**
     * Called when it is time to draw the Bot. Your Bot should be (mostly)
     * within a circle inscribed inside a square with top left coordinates
     * <i>(x,y)</i> and a size of <i>RADIUS * 2</i>. If you are using an image,
     * just put <i>null</i> for the ImageObserver - the arena has some special features
     * to make sure your images are loaded before you will use them.
     *
     * @param g The Graphics object to draw yourself on.
     * @param x The x location of the top left corner of the drawing area
     * @param y The y location of the top left corner of the drawing area
     */
    @Override
    public void draw(Graphics g, int x, int y) {
        g.setColor(Color.BLUE);
        g.fillRect(x + 2, y + 2, RADIUS * 2 - 4, RADIUS * 2 - 4);
    }

    /**
     * This method will only be called once, just after your Bot is created,
     * to set your name permanently for the entire match.
     *
     * @return The Bot's name
     */
    @Override
    public String getName() {
        return "QirongBot";
    }

    /**
     * This method is called at every time step to find out what team you are
     * currently on. Of course, there can only be one winner, but you can
     * declare and change team allegiances throughout the match if you think
     * anybody will care. Perhaps you can send coded broadcast message or
     * invitation to other Bots to set up a temporary team...
     *
     * @return The Bot's current team name
     */
    @Override
    public String getTeamName() {
        return "Team2";
    }

    /**
     * This is only called after you have requested a SEND_MESSAGE move (see
     * the documentation for <i>getMove()</i>). However if you are already over
     * your messaging cap, this method will not be called. Messages longer than
     * 200 characters will be truncated by the arena before being broadcast, and
     * messages will be further truncated to fit on the message area of the screen.
     *
     * @return The message you want to broadcast
     */
    @Override
    public String outgoingMessage() {
        return null;
    }

    /**
     * This is called whenever the referee or a Bot sends a broadcast message.
     *
     * @param botNum The ID of the Bot who sent the message, or <i>BattleBotArena.SYSTEM_MSG</i> if the message is from the referee.
     * @param msg    The text of the message that was broadcast.
     */
    @Override
    public void incomingMessage(int botNum, String msg) {

    }

    /**
     * This is called by the arena at startup to find out what image names you
     * want it to load for you. All images must be stored in the <i>images</i>
     * folder of the project, but you only have to return their names (not
     * their paths).<br><br>
     * <p>
     * PLEASE resize your images in an image manipulation
     * program. They should be squares of size RADIUS * 2 so that they don't
     * take up much memory.
     *
     * @return An array of image names you want the arena to load.
     */
    @Override
    public String[] imageNames() {
        return new String[0];
    }

    /**
     * Once the arena has loaded your images (see <i>imageNames()</i>), it
     * calls this method to pass you the images it has loaded for you. Store
     * them and use them in your draw method.<br><br>
     * <p>
     * PLEASE resize your images in an
     * image manipulation program. They should be squares of size RADIUS * 2 so
     * that they don't take up much memory.<br><br>
     * <p>
     * CAREFUL: If you got the file names wrong, the image array might be null
     * or contain null elements.
     *
     * @param images The array of images (or null if there was a problem)
     */
    @Override
    public void loadedImages(Image[] images) {

    }
}