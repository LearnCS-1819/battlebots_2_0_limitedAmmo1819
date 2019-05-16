package bots;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;
import jdk.nashorn.internal.ir.ReturnNode;

import java.awt.*;

public class JerryBot extends Bot {


    /**
     * JerryBot ended up turning out to be relatively simple. the bot dodges, shoots, and knows when it has no ammo.
     * It does not move normally when it has ammo but instead shoots enemy bots and greets bots with the same team name.
     * Once it runs out of ammo it will start to seek any dead bot across the map and try to touch it to refill ammo.
     * I scrapped my "hasAmmo" boolean method idea since some issues started to occur and it was much simpler just using
     * me.getBulletsLeft.
     */


    /**
     * This method is called at the beginning of each round. Use it to perform
     * any initialization that you require when starting a new round.
     */
    @Override
    // always sets hasAmmo to true at the start of a round because ammo is refilled
    public void newRound() {


    }


    //initialization for BotHelper class
    BotHelper botAssist = new BotHelper();


    // integer for how close a bot must be to be within shooting distance, lower value to try and be accurate
    private int shotZone = 20;

    // integer for how close a shot must be to be within evasion range.
    private int shotDangerZone = 50;


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


        // stores how much bullets we have left in an int
        int bulletsLeft = me.getBulletsLeft();

        // initializes closestBot variable to find closest living bot
        BotInfo closestBot = botAssist.findClosest(me, liveBots);

        // initializes closestDead variable to find closest dead bot
        BotInfo closestDead = botAssist.findClosest(me, deadBots);

        // initializes closestBullet variable to find closest bullet
        Bullet closestBullet = botAssist.findClosest(me, bullets);

        // firstly makes sure there are bots on screen so the rest of the code works
        if (liveBots.length > 0) {


            /**
             * evasion behavior for the bot. evasion is something that does not need to require ammo, so it is outside
             * of the other if statements.
             */
            // ensure  a bullet is active in the arena
            if (bullets.length > 0) {
                //checks if a bullet is within the danger zone, then will execute other checks
                if (botAssist.calcDistance(me.getX(), me.getY(), closestBullet.getX(), closestBullet.getY()) < shotDangerZone) {
                    // checks if the bullet is travelling horizontally
                    if (closestBullet.getXSpeed() != 0 && closestBullet.getYSpeed() == 0) {
                        // if it is horizontal, checks if my bot is above the bullet, dodging upwards if so
                        if (me.getY() + Bot.RADIUS < closestBullet.getY()) {
                            return BattleBotArena.UP;
                            // otherwise dodge down because the bot is not above the bullet
                        } else {
                            return BattleBotArena.DOWN;
                        }

                    }
                    // checks if the bullet is travelling vertically
                    if (closestBullet.getYSpeed() != 0 && closestBullet.getXSpeed() == 0) {
                        //if it is vertical, checks if my bot is to the right of the bullet, dodging right if so
                        if (me.getX() + Bot.RADIUS < closestBullet.getX()) {

                            return BattleBotArena.RIGHT;
                            // otherwise dodge left
                        } else {
                            return BattleBotArena.LEFT;
                        }
                    }
                }
            }


            /**
             * behavior for the bot if the bot has ammo. regularly checks if the bot does have ammo.
             */
            if (me.getBulletsLeft() > 0) {
                //if a bot is within the shotZone, the rest of the code will trigger.
                // if the closest bot that triggered the shotzone detection's team name is equal to Team2, send out a friendly message.
                // this is so the bot    does not fire anything if it's a friendly and also acknowledges that there is a friendly.
                if (closestBot.getTeamName().equals("Team2")) {
                    return 0;
                } else { // otherwise mark them as enemy, proceed to fireing code
                    //if enemy bot is on the same Y axis as my bot, shot is OK, and there is ammo left, continue
                    if (me.getY() < closestBot.getY() + Bot.RADIUS && me.getY() > closestBot.getY() - Bot.RADIUS) {
                        if (shotOK) {
                            // if bot is to the left of my bot, fire left, else fire to the right if bot is on the right
                            if (me.getX() > closestBot.getX()) {
                                return BattleBotArena.FIRELEFT;
                            } else if (me.getX() < closestBot.getX()) {
                                return BattleBotArena.FIRERIGHT;
                            }
                        }

                    }
                    //if enemy bot is on the same X axis as my bot, shot is ok, and there is ammo left, continue
                    if (me.getX() + Bot.RADIUS > closestBot.getX() && me.getX() + Bot.RADIUS < closestBot.getX() - Bot.RADIUS) {
                        if (shotOK) {

                            //if enemy bot is above my bot shoot up, else shoot down if bot is below my bot
                            if (me.getY() > closestBot.getY()) {
                                return BattleBotArena.FIREUP;
                            } else if (me.getY() < closestBot.getY()) {
                                return BattleBotArena.FIREDOWN;
                            }
                            // if there is no bullets left, has Ammo is set to false and code stops.
                        }
                    }
                }
            }


            /**
             * Moving away from dead bots if this bot has ammo. Originally was going to be universal, but would conflict
             * with the actions of trying to get ammo from dead bots when this bot has no ammo. Basically also helps
             * pull the bot away from a dead bot once it refills ammo.
             */
            //checks if there is dead bots on the field
            if (deadBots.length > 0) {
                //checks if the closest dead bot has no bullets
                if (closestDead.getBulletsLeft() == 0) {
                    //makes sure that the closest dead bot is within 10 pixels of the bot before attempting to move away
                    if (botAssist.calcDistance(me.getX(), me.getY(), closestDead.getX(), closestDead.getY()) < 30) {
                        //if dead bot is on the same y axis after distance being calculated
                        if (me.getY() > closestDead.getY() + Bot.RADIUS && me.getY() < closestDead.getY() - Bot.RADIUS) {
                            // if the dead bot is to the bots right, move to the left, else move to the right
                            if (me.getX() > closestDead.getX()) {
                                return BattleBotArena.RIGHT;
                            } else if (me.getX() < closestDead.getX()) {
                                return BattleBotArena.LEFT;
                            }
                        }
                        // if the dead bot is on the same x axis after distance being calculated
                        if (me.getX() > closestDead.getX() + Bot.RADIUS && me.getX() < closestDead.getX() - Bot.RADIUS) {
                            // if the dead bot is above us, move down
                            if (me.getY() > closestDead.getX()) {
                                return BattleBotArena.DOWN;
                                //if the dead bot is below us, move up
                            } else if (me.getY() < closestDead.getY()) {
                                return BattleBotArena.UP;
                            }
                        }

                    }
                }
            }

        }


        /**
         * behavior for the bot if the bot does not have ammo. regularly
         */
        // if there is no ammo, rest of this code will trigger
        if (me.getBulletsLeft() == 0) {

            // if there is dead bots on the map
            if (deadBots.length > 0) {
                // instead of having any distance calculation, this seeks for any dead bot that  is above this bot around the map
                // and moves downwards to it. otherwise moves upwards towards the dead bot
                if (me.getY() + Bot.RADIUS < closestDead.getY()) {
                    return BattleBotArena.DOWN;
                } else if (me.getY() + Bot.RADIUS > closestDead.getY()) {
                    return BattleBotArena.UP;
                }

                if (me.getX() + Bot.RADIUS < closestDead.getX()) {
                    return BattleBotArena.LEFT;
                } else if (me.getX() + Bot.RADIUS > closestDead.getX()) {
                    return BattleBotArena.RIGHT;
                }

            }

        }
        return BattleBotArena.STAY;
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
            g.setColor(Color.CYAN);
            g.fillOval(x,y, RADIUS*2, RADIUS*2);
    }

    /**
     * This method will only be called once, just after your Bot is created,
     * to set your name permanently for the entire match.
     *
     * @return The Bot's name
     */
    @Override
    public String getName() {
        return "Mavus";
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
        //returns that the team this bot is on is team 2
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
        return "sup, friend!";
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
