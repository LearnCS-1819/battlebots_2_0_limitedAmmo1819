package bots;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

import javax.management.BadAttributeValueExpException;
import java.awt.*;

/**
 * The MattBot is a bot which intelligently targets other bots and avoids incoming
 * projectiles within a specific distance. It follows the closest bot both vertically
 * and horizontally until MattBot has a clear shot. During many plays, it waits for
 * other bots to come closer to accurately eliminate enemies. Some of the downsides
 * of MattBot include not regaining ammunition, "hesitation" or stops shooting/moving
 * when closest bot runs out of ammunition, gets stuck against deadbots occasionally,
 * and has a slow program execution time.
 * @author Matt
 * @version v1.0
 */
public class MattBot extends Bot {

    // Create new instant of BotHelper, used for determining where the closest bot/bullet is
    BotHelper helper = new BotHelper();

    // Stores current image
    Image image;

    // Constant vars
    private static int AVOIDANCE_FEATHER = 20; // The bigger the number, the bigger the safe zone that the bot will try to protect around itself from other projectiles
    private static int AVOIDANCE_TRIGGER = 90; // The distance at which the bot will start to avoid a bullet at

    /**
     * This method is called at the beginning of each round. Use it to perform
     * any initialization that you require when starting a new round.
     */
    @Override
    public void newRound() { }

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
     * @param deadbots An array of BotInfo objects for the dead Bots littering the arena
     * @param bullets  An array of all Bullet objects currently in play
     * @return A legal move (use the constants defined in BattleBotArena)
     */
    @Override
    public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots, BotInfo[] deadbots, Bullet[] bullets) {

        // Make sure there are at least 1 enemy bot and 1 bullet in the arena
        if (liveBots.length > 0) {

            BotInfo closest = helper.findClosest(me, liveBots); // determine closest enemy bot

            //prevents MattBot from getting stuck on edge of world
            if (me.getX() <= BattleBotArena.LEFT_EDGE + 1) {
                return BattleBotArena.RIGHT;
            }
            if (me.getX() >= BattleBotArena.RIGHT_EDGE - 26) {
                return BattleBotArena.LEFT;
            }
            if (me.getY() <= BattleBotArena.TOP_EDGE + 1) {
                return BattleBotArena.DOWN;
            }
            if (me.getY() >= BattleBotArena.BOTTOM_EDGE - 26) {
                return BattleBotArena.UP;
            }

             //**********************************************************************************************************************************************************
             // Handle bullet avoidance
             //**********************************************************************************************************************************************************
            if (bullets.length > 0) {
                Bullet closestBullet = helper.findClosest(me, bullets); // determine closest bullet

                // find the distance using pythagorean theorem of closest bullet
                double bulletDistance = calcDistance(closestBullet.getX(), closestBullet.getY(), me.getX(), me.getY());

                if (bulletDistance <= AVOIDANCE_TRIGGER) { // if bullet is within 100 pixels
                    if (closestBullet.getYSpeed() != 0) {       //if bullet is moving vertically
                        if (me.getX() + (Bot.RADIUS * 2) >= closestBullet.getX() - AVOIDANCE_FEATHER && me.getX() <= closestBullet.getX() + (6) + AVOIDANCE_FEATHER) {
                            if (Math.abs(me.getX() - BattleBotArena.LEFT) < Bot.RADIUS * 2) {
                                return BattleBotArena.RIGHT;
                            } else if (Math.abs(me.getX() - BattleBotArena.RIGHT) < Bot.RADIUS * 2) {
                                return BattleBotArena.LEFT;
                            }
                            if (me.getX() < closestBullet.getX()) {
                                return BattleBotArena.LEFT;
                            } else if (me.getX() > closestBullet.getX()) {
                                return BattleBotArena.RIGHT;
                            }
                        }
                        //tests if the displacement in the y is more than that in the x
                    } else if (closestBullet.getXSpeed() != 0) {        //if bullet is moving horizontally
                        if (me.getY() + (Bot.RADIUS * 2) >= closestBullet.getY() - AVOIDANCE_FEATHER && me.getY() <= closestBullet.getY() + (6) + AVOIDANCE_FEATHER) {
                            if (Math.abs(me.getY() - BattleBotArena.TOP_EDGE) < Bot.RADIUS * 2) {
                                return BattleBotArena.DOWN;
                            } else if (Math.abs(me.getY() - BattleBotArena.BOTTOM_EDGE) < Bot.RADIUS * 2) {
                                return BattleBotArena.UP;
                            }
                            if (me.getY() < closestBullet.getY()) {
                                return BattleBotArena.UP;
                            } else if (me.getY() > closestBullet.getY()) {
                                return BattleBotArena.DOWN;
                            }
                        }
                    }
                }
                //**********************************************************************************************************************************************************
                // Handle movement
                //**********************************************************************************************************************************************************
                else if (bulletDistance >= AVOIDANCE_TRIGGER) {           //if no bullet is detected in danger zone
                    if (closest.getTeamName() != me.getTeamName()) {        //avoid friendly fire
                        //checks is bot and closest bot is aligned either vertically or horizontally
                        if (me.getY() + (Bot.RADIUS * 2) >= closest.getY() && me.getY() <= closest.getY() + (Bot.RADIUS * 2) || me.getX() + (Bot.RADIUS * 2) >= closest.getX() && me.getX() <= closest.getX() + (Bot.RADIUS * 2)) {
                            if (me.getY() + (Bot.RADIUS * 2) >= closest.getY() && me.getY() <= closest.getY() + (Bot.RADIUS * 2)) {
                                if (closest.getX() < me.getX()) {
                                    return BattleBotArena.LEFT;
                                } else {
                                    return BattleBotArena.RIGHT;
                                }
                            } else if (me.getX() + (Bot.RADIUS * 2) >= closest.getX() && me.getX() <= closest.getX() + (Bot.RADIUS * 2)) {
                                if (closest.getY() < me.getY()) {
                                    return BattleBotArena.UP;
                                } else {
                                    return BattleBotArena.DOWN;
                                }
                            }
                        } else {        //if not aligned, align MattBot
                            if (Math.abs(me.getY() - closest.getY()) > Math.abs(me.getX() - closest.getX())) {
                                if (me.getX() < closest.getX()) {
                                    return BattleBotArena.RIGHT;
                                } else if (me.getX() > closest.getX()) {
                                    return BattleBotArena.LEFT;
                                }
                            } else if (Math.abs(me.getY() - closest.getY()) < Math.abs(me.getX() - closest.getX())) {
                                if (me.getY() < closest.getY()) {
                                    return BattleBotArena.DOWN;
                                } else if (me.getY() > closest.getY()) {
                                    return BattleBotArena.UP;
                                }
                            }
                        }
                    }
                }
            }


            //**********************************************************************************************************************************************************
            // Handle firing
            //**********************************************************************************************************************************************************
            if (closest.getTeamName() != me.getTeamName()) {        //avoid friendly fire
                if (me.getY() + (Bot.RADIUS*2) >= closest.getY() && me.getY() <= closest.getY() + (Bot.RADIUS*2)) { // make sure the enemy bot is directly to the right or left of our bot
                    if (shotOK) {
                        // if the enemy bot is to the left of us, shoot left, otherwise shoot right
                        if (closest.getX() < me.getX()) {
                            return BattleBotArena.FIRELEFT;
                        } else {
                            return BattleBotArena.FIRERIGHT;
                        }
                    }
                } else if (me.getX() + (Bot.RADIUS * 2) >= closest.getX() && me.getX() <= closest.getX() + (Bot.RADIUS * 2)) { // if the enemy bot is not to the right or left, check up and down
                    if (shotOK) {
                        // if the enemy bot is above us, shoot up, otherwise shoot down
                        if (closest.getY() < me.getY()) {
                            return BattleBotArena.FIREUP;
                        } else {
                            return BattleBotArena.FIREDOWN;
                        }
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Used to determine the distance of an another point in the arena.
     * This is done by using Pythagorean Theorem
     *
     * @param x1 The x location of the first point (x1, y1)
     * @param y1 The y location of the first point (x1, y1)
     * @param x2 The x location of the second point (x2, y2)
     * @param y2 The x location of the second point (x2, y2)
     * @return The distance in units
     */
    public double calcDistance(double x1, double y1, double x2, double y2) {
        double displacementX = Math.abs(x1 - x2);
        double displacementY = Math.abs(y1 - y2);

        return Math.sqrt(Math.pow(displacementX, 2) + Math.pow(displacementY, 2));
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
        g.drawImage(image, x, y, Bot.RADIUS*2, Bot.RADIUS*2, null);
    }

    /**
     * This method will only be called once, just after your Bot is created,
     * to set your name permanently for the entire match.
     *
     * @return The Bot's name
     */
    @Override
    public String getName() {
        return "MattBot";
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
        return "Team 1";
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
        String[] paths = {"s_down.png"};
        return paths;
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
        if (images != null) {
            image = images[0];
        }
    }
}