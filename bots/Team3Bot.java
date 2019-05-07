package bots;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Team3Bot extends Bot {

    private BotHelper helper = new BotHelper();
    private Boolean closeBullet = false; //Boolean value to check if a bullet is close
    private static int tooClose = Bot.RADIUS * 5; //Distance at which a bullet is too close to ignore
    private static int shotRange;

    // Variables to hold the last X & Y coordinates of the bot
    private double lastX, lastY;

    // Variable to keep a track of the number of times the bot gets stuck
    private int stuckCounter;

    // If my bot is stuck
    private boolean isStuck;

    // Checks if my bot is all out of bullets
    private boolean needBullets;

    // Checks if the targeted bot is out of bullets
    private boolean outOfBullets;

    // List to hold dead bots and remove ones without ammo
    private ArrayList<BotInfo> deadBotList = new ArrayList<BotInfo>();

    private Image image = null; //bot image

    /**
     * This method is called at the beginning of each round. Use it to perform
     * any initialization that you require when starting a new round.
     */
    @Override
    public void newRound() {

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
        int move = 0;
        int mode = getMode(liveBots);
        if (mode == 2) move = attack(me, liveBots, bullets, shotOK, deadBots);
        else if (mode == 1) move = defense(me, liveBots, bullets, shotOK, deadBots);
        else move = group(me, liveBots, bullets, shotOK, deadBots);
        return move;
    }

    private int getMode(BotInfo[] liveBots){
        int numAlive = liveBots.length;
        int teamMembers = 1;
        for (BotInfo bot : liveBots){
            if (bot.getTeamName() == "Team 3") teamMembers++;
        }


        if (numAlive <= 8 && teamMembers > 1){
            return 2;
        } else if (numAlive > 8){
            return 0;
        } else{
            return 1;
        }
    }


    /**
     * @param botArrIn
     * @param indexIn
     * @return
     */
    private static BotInfo[] removeBotIndex(BotInfo[] botArrIn,
                                            int indexIn) {

        // If the array is empty or the index is not in array range
        // return the original array
        if (botArrIn == null || indexIn < 0 || indexIn >= botArrIn.length) {
            return botArrIn;
        }

        // Creating another array of size one less
        BotInfo[] copiedArr = new BotInfo[botArrIn.length - 1];

        // Copy the elements except the index
        // from original array to the other array
        for (int i = 0, k = 0; i < botArrIn.length; i++) {

            // if the index reaches the element asked to be removed
            // just do nothing and continue copying the array excluding
            // that index
            if (i == indexIn) {
                continue;
            }

            // Continue adding elements to copied array as long as not landing on the index
            // asked to be removed
            copiedArr[k++] = botArrIn[i];

        }

        // Returning final array excluding removed index
        return copiedArr;
    }


    /**
     * @param botIn
     * @param botsIn
     * @return
     */
    private static int getIndex(BotInfo botIn, BotInfo[] botsIn) {
        for (int i = 0; i < botsIn.length; i++) {
            if (botIn.getName().equals(botsIn[i].getName())) {
                return i;
            }
        }
        return 0;
    }

    private int defense(BotInfo me, BotInfo[] liveBots, Bullet[] bullets, Boolean shotOK, BotInfo[] deadBots){
        // Variable in charge of checking if a bullet is close to my bot
        closeBullet = false;

        // Finding the closest bot to my bot
        BotInfo closestBot = helper.findClosest(me, liveBots);


        //************************************************************************************************
        // Finding the closest non-teammate bot
        //************************************************************************************************

        // If the found bot is a teammate
        while (closestBot.getTeamName().equals("Team 3")) {
            // Remove teammate bot from liveBots
            liveBots = removeBotIndex(liveBots, getIndex(closestBot, liveBots));
            // Try generating the closest bot from the updated array
            // once more
            closestBot = helper.findClosest(me, liveBots);
        }


        //************************************************************************************************
        // Defining variables for later calculations
        //************************************************************************************************

        // Calculating the X displacement between my bot and the targeted bot
        double dispX = helper.calcDisplacement(me.getX() + Bot.RADIUS, closestBot.getX());

        // Calculating the Y displacement between my bot and the targeted bot
        double dispY = helper.calcDisplacement(me.getY() + Bot.RADIUS, closestBot.getY());

        // Calculating the distance between my bot and the targeted bot
        double disFromBot = helper.calcDistance(me.getX() + Bot.RADIUS, me.getY() +
                Bot.RADIUS, closestBot.getX(), closestBot.getY());

        //************************************************************************************************
        // Based on the number of bullets the targeted bot has, getting closer to the bot if
        // the number is 0 to maximize the bot's chances to hit its target
        //************************************************************************************************

        // Changing shooting range based on the number of bullets the targeted bot holds
        if (me.getBulletsLeft() > 0 && closestBot.getBulletsLeft() == 0) {
            shotRange = 50;
        } else {
            shotRange = 120;
        }

        //************************************************************************************************
        // Looping through all bullets in the world instead of just getting the closest bullet
        // (in case 2+ bullets are approaching the bot simultaneously) and dodging them.
        //************************************************************************************************

        for (Bullet bullet : bullets) {

            // Trying to dodge when a bullet is shot and is within the danger zone range
            if (helper.calcDistance(me.getX(), me.getY(), bullet.getX(), bullet.getY()) <= tooClose) {
                // A bullet is nearby
                closeBullet = true;

                // If bullet is approaching from the right or left
                if (bullet.getX() >= me.getX() + Bot.RADIUS || bullet.getX() + Bot.RADIUS <= me.getX()) {

                    // Ensuring that only bullets that move horizontally are targeted
                    if (bullet.getXSpeed() != 0 && bullet.getYSpeed() == 0) {

                        // If my bot is above the bullet
                        if (me.getY() + Bot.RADIUS > bullet.getY()) {
                            // Dodging up
                            return BattleBotArena.DOWN;
                        }

                        // If my bot is below the bullet
                        else {
                            // Dodging down
                            return BattleBotArena.UP;
                        }
                    }
                }


                // If bullet is approaching from above or below
                if (bullet.getY() >= me.getY() + Bot.RADIUS || bullet.getY() + Bot.RADIUS <= me.getY()) {

                    // Ensuring that only bullets that move vertically are targeted
                    if (bullet.getYSpeed() != 0 && bullet.getXSpeed() == 0) {

                        // If the bullet is approaching from above and to my left
                        if (me.getX() + Bot.RADIUS > bullet.getX()) {

                            // Moving in opposite direction
                            return BattleBotArena.RIGHT;
                        }
                        // If the bullet is approaching from above and to my right
                        else {

                            // Moving in opposite direction
                            return BattleBotArena.LEFT;
                        }
                    }
                }
            }

        }

        //************************************************************************************************
        // Fetching bullets when running all out
        //************************************************************************************************

        // If my bot's number of bullets is 0 and is both not stuck and there is not
        // a bullet close by
        if (me.getBulletsLeft() == 0 && !isStuck && !closeBullet) {

            // Populating array list with all the dead bots in the arena
            for (BotInfo deadBot : deadBots) {
                Collections.addAll(deadBotList, deadBot);
            }

            // Looping through array list holding all the dead bots in the
            // arena
            for (BotInfo deadBot : deadBotList) {
                // Ensuring the selected bot has more than 0 bullets
                if (deadBot.getBulletsLeft() > 0) {
                    // If the dead bot is not aligned vertically with my bot
                    if (!(me.getX() >= deadBot.getX() - 13
                            && me.getX() <= deadBot.getX() + 13)) {
                        // While my bot is not stuck
                        if (!isStuck) {
                            // Since my bot need bullets, assigning to true
                            needBullets = true;

                            // Calculating the X displacement between the dead bot and my bot
                            // and aligning my bot with the dead bot on the Y axis by moving it
                            // left or right depending on the dead bot's position
                            if (helper.calcDisplacement(me.getX() + Bot.RADIUS, deadBot.getX()) > 0) {
                                return BattleBotArena.RIGHT;
                            } else {
                                return BattleBotArena.LEFT;
                            }

                            // If my bot is stuck, prioritizing breaking free
                        } else {
                            needBullets = false;
                        }

                        // If aligned over the Y axis, moving up or down depending on the dead bot's
                        // position to loot bullets
                    } else {
                        if (helper.calcDisplacement(me.getY() + Bot.RADIUS, deadBot.getY()) > 0) {
                            return BattleBotArena.DOWN;

                        } else if (helper.calcDisplacement(me.getY() + Bot.RADIUS, deadBot.getY()) < 0) {
                            return BattleBotArena.UP;
                        }
                    }

                    // If not dead bots with bullets are found, removing bots from list and staying
                    // in the same spot to avoid death
                } else {
                    if (deadBotList.size() > 0) deadBotList.remove(deadBot);
                    return BattleBotArena.STAY;
                }
            }
            // If my bot has more than 0 bullets, no need to fetch bullets
        } else {
            needBullets = false;
        }

        //************************************************************************************************
        // If my bot is stuck at either edge of the screen, loosing it free by moving it to the
        // opposite direction.
        //************************************************************************************************

        // Ensuring that no bullets are nearby before checking if the bot is stuck
        if (!closeBullet && helper.calcDistance(me.getX(), me.getY(), closestBot.getX(), closestBot.getY()) > 30) {

            // If stuck at the left edge of the screen, moving to the right
            if (me.getX() <= BattleBotArena.LEFT_EDGE + 5) {
                return BattleBotArena.RIGHT;
            }

            // If stuck at the right edge of the screen, moving to the left
            if (me.getX() >= BattleBotArena.RIGHT_EDGE - 35) {
                return BattleBotArena.LEFT;

                // If stuck at the top edge of the screen, moving down
            } else if (me.getY() <= BattleBotArena.TOP_EDGE + 5) {
                return BattleBotArena.DOWN;

                // If stuck at the bottom edge of the screen, moving up
            } else if (me.getY() >= BattleBotArena.BOTTOM_EDGE - 35) {
                return BattleBotArena.UP;
            }
        }

        //************************************************************************************************
        // If my bot is stuck to another bot, moving in opposite direction.
        //************************************************************************************************

        // As long as my bot is not out of bullets
        if (!outOfBullets) {
            // Incrementing stuck counter if my bot stays in the same spot
            // less than 15 times
            if (me.getX() == lastX && me.getY() == lastY && stuckCounter < 15) {
                stuckCounter++;

                // If my bot is not stuck, resetting counter and setting
                // boolean to false
            } else {
                stuckCounter = 0;
                isStuck = false;
            }

            // If my bot got stuck 10 times and no bullets are near by
            if (stuckCounter > 10 && !closeBullet) {
                // My bot is stuck
                isStuck = true;

                // If the distance between my bot and the targeted bot is too small,
                // which means that my bot is stuck, trying to get unstuck
                if (helper.calcDistance(me.getX(), me.getY(), closestBot.getX(),
                        closestBot.getY()) > Bot.RADIUS * 2 + 10 ||
                        closestBot.isOverheated()) {

                    // If the targeted bot is aligned horizontally with my bot
                    if (!(me.getX() >= closestBot.getX() - 13
                            && me.getX() <= closestBot.getX() + 13)) {

                        //************************************************************************************
                        // Depending on the targeted bot's position, going the opposite
                        // direction and trying to get my bot unstuck
                        //************************************************************************************

                        // If my bot is above the targeted bot, moving in opposite direction
                        // as long as not at the top corner of the arena
                        if (me.getY() + Bot.RADIUS > closestBot.getY()) {
                            if (!(me.getY() <= BattleBotArena.TOP_EDGE + 5)) {
                                return BattleBotArena.DOWN;

                                // If my bot is at the top corner of the arena, moving down
                            } else {
                                return BattleBotArena.UP;
                            }


                            // If my bot is below the targeted bot, moving in opposite direction
                            // as long as not at the bottom corner of the arena
                        } else {
                            if (!(me.getY() >= BattleBotArena.BOTTOM_EDGE - 35)) {
                                return BattleBotArena.DOWN;

                                // If my bot is at the bottom corner of the arena, moving up
                            } else {
                                return BattleBotArena.UP;
                            }
                        }
                    }

                    // If the distance is too great, which means the my bot is stuck to a
                    // dead bot
                } else {

                    // Moving my bot in the opposite direction
                    if (me.getX() + Bot.RADIUS > closestBot.getX()) {
                        return BattleBotArena.RIGHT;
                    } else {
                        return BattleBotArena.LEFT;
                    }
                }
            }
        }

        // Storing the current X & Y coordinates of my bot to later compare
        // and determine whether my bot is stuck
        lastX = me.getX();
        lastY = me.getY();

        //************************************************************************************************
        // My bot's movement strategy which incorporates following other bots, aligning with them over
        // the Y axis and shooting when within shooting distance to maximize hitting rates
        //************************************************************************************************

        // If my bot is not stuck and there is no bullet near by
        if (!isStuck && !closeBullet) {
            // If my bot is within shooting range from the targeted bot
            if (disFromBot < shotRange && !needBullets) {

                // If my bot is horizontally aligned with the targeted (both bots have same x value)
                if (me.getY() >= closestBot.getY() - 5
                        && me.getY() <= closestBot.getY() + Bot.RADIUS * 2 + 5) {

                    // If the targeted bot is to my left
                    if (shotOK && me.getX() + Bot.RADIUS > closestBot.getX() + Bot.RADIUS) {
                        // Shooting left
                        return BattleBotArena.FIRELEFT;

                        // If the bot is to my right
                    } else if (shotOK && me.getX() + Bot.RADIUS < closestBot.getX() + Bot.RADIUS) {
                        // Shooting right
                        return BattleBotArena.FIRERIGHT;
                    }

                    // If my bot is not aligned horizontally with the targeted bot
                } else {

                    // if my bot and the targeted bot are aligned on the Y axis (both bots have the same Y values)
                    if (me.getX() >= closestBot.getX() - 20
                            && me.getX() <= closestBot.getX() + 20) {

                        // If the targeted bot is above me
                        if (me.getY() + Bot.RADIUS >= closestBot.getY() + Bot.RADIUS) {
                            // Shooting upwards
                            return BattleBotArena.FIREUP;


                            // If the targeted bot is below me
                        } else if (me.getY() + Bot.RADIUS < closestBot.getY() + Bot.RADIUS) {
                            // Shooting downwards
                            return BattleBotArena.FIREDOWN;
                        }
                    }
                }

                // If not within shooting range, following bots until within range
            } else {
                // If the targeted bot and my bot are not aligned vertically
                if (!(me.getX() >= closestBot.getX() - 20
                        && me.getX() <= closestBot.getX() + 20)) {

                    // As long as the targeted bot's last move is not up or down and my bot is not stuck
                    if (!(closestBot.getLastMove() == BattleBotArena.UP) ||
                            !(closestBot.getLastMove() == BattleBotArena.DOWN)
                                    && !isStuck) {

                        // Following the bot along the X axis by going left and right based
                        // on the direction the targeted bot is facing me from as long as
                        // my bot is NOT out of bullets, there is not bullet nearby,
                        // is not stuck and the distance between the targeted bot and my
                        // bot is more than 45
                        if (!needBullets && !closeBullet && !isStuck && disFromBot > 45) {
                            // If the targeted bot is facing my bot from the right side
                            if (dispX > 0) {
                                // Following bot by going right
                                return BattleBotArena.RIGHT;

                                // If the targeted bot is facing my bot from the left side
                            } else if (dispX < 0) {
                                // Following bot by going left
                                return BattleBotArena.LEFT;

                                // If the distance is 0, moving down to get within shooting range
                            } else {
                                return BattleBotArena.DOWN;
                            }

                            // If the distance between the targeted bot and my bot is smaller than 45
                        } else {
                            // If the targeted bot and my bot are aligned over the Y axis
                            if (me.getX() >= closestBot.getX() - 20
                                    && me.getX() <= closestBot.getX() + 20) {

                                // If the targeted bot is facing me from the left
                                if (shotOK && me.getX() + Bot.RADIUS > closestBot.getX() + Bot.RADIUS) { // farther apart from the bot
                                    // Shooting to its direction
                                    return BattleBotArena.FIRELEFT;

                                    // If the targeted bot is facing me from the right
                                } else if (shotOK && me.getX() + Bot.RADIUS < closestBot.getX() + Bot.RADIUS) {
                                    // Shooting to its direction
                                    return BattleBotArena.FIRERIGHT;
                                }
                            }
                        }
                    }

                    // As long as the targeted bot and my bot are not aligned vertically,
                    // aligning my bot with the targeted bot depending on its direction
                } else {
                    // If my bot is not out of bullets and there is no bullet near by
                    if (!needBullets && !closeBullet) {

                        // If the targeted bot is below me
                        if (dispY > 0) {
                            // Moving accordingly down
                            return BattleBotArena.DOWN;

                            // If the targeted bot is above me
                        } else if (dispY < 0) {
                            // Moving accordingly up
                            return BattleBotArena.UP;
                        }
                    }
                }

            }
        }
        return 0;
    }

    private int attack(BotInfo me, BotInfo[] liveBots, Bullet[] bullets, Boolean shotOK, BotInfo[] deadBots){

        //Bullet dodging
        //Check every bullet
        for (Bullet bullet : bullets){

            //Check if the bullet is too close
            if ( (helper.calcDistance(bullet.getX(), bullet.getY(), me.getX(), me.getY()) ) <= tooClose){
                closeBullet = true;

                //Dodge accordingly

                //Dodging horizontal bullets
                if ( (((bullet.getX() >= me.getX() + Bot.RADIUS) && (bullet.getXSpeed() < 0)) || ((bullet.getX() + Bot.RADIUS <= me.getX()) && (bullet.getXSpeed() > 0)))
                        && ( (bullet.getXSpeed() != 0) && (bullet.getYSpeed() == 0) )
                ){
                    //Dodge up
                    if (me.getY() + Bot.RADIUS > bullet.getY()) {

                        return BattleBotArena.DOWN;
                    }

                    //Dodge down
                    else {

                        return BattleBotArena.UP;
                    }
                }

                //Dodging vertical bullets
                if ( ( ((bullet.getY() >= me.getY() + Bot.RADIUS) && (bullet.getYSpeed() < 0))  || ((bullet.getY() + Bot.RADIUS <= me.getY()) && (bullet.getYSpeed() > 0)))
                        && ( (bullet.getYSpeed() != 0) && (bullet.getXSpeed() == 0))
                ){
                    //Dodge right
                    if (me.getX() + Bot.RADIUS > bullet.getX()) {

                        return BattleBotArena.RIGHT;
                    }

                    //Dodge left
                    else {
                        return BattleBotArena.LEFT;
                    }
                }

            }else closeBullet = false;

        } //end of bullet dodging




        //Find closest isolated bot
        BotInfo closestBot = helper.findClosest(me, liveBots);

        while (closestBot.getTeamName().equals("Team 3")) {
            liveBots = removeBotIndex(liveBots, getIndex(closestBot, liveBots));
            // removeBot(closestBot); // Removes bot from list
            closestBot = helper.findClosest(me, liveBots);
        }
        double closeBotDis = helper.manhattanDist(me.getX() + Bot.RADIUS, me.getY() + Bot.RADIUS,
                closestBot.getX(), closestBot.getY());




        //************************************************************************************************
        // Fetching bullets when running all out
        //************************************************************************************************

        // If my bot's number of bullets is 0 and is both not stuck and there is not
        // a bullet close by
        if (me.getBulletsLeft() == 0 && !isStuck && !closeBullet) {

            // Populating array list with all the dead bots in the arena
            for (BotInfo deadBot : deadBots) {
                Collections.addAll(deadBotList, deadBot);
            }

            // Looping through array list holding all the dead bots in the
            // arena
            for (BotInfo deadBot : deadBotList) {
                // Ensuring the selected bot has more than 0 bullets
                if (deadBot.getBulletsLeft() > 0) {
                    // If the dead bot is not aligned vertically with my bot
                    if (!(me.getX() >= deadBot.getX() - 13
                            && me.getX() <= deadBot.getX() + 13)) {
                        // While my bot is not stuck
                        if (!isStuck) {
                            // Since my bot need bullets, assigning to true
                            needBullets = true;

                            // Calculating the X displacement between the dead bot and my bot
                            // and aligning my bot with the dead bot on the Y axis by moving it
                            // left or right depending on the dead bot's position
                            if (helper.calcDisplacement(me.getX() + Bot.RADIUS, deadBot.getX()) > 0) {
                                return BattleBotArena.RIGHT;
                            } else {
                                return BattleBotArena.LEFT;
                            }

                            // If my bot is stuck, prioritizing breaking free
                        } else {
                            needBullets = false;
                        }

                        // If aligned over the Y axis, moving up or down depending on the dead bot's
                        // position to loot bullets
                    } else {
                        if (helper.calcDisplacement(me.getY() + Bot.RADIUS, deadBot.getY()) > 0) {
                            return BattleBotArena.DOWN;

                        } else if (helper.calcDisplacement(me.getY() + Bot.RADIUS, deadBot.getY()) < 0) {
                            return BattleBotArena.UP;
                        }
                    }

                    // If not dead bots with bullets are found, removing bots from list and staying
                    // in the same spot to avoid death
                } else {
                    if (deadBotList.size() > 0) deadBotList.remove(deadBot);
                    return BattleBotArena.STAY;
                }
            }
            // If my bot has more than 0 bullets, no need to fetch bullets
        } else {
            needBullets = false;
        }

        //************************************************************************************************
        // If my bot is stuck at either edge of the screen, loosing it free by moving it to the
        // opposite direction.
        //************************************************************************************************

        // Ensuring that no bullets are nearby before checking if the bot is stuck
        if (!closeBullet && helper.calcDistance(me.getX(), me.getY(), closestBot.getX(), closestBot.getY()) > 30) {

            // If stuck at the left edge of the screen, moving to the right
            if (me.getX() <= BattleBotArena.LEFT_EDGE + 5) {
                return BattleBotArena.RIGHT;
            }

            // If stuck at the right edge of the screen, moving to the left
            if (me.getX() >= BattleBotArena.RIGHT_EDGE - 35) {
                return BattleBotArena.LEFT;

                // If stuck at the top edge of the screen, moving down
            } else if (me.getY() <= BattleBotArena.TOP_EDGE + 5) {
                return BattleBotArena.DOWN;

                // If stuck at the bottom edge of the screen, moving up
            } else if (me.getY() >= BattleBotArena.BOTTOM_EDGE - 35) {
                return BattleBotArena.UP;
            }
        }

        //************************************************************************************************
        // If my bot is stuck to another bot, moving in opposite direction.
        //************************************************************************************************

        // As long as my bot is not out of bullets
        if (!outOfBullets) {
            // Incrementing stuck counter if my bot stays in the same spot
            // less than 15 times
            if (me.getX() == lastX && me.getY() == lastY && stuckCounter < 15) {
                stuckCounter++;

                // If my bot is not stuck, resetting counter and setting
                // boolean to false
            } else {
                stuckCounter = 0;
                isStuck = false;
            }

            // If my bot got stuck 10 times and no bullets are near by
            if (stuckCounter > 10 && !closeBullet) {
                // My bot is stuck
                isStuck = true;

                // If the distance between my bot and the targeted bot is too small,
                // which means that my bot is stuck, trying to get unstuck
                if (helper.calcDistance(me.getX(), me.getY(), closestBot.getX(),
                        closestBot.getY()) > Bot.RADIUS * 2 + 10 ||
                        closestBot.isOverheated()) {

                    // If the targeted bot is aligned horizontally with my bot
                    if (!(me.getX() >= closestBot.getX() - 13
                            && me.getX() <= closestBot.getX() + 13)) {

                        //************************************************************************************
                        // Depending on the targeted bot's position, going the opposite
                        // direction and trying to get my bot unstuck
                        //************************************************************************************

                        // If my bot is above the targeted bot, moving in opposite direction
                        // as long as not at the top corner of the arena
                        if (me.getY() + Bot.RADIUS > closestBot.getY()) {
                            if (!(me.getY() <= BattleBotArena.TOP_EDGE + 5)) {
                                return BattleBotArena.DOWN;

                                // If my bot is at the top corner of the arena, moving down
                            } else {
                                return BattleBotArena.UP;
                            }


                            // If my bot is below the targeted bot, moving in opposite direction
                            // as long as not at the bottom corner of the arena
                        } else {
                            if (!(me.getY() >= BattleBotArena.BOTTOM_EDGE - 35)) {
                                return BattleBotArena.DOWN;

                                // If my bot is at the bottom corner of the arena, moving up
                            } else {
                                return BattleBotArena.UP;
                            }
                        }
                    }

                    // If the distance is too great, which means the my bot is stuck to a
                    // dead bot
                } else {

                    // Moving my bot in the opposite direction
                    if (me.getX() + Bot.RADIUS > closestBot.getX()) {
                        return BattleBotArena.RIGHT;
                    } else {
                        return BattleBotArena.LEFT;
                    }
                }
            }
        }





        //Ensure there are no nearby bullets before target tracking and shooting
        shotRange = Bot.RADIUS * 20;
        if (!closeBullet) {
            //If the closest bot is less than the shooting range units away and they are not lined up, line up

            //Check if in range
            if (closeBotDis <= shotRange){
                //Shoot if lined up

                //Up and down
                if ( Math.abs(me.getX() - closestBot.getX()) <= Bot.RADIUS  ) {

                    //Check if the bot is above my bot
                    if (shotOK && me.getY() + Bot.RADIUS > closestBot.getY() + Bot.RADIUS) {
                        return BattleBotArena.FIREUP; //shoot up

                        //Check if the bot is below my bot
                    } else if (shotOK && me.getY() + Bot.RADIUS < closestBot.getY() + Bot.RADIUS) {
                        return BattleBotArena.FIREDOWN;
                    }

                    //Left and right
                }else if ( Math.abs(me.getY() - closestBot.getY()) <= Bot.RADIUS  ){
                    //Check if the bot is to the left
                    if (shotOK && me.getX() + Bot.RADIUS > closestBot.getX() + Bot.RADIUS) { // farther apart from the bot
                        return BattleBotArena.FIRELEFT; //shoot left
                        //check if the bot is to the right of my bot
                    } else if (me.getX() + Bot.RADIUS < closestBot.getX() + Bot.RADIUS) {
                        return BattleBotArena.FIRERIGHT; //shoot right
                    }

                    //Move to line up
                }else{
                    double xDiff = me.getX() - closestBot.getX();
                    double yDiff = me.getY() - closestBot.getY();

                    if(Math.abs(xDiff) < Math.abs(yDiff)){
                        if (xDiff > 0) return BattleBotArena.LEFT;
                        else return BattleBotArena.RIGHT;
                    }else{
                        if (yDiff > 0) return BattleBotArena.UP;
                        else return BattleBotArena.DOWN;
                    }
                }
            }else {//if the close bot is not in range

                double xDiff = me.getX() - closestBot.getX();
                double yDiff = me.getY() - closestBot.getY();

                if(Math.abs(xDiff) > Math.abs(yDiff)){
                    if (xDiff > 0) return BattleBotArena.LEFT;
                    else return BattleBotArena.RIGHT;
                }else{
                    if (yDiff > 0) return BattleBotArena.UP;
                    else return BattleBotArena.DOWN;
                }
            }


        }//end of attacking



        return 0;
    }//end of attack function

    private int group(BotInfo me, BotInfo[] liveBots, Bullet[] bullets, Boolean shotOK, BotInfo[] deadBots){

        //Bullet dodging
        //Check every bullet
        for (Bullet bullet : bullets){

            //Check if the bullet is too close
            if ( (helper.calcDistance(bullet.getX(), bullet.getY(), me.getX(), me.getY()) ) <= tooClose){
                closeBullet = true;

                //Dodge accordingly

                //Dodging horizontal bullets
                if ( (((bullet.getX() >= me.getX() + Bot.RADIUS) && (bullet.getXSpeed() < 0)) || ((bullet.getX() + Bot.RADIUS <= me.getX()) && (bullet.getXSpeed() > 0)))
                        && ( (bullet.getXSpeed() != 0) && (bullet.getYSpeed() == 0) )
                ){
                    //Dodge up
                    if (me.getY() + Bot.RADIUS > bullet.getY()) {

                        return BattleBotArena.DOWN;
                    }

                    //Dodge down
                    else {

                        return BattleBotArena.UP;
                    }
                }

                //Dodging vertical bullets
                if ( ( ((bullet.getY() >= me.getY() + Bot.RADIUS) && (bullet.getYSpeed() < 0))  || ((bullet.getY() + Bot.RADIUS <= me.getY()) && (bullet.getYSpeed() > 0)))
                        && ( (bullet.getYSpeed() != 0) && (bullet.getXSpeed() == 0))
                ){
                    //Dodge right
                    if (me.getX() + Bot.RADIUS > bullet.getX()) {

                        return BattleBotArena.RIGHT;
                    }

                    //Dodge left
                    else {
                        return BattleBotArena.LEFT;
                    }
                }

            }else closeBullet = false;

        } //end of bullet dodging


        //Find closest isolated bot
        BotInfo closestBot = helper.findClosest(me, liveBots);

        while (closestBot.getTeamName().equals("Team 3")) {
            liveBots = removeBotIndex(liveBots, getIndex(closestBot, liveBots));
            // removeBot(closestBot); // Removes bot from list
            closestBot = helper.findClosest(me, liveBots);
        }
        double closeBotDis = helper.manhattanDist(me.getX() + Bot.RADIUS, me.getY() + Bot.RADIUS,
                closestBot.getX(), closestBot.getY());


        //************************************************************************************************
        // Fetching bullets when running all out
        //************************************************************************************************

        // If my bot's number of bullets is 0 and is both not stuck and there is not
        // a bullet close by
        if (me.getBulletsLeft() == 0 && !isStuck && !closeBullet) {

            // Populating array list with all the dead bots in the arena
            for (BotInfo deadBot : deadBots) {
                Collections.addAll(deadBotList, deadBot);
            }

            // Looping through array list holding all the dead bots in the
            // arena
            for (BotInfo deadBot : deadBotList) {
                // Ensuring the selected bot has more than 0 bullets
                if (deadBot.getBulletsLeft() > 0) {
                    // If the dead bot is not aligned vertically with my bot
                    if (!(me.getX() >= deadBot.getX() - 13
                            && me.getX() <= deadBot.getX() + 13)) {
                        // While my bot is not stuck
                        if (!isStuck) {
                            // Since my bot need bullets, assigning to true
                            needBullets = true;

                            // Calculating the X displacement between the dead bot and my bot
                            // and aligning my bot with the dead bot on the Y axis by moving it
                            // left or right depending on the dead bot's position
                            if (helper.calcDisplacement(me.getX() + Bot.RADIUS, deadBot.getX()) > 0) {
                                return BattleBotArena.RIGHT;
                            } else {
                                return BattleBotArena.LEFT;
                            }

                            // If my bot is stuck, prioritizing breaking free
                        } else {
                            needBullets = false;
                        }

                        // If aligned over the Y axis, moving up or down depending on the dead bot's
                        // position to loot bullets
                    } else {
                        if (helper.calcDisplacement(me.getY() + Bot.RADIUS, deadBot.getY()) > 0) {
                            return BattleBotArena.DOWN;

                        } else if (helper.calcDisplacement(me.getY() + Bot.RADIUS, deadBot.getY()) < 0) {
                            return BattleBotArena.UP;
                        }
                    }

                    // If not dead bots with bullets are found, removing bots from list and staying
                    // in the same spot to avoid death
                } else {
                    if (deadBotList.size() > 0) deadBotList.remove(deadBot);
                    return BattleBotArena.STAY;
                }
            }
            // If my bot has more than 0 bullets, no need to fetch bullets
        } else {
            needBullets = false;
        }

        //************************************************************************************************
        // If my bot is stuck at either edge of the screen, loosing it free by moving it to the
        // opposite direction.
        //************************************************************************************************

        // Ensuring that no bullets are nearby before checking if the bot is stuck
        if (!closeBullet && helper.calcDistance(me.getX(), me.getY(), closestBot.getX(), closestBot.getY()) > 30) {

            // If stuck at the left edge of the screen, moving to the right
            if (me.getX() <= BattleBotArena.LEFT_EDGE + 5) {
                return BattleBotArena.RIGHT;
            }

            // If stuck at the right edge of the screen, moving to the left
            if (me.getX() >= BattleBotArena.RIGHT_EDGE - 35) {
                return BattleBotArena.LEFT;

                // If stuck at the top edge of the screen, moving down
            } else if (me.getY() <= BattleBotArena.TOP_EDGE + 5) {
                return BattleBotArena.DOWN;

                // If stuck at the bottom edge of the screen, moving up
            } else if (me.getY() >= BattleBotArena.BOTTOM_EDGE - 35) {
                return BattleBotArena.UP;
            }
        }

        //************************************************************************************************
        // If my bot is stuck to another bot, moving in opposite direction.
        //************************************************************************************************

        // As long as my bot is not out of bullets
        if (!outOfBullets) {
            // Incrementing stuck counter if my bot stays in the same spot
            // less than 15 times
            if (me.getX() == lastX && me.getY() == lastY && stuckCounter < 15) {
                stuckCounter++;

                // If my bot is not stuck, resetting counter and setting
                // boolean to false
            } else {
                stuckCounter = 0;
                isStuck = false;
            }

            // If my bot got stuck 10 times and no bullets are near by
            if (stuckCounter > 10 && !closeBullet) {
                // My bot is stuck
                isStuck = true;

                // If the distance between my bot and the targeted bot is too small,
                // which means that my bot is stuck, trying to get unstuck
                if (helper.calcDistance(me.getX(), me.getY(), closestBot.getX(),
                        closestBot.getY()) > Bot.RADIUS * 2 + 10 ||
                        closestBot.isOverheated()) {

                    // If the targeted bot is aligned horizontally with my bot
                    if (!(me.getX() >= closestBot.getX() - 13
                            && me.getX() <= closestBot.getX() + 13)) {

                        //************************************************************************************
                        // Depending on the targeted bot's position, going the opposite
                        // direction and trying to get my bot unstuck
                        //************************************************************************************

                        // If my bot is above the targeted bot, moving in opposite direction
                        // as long as not at the top corner of the arena
                        if (me.getY() + Bot.RADIUS > closestBot.getY()) {
                            if (!(me.getY() <= BattleBotArena.TOP_EDGE + 5)) {
                                return BattleBotArena.DOWN;

                                // If my bot is at the top corner of the arena, moving down
                            } else {
                                return BattleBotArena.UP;
                            }


                            // If my bot is below the targeted bot, moving in opposite direction
                            // as long as not at the bottom corner of the arena
                        } else {
                            if (!(me.getY() >= BattleBotArena.BOTTOM_EDGE - 35)) {
                                return BattleBotArena.DOWN;

                                // If my bot is at the bottom corner of the arena, moving up
                            } else {
                                return BattleBotArena.UP;
                            }
                        }
                    }

                    // If the distance is too great, which means the my bot is stuck to a
                    // dead bot
                } else {

                    // Moving my bot in the opposite direction
                    if (me.getX() + Bot.RADIUS > closestBot.getX()) {
                        return BattleBotArena.RIGHT;
                    } else {
                        return BattleBotArena.LEFT;
                    }
                }
            }
        }





        //Ensure there are no nearby bullets before grouping
        shotRange = Bot.RADIUS * 20;
        if (!closeBullet) {
            if (me.getX() <= BattleBotArena.LEFT_EDGE + 5 && me.getY() <= BattleBotArena.TOP_EDGE + 5 ) {
                return 0;
            } else if (me.getX() <= BattleBotArena.LEFT_EDGE + 5) {
                return BattleBotArena.UP;
            } else if (me.getY() <= BattleBotArena.TOP_EDGE + 6) {
                return BattleBotArena.LEFT;
            } else if (me.getX() >= BattleBotArena.RIGHT_EDGE - 35 && me.getY() >= BattleBotArena.BOTTOM_EDGE - 35) {
                return BattleBotArena.LEFT;
            } else if (me.getX() >= BattleBotArena.RIGHT_EDGE - 35) {
                return BattleBotArena.UP;
            } else {
                return BattleBotArena.UP;
            }
        }

        return 0;

        }//end of grouping up


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
        g.drawImage(image, x, y, Bot.RADIUS * 2, Bot.RADIUS * 2, null);
    }

    /**
     * This method will only be called once, just after your Bot is created,
     * to set your name permanently for the entire match.
     *
     * @return The Bot's name
     */
    @Override
    public String getName() {
        return "ELIT3";
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
        return "Team 3";
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

        String[] paths = {"team3.jpg"}; //my custom image
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
        image = images[0];
    }
}
