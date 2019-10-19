package com.vladwild.coinshunter.animals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.TimeUtils;
import com.vladwild.coinshunter.screens.GameScreen;
import com.vladwild.coinshunter.resource.ResourceManager;

public class Hunter extends Animal {
    private static final String HUNTER_TEXTURES[] =                                    //массив ключей в файле properties
            {"hunter_left_01", "hunter_left_02", "hunter_rigth_01", "hunter_rigth_02",
             "hunter_down_01", "hunter_down_02", "hunter_up_01", "hunter_up_02"};
    private static final String HUNTER_TEXTURES_DEAD[] =                               //массив ключей в файле properties
            {"hunter_dead_01", "hunter_dead_02", "hunter_dead_03", "hunter_dead_04",
             "hunter_dead_05", "hunter_dead_06", "hunter_dead_07", "hunter_dead_08",
             "hunter_dead_09", "hunter_dead_10", "hunter_dead_11", "hunter_dead_12",
             "hunter_dead_13", "hunter_dead_14", "hunter_dead_15", "hunter_dead_16",
             "hunter_dead_17", "hunter_dead_18", "hunter_dead_19", "hunter_dead_20"};
    private static int shift_left_on_start;
    private Animation walkAnimationDead;
    private static final int NUMBER_TEXTURES = 8;
    private static final int NUMBER_TEXTURES_SIDE = 2;
    private static final int MULTIPLER_LIVE = 2;
    private static final float SPEED_ANIMATION = 0.1f;
    private static final float SPEED_ANIMATION_DEAD = 0.1f;
    private static Sprite lastSprite;
    private static Sprite liveSprite;
    private static int level;
    private static int points;
    private static int live;
    private static int numberMonster;
    private static int beginMultiplerLive;

    private int valueElemLogicMatrix;
    private int frameNumber;

    public Hunter(GameScreen gameScreen, Direction direction, GridPoint2 position, long speed, int live) {
        super(gameScreen, direction, position, speed);
        Sprite sprites[] = new Sprite[NUMBER_TEXTURES];
        for (int i = 0; i < NUMBER_TEXTURES; i++) {
            sprites[i] = new Sprite(new Texture(new ResourceManager(PROPERTY_ANIMALS).getFileHandle(HUNTER_TEXTURES[i])));
        }
        for (int i = 0; i < NUMBER_TEXTURES / NUMBER_TEXTURES_SIDE; i++) {
            this.walkAnimation[i] = new Animation(SPEED_ANIMATION, sprites[NUMBER_TEXTURES_SIDE * i], sprites[NUMBER_TEXTURES_SIDE * i + 1]);
        }

        Sprite spritesDead[] = new Sprite[gameScreen.NUMBER_TEXTURE_DEAD];
        for (int i = 0; i < gameScreen.NUMBER_TEXTURE_DEAD; i++) {
            spritesDead[i] = new Sprite(new Texture(new ResourceManager(PROPERTY_ANIMALS).getFileHandle(HUNTER_TEXTURES_DEAD[i])));
        }
        this.liveSprite = new Sprite(new Texture(new ResourceManager(PROPERTY_ANIMALS).getFileHandle(HUNTER_TEXTURES[0])));
        this.walkAnimationDead = new Animation(SPEED_ANIMATION_DEAD, spritesDead);
        this.shift_left_on_start = gameScreen.screen.blockSizeX / 2;
        this.positionPixel.x -= this.shift_left_on_start;
        this.beginMultiplerLive = gameScreen.POINT_ADD_LIVE;
        this.live = live;
        this.frameNumber = 0;
        this.level = 1;
        this.points = 0;
        this.numberMonster = 0;

    }

    //определение следующего логического квадрата по направлению
    private int nextLogicSquare(Direction direction){

        int xCurrent = positionLogic.x;
        if ((xCurrent >= this.gameScreen.screen.fieldSizeX - 1) && (direction == Direction.RIGTH)) {
            positionLogic.x = -1;
            if (xCurrent >= this.gameScreen.screen.fieldSizeX) {
                positionPixel.x = this.gameScreen.screen.blockSizeX * positionLogic.x;
            }
        }
        if ((xCurrent <= 1) && (direction == Direction.LEFT)) {
            positionLogic.x = this.gameScreen.screen.fieldSizeX;
            if (xCurrent <= -1) {
                positionPixel.x = this.gameScreen.screen.blockSizeX * positionLogic.x;
            }
        }

        switch (direction){
            case LEFT:
                valueElemLogicMatrix = gameScreen.matrixLevelLogic[getPositionLogic().y][getPositionLogic().x - 1];
                break;
            case RIGTH:
                valueElemLogicMatrix = gameScreen.matrixLevelLogic[getPositionLogic().y][getPositionLogic().x + 1];
                break;
            case DOWN:
                valueElemLogicMatrix = gameScreen.matrixLevelLogic[getPositionLogic().y - 1][getPositionLogic().x];
                break;
            case UP:
                valueElemLogicMatrix = gameScreen.matrixLevelLogic[getPositionLogic().y + 1][getPositionLogic().x];
                break;
        }
        return this.valueElemLogicMatrix;
    }

    //инкремент уровня
    public void incrementLevel(){
        this.level++;
    }

    //получение текущего уровня
    public int getLevel(){
        return this.level;
    }

    public void incrementLive(boolean point){
        if (point) {
            this.beginMultiplerLive *= this.MULTIPLER_LIVE;
        }
        this.live++;
    }

    public int getBeginMultiplerLive(){
        return this.beginMultiplerLive;
    }

    public void decrementLive(){this.live--;}

    public int getLive(){
        return this.live;
    };

    public void addPoint(int points){
        this.points += points;
    }

    public int getPoints(){
        return this.points;
    }

    public Sprite getLiveSprite(){
        return this.liveSprite;
    }

    public void incrementNumberEatMonster(){
        this.numberMonster++;
    }

    public void resetNumberEatMonster(){
        this.numberMonster = 0;
    }

    public int getNumberEatMonster(){
        return  this.numberMonster;
    }

    //движение пекмана
    @Override
    public void move(Direction directionIn, int pixel){
        if (this.inCenterLogicalSquare()) {
            if (directionIn != this.getDirection()) {
                switch (directionIn){
                    case LEFT:
                        if (getPositionLogic().x > 0) {
                            if (gameScreen.matrixLevelLogic[getPositionLogic().y][getPositionLogic().x - 1] == 1) {
                                this.setDirection(directionIn, pixel);
                            } else {
                                //проверка свободы следующего логического квадрата по направлению движения
                                if (nextLogicSquare(this.getDirection()) == 1) {
                                    this.setDirection(this.getDirection(), pixel);
                                } else {
                                    this.setPositionStop();
                                }
                            }
                        }
                        break;
                    case RIGTH:
                        if (getPositionLogic().x < this.gameScreen.screen.fieldSizeX - 1) {
                            if (gameScreen.matrixLevelLogic[getPositionLogic().y][getPositionLogic().x + 1] == 1) {
                                this.setDirection(directionIn, pixel);
                            } else {
                                //проверка свободы следующего логического квадрата по направлению движения
                                if (nextLogicSquare(this.getDirection()) == 1) {
                                    this.setDirection(this.getDirection(), pixel);
                                } else {
                                    this.setPositionStop();
                                }
                            }
                        }
                        break;
                    case DOWN:
                        if ((getPositionLogic().x > 0) && (getPositionLogic().x < this.gameScreen.screen.fieldSizeX - 1)) {
                            if (gameScreen.matrixLevelLogic[getPositionLogic().y - 1][getPositionLogic().x] == 1) {
                                this.setDirection(directionIn, pixel);
                            } else {
                                //проверка свободы следующего логического квадрата по направлению движения
                                if (nextLogicSquare(this.getDirection()) == 1) {
                                    this.setDirection(this.getDirection(), pixel);
                                } else {
                                    this.setPositionStop();
                                }
                            }
                        }
                        break;
                    case UP:
                        if ((getPositionLogic().x > 0) && (getPositionLogic().x < this.gameScreen.screen.fieldSizeX - 1)) {
                            if (gameScreen.matrixLevelLogic[getPositionLogic().y + 1][getPositionLogic().x] == 1) {
                                this.setDirection(directionIn, pixel);
                            } else {
                                //проверка свободы следующего логического квадрата по направлению движения
                                if (nextLogicSquare(this.getDirection()) == 1) {
                                    this.setDirection(this.getDirection(), pixel);
                                } else {
                                    this.setPositionStop();
                                }
                            }
                        }
                        break;
                }
            } else {
                //проверка свободы следующего логического квадрата по направлению движения
                if (nextLogicSquare(this.getDirection()) == 1){
                    this.setDirection(this.getDirection(), pixel);
                } else {
                    this.setPositionStop();
                }
            }
        } else {
            this.setDirection(this.getDirection(), pixel);
        }
        this.reverseDirection(directionIn);
    }

    @Override
    public void nextDiriction(int pixel) {
        this.direction = this.directionNext;
    }

    //возрождение пекмана при смерти
    @Override
    public void newStartCoordinates() {
        this.positionLogic = new GridPoint2(gameScreen.coordinateX, gameScreen.coordinateY + gameScreen.shift);
        this.positionPixel = new GridPoint2(this.gameScreen.screen.blockSizeX * getPositionLogic().x - shift_left_on_start,
                this.gameScreen.screen.blockSizeY * getPositionLogic().y);
        direction = Direction.LEFT;
    }

    //проверка пекмана на противоположное направление
    @Override
    protected boolean reverseDirection(Direction directionIn){
        if (directionIn == Direction.LEFT && this.direction == Direction.RIGTH) {
            this.direction = Direction.LEFT;
        }
        if (directionIn == Direction.RIGTH && this.direction == Direction.LEFT) {
            this.direction = Direction.RIGTH;
        }
        if (directionIn == Direction.UP && this.direction == Direction.DOWN) {
            this.direction = Direction.UP;
        }
        if (directionIn == Direction.DOWN && this.direction == Direction.UP) {
            this.direction = Direction.DOWN;
        }
        return true;
    }

    //получение спрайта пекмана
    @Override
    public Sprite getSprite(){
        this.stateTime += Gdx.graphics.getDeltaTime();
        switch (state) {
            case LIVE:
                switch (this.direction){
                    case LEFT:
                        this.spriteCurrent = (Sprite) this.walkAnimation[0].getKeyFrame(stateTime, true);
                        lastSprite = (Sprite) this.walkAnimation[0].getKeyFrame(stateTime, true);
                        break;
                    case RIGTH:
                        this.spriteCurrent = (Sprite) this.walkAnimation[1].getKeyFrame(stateTime, true);
                        lastSprite = (Sprite) this.walkAnimation[1].getKeyFrame(stateTime, true);
                        break;
                    case DOWN:
                        this.spriteCurrent = (Sprite) this.walkAnimation[2].getKeyFrame(stateTime, true);
                        lastSprite = (Sprite) this.walkAnimation[2].getKeyFrame(stateTime, true);
                        break;
                    case UP:
                        this.spriteCurrent = (Sprite) this.walkAnimation[3].getKeyFrame(stateTime, true);
                        lastSprite = (Sprite) this.walkAnimation[3].getKeyFrame(stateTime, true);
                        break;
                    default:
                        this.spriteCurrent =  lastSprite;
                        break;
                }
                break;
            case DEAD:
                frameNumber = (int) (gameScreen.NUMBER_TEXTURE_DEAD * (TimeUtils.nanoTime() - gameScreen.currentTimeDead) / gameScreen.DELAY_AFTER_DEATH_HUNTER);
                if (frameNumber > gameScreen.NUMBER_TEXTURE_DEAD - 1) {
                    frameNumber = gameScreen.NUMBER_TEXTURE_DEAD - 1;
                }
                this.spriteCurrent = (Sprite) this.walkAnimationDead.getKeyFrames()[frameNumber];
                break;
            case EAT:
                this.spriteCurrent = this.eatSprite;
                break;
        }
        return this.spriteCurrent;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

}
