package bots;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

import java.awt.*;

/**
 * The KylerBot is a bot which intelligently targets other bots and avoids incoming
 * projectiles. It does this determining when a bot is close enough to be shot and
 * follows the targetted bot until it can make a clear shot. My bot only moves up
 * and down to track down other bots, for increased efficiency. It avoids bullets by
 * determining which direction the bullet is travelling, and moving accordingly to
 * avoid being hit.
 *
 * @author Kyler Swanson
 * @version v1.0
 */
public class KylerBot extends Bot {

    // Create new instant of BotHelper, used for determining where the closest bot/bullet is
    BotHelper helper = new BotHelper();

    // Stores current image
    Image image;

    // Constant vars
    private static int AVOIDANCE_FEATHER = 20; // The bigger the number, the bigger the safe zone that the bot will try to protect around itself from other projectiles
    private static int AVOIDANCE_TRIGGER = 90; // The distance at which the bot will start to avoid a bullet at
    private static int MOVEMENT_AVOIDANCE_FEATHER = 2; // The multiplier which determines how far a bullet should be before it stops moving relative to the bot's velocity

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
     * @param deadBots An array of BotInfo objects for the dead Bots littering the arena
     * @param bullets  An array of all Bullet objects currently in play
     * @return A legal move (use the constants defined in BattleBotArena)
     */
    @Override
    public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {

        // Make sure there are at least 1 enemy bot and 1 bullet in the arena
        if (liveBots.length > 0) {
            // Determines closest enemy bot
            BotInfo closest = helper.findClosest(me, liveBots);

            // Determines closest dead bot/tombstone
            BotInfo closestDead = null;
            if (deadBots.length > 0) {
                closestDead = helper.findClosest(me, deadBots);
            }

            /**
             * Handle bullet avoidance
             */

            if (bullets.length > 0) {
                Bullet closestBullet = helper.findClosest(me, bullets); // determine closest bullet

                // find the distance using pythagorean theorem of closest bullet
                double bulletDistance = calcDistance(closestBullet.getX(), closestBullet.getY(), me.getX(), me.getY());

                if (bulletDistance <= AVOIDANCE_TRIGGER) { // if bullet is within 100 pixels
                    if (closestBullet.getXSpeed() != 0) { // if the bullet is moving left/right
                        // if bullet's trajectory will collide with our bot, move up
                        if (closestBullet.getY() >= me.getY() - AVOIDANCE_FEATHER && closestBullet.getY() <= me.getY() + (Bot.RADIUS * 2) + AVOIDANCE_FEATHER) {
                            // if bullet is below, move up, otherwise move down
                            if (closestBullet.getY() > me.getY()) {
                                return BattleBotArena.UP;
                            } else {
                                return BattleBotArena.DOWN;
                            }
                        }
                    } else if (closestBullet.getYSpeed() != 0) { // if the bullet is moving up/down
                        // if bullet's trajectory will collide with our bot, move right
                        if (closestBullet.getX() <= me.getX() + (Bot.RADIUS * 2) + AVOIDANCE_FEATHER && closestBullet.getX() >= me.getX() - AVOIDANCE_FEATHER) {
                            // if bullet is to the right, move left, otherwise move right
                            if (closestBullet.getX() > me.getX()) {
                                return BattleBotArena.LEFT;
                            } else {
                                return BattleBotArena.RIGHT;
                            }
                        }
                    }
                }
            }

            /**
             * Collect ammo
             */
            if (me.getBulletsLeft() < 5) {
                if (deadBots.length > 0) {
                    BotInfo deadBot = findClosestWithLoot(me, deadBots);

                    if (deadBot != null) {
                        // if the deadBot is to the left of us
                        if (me.getX() >= deadBot.getX() + (Bot.RADIUS)) {
                            // if we are hitting a dead bot that isn't our target, move down
                            if (calcDistance(me.getX(), me.getY(), closestDead.getX(), closestDead.getY()) < 50) {
                                if (closestDead.getBotNumber() != deadBot.getBotNumber()) {
                                    if (me.getY() + (Bot.RADIUS * 2) >= closestDead.getY() && me.getY() <= closestDead.getY() + (Bot.RADIUS * 2)) {
                                        return BattleBotArena.DOWN;
                                    }
                                }
                            }

                            return BattleBotArena.LEFT;
                        }

                        if (me.getX() + (Bot.RADIUS) <= deadBot.getX()) {
                            // if we are hitting a dead bot that isn't our target, move down
                            if (calcDistance(me.getX(), me.getY(), closestDead.getX(), closestDead.getY()) < 50) {
                                if (closestDead.getBotNumber() != deadBot.getBotNumber()) {
                                    if (me.getY() + (Bot.RADIUS * 2) >= closestDead.getY() && me.getY() <= closestDead.getY() + (Bot.RADIUS * 2)) {
                                        return BattleBotArena.DOWN;
                                    }
                                }
                            }

                            return BattleBotArena.RIGHT;
                        }

                        // move up if loot is directly above
                        if (me.getY() > deadBot.getY() + (Bot.RADIUS * 2)) {
                            return BattleBotArena.UP;
                        }

                        // move down if loot is directly below
                        if (me.getY() + (Bot.RADIUS * 2) < deadBot.getY()) {
                            return BattleBotArena.DOWN;
                        }
                    }
                }
            }

            /**
             * Handle combat
             */
            if (closest.getTeamName() != me.getTeamName()) {
                if (me.getY() + Bot.RADIUS >= closest.getY() && me.getY() + Bot.RADIUS <= closest.getY() + (Bot.RADIUS * 2)) { // make sure the enemy bot is directly to the right or left of our bot
                    if (shotOK) {
                        // if the enemy bot is to the left of us, shoot left, otherwise shoot right
                        if (closest.getX() < me.getX()) {
                            // check for obstacle that could block firing
                            if (closestDead != null) {
                                if (me.getY() + Bot.RADIUS >= closestDead.getY() && me.getY() + Bot.RADIUS <= closestDead.getY() + (Bot.RADIUS * 2)) {
                                    if (closestDead.getX() < me.getX()) {
                                        return BattleBotArena.STAY;
                                    }
                                }
                            }
                            return BattleBotArena.FIRELEFT;
                        } else {
                            // check for obstacle that could block firing
                            if (closestDead != null) {
                                if (me.getY() + Bot.RADIUS >= closestDead.getY() && me.getY() + Bot.RADIUS <= closestDead.getY() + (Bot.RADIUS * 2)) {
                                    if (closestDead.getX() > me.getX()) {
                                        return BattleBotArena.STAY;
                                    }
                                }
                            }
                            return BattleBotArena.FIRERIGHT;
                        }
                    }
                } else if (me.getX() + Bot.RADIUS >= closest.getX() && me.getX() + Bot.RADIUS <= closest.getX() + (Bot.RADIUS * 2)) { // if the enemy bot is not to the right or left, check up and down
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

            /**
             * Handle movement
             */

            if (closest.getTeamName() != me.getTeamName()) {
                if (closest.getY() > me.getY()) { // if the closest bot is below us, try to move down
                    // if there is a bullet below us, don't move
                    if (bullets.length > 0) {
                        Bullet closestBullet = helper.findClosest(me, bullets); // determine closest bullet

                        // find the distance using pythagorean theorem of closest bullet
                        double bulletDistance = calcDistance(closestBullet.getX(), closestBullet.getY(), me.getX(), me.getY());

                        if (closestBullet.getY() > me.getY()
                                && closestBullet.getY() < me.getY() + (BattleBotArena.BOT_SPEED * MOVEMENT_AVOIDANCE_FEATHER) + (Bot.RADIUS * 2)
                                && closestBullet.getXSpeed() != 0) {

                            return BattleBotArena.STAY;
                        }
                    }

                    // check for obstacle that could block our movement, then move out of the way
                    if (closestDead != null) {
                        double deadBotDistance = calcDistance(closestDead.getX(), closestDead.getY(), me.getX(), me.getY());
                        if (deadBotDistance <= 100) {
                            if (closestDead.getY() > me.getY()) {
                                if (me.getX() + Bot.RADIUS > closestDead.getX() + Bot.RADIUS) {
                                    if (me.getX() < closestDead.getX() + (Bot.RADIUS * 2) + 5) {
                                        return BattleBotArena.RIGHT;
                                    }
                                } else if (me.getX() + Bot.RADIUS < closestDead.getX() + Bot.RADIUS) {
                                    if (me.getX() + (Bot.RADIUS * 2) + 5 > closestDead.getX()) {
                                        return BattleBotArena.LEFT;
                                    }
                                }
                            }
                        }
                    }
                    return BattleBotArena.DOWN;
                } else if (closest.getY() < me.getY()) { // if the closest bot is above us, try to move up
                    // if there is a bullet above us, don't move
                    if (bullets.length > 0) {
                        Bullet closestBullet = helper.findClosest(me, bullets); // determine closest bullet

                        // find the distance using pythagorean theorem of closest bullet
                        double bulletDistance = calcDistance(closestBullet.getX(), closestBullet.getY(), me.getX(), me.getY());

                        if (closestBullet.getY() > me.getY() - (BattleBotArena.BOT_SPEED * MOVEMENT_AVOIDANCE_FEATHER)
                                && closestBullet.getY() < me.getY()
                                && closestBullet.getXSpeed() != 0) {

                            return BattleBotArena.STAY;
                        }
                    }

                    // check for obstacle that could block our movement, then move out of the way
                    if (closestDead != null) {
                        double deadBotDistance = calcDistance(closestDead.getX(), closestDead.getY(), me.getX(), me.getY());
                        if (deadBotDistance <= 100) {
                            if (closestDead.getY() < me.getY()) {
                                if (me.getX() + Bot.RADIUS > closestDead.getX() + Bot.RADIUS) {
                                    if (me.getX() < closestDead.getX() + (Bot.RADIUS * 2) + 5) {
                                        return BattleBotArena.RIGHT;
                                    }
                                } else if (me.getX() + Bot.RADIUS < closestDead.getX() + Bot.RADIUS) {
                                    if (me.getX() + (Bot.RADIUS * 2) + 5 > closestDead.getX()) {
                                        return BattleBotArena.LEFT;
                                    }
                                }
                            }
                        }
                    }
                    return BattleBotArena.UP;
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
     * Returns BotInfo of bot which is close and contains bullets to collect.
     * This is primarily for looting deadBots in order to have bullets when
     * the bot is out.
     *
     * @param me The BotInfo of the bot we are searching from
     * @param loot An array which contains bots which are dead
     * @return The closest BotInfo with bullets remaining
     */
    public BotInfo findClosestWithLoot(BotInfo me, BotInfo[] loot) {
        BotInfo closestWithLoot = loot[0];
        double closestDistanceWithLoot = Integer.MAX_VALUE;

        for (int i = 0; i < loot.length; i++) {
            double distance = calcDistance(me.getX(), me.getY(), loot[i].getX(), loot[i].getY());

            if (distance < closestDistanceWithLoot && loot[i].getBulletsLeft() > 10) {
                closestWithLoot = loot[i];
                closestDistanceWithLoot = distance;
            }
        }

        if (closestWithLoot.getBulletsLeft() > 0) {
            return closestWithLoot;
        } else {
            return null;
        }
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
        return "KylerBot";
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
        String[] paths = {"starfish4.png"};
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