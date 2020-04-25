package app.components;

import app.gui.Button;
import app.gui.ScoreBoard;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game implements Runnable {
    private Board board;
    private AI[] players;
    private User user;
    private int width, height, turns, currentTurn, playingTurn, playerToPlay, typeOfPlay, secondChancePhase;
    private int sharingPlayer, sharingCard, endGameCard, AISpeed, endGameSpeed;
    private boolean ready, phase1, phase2, canContinue, openStackChoosed, shuffleMode, endGame, goToNextTurn, changePlayer;
    private boolean isRunning;
    private boolean[] lastTurn;
    private Thread gameThread;
    private Button ltButton;
    private ScoreBoard scoreBoard;
    
    public Game(int width, int height, int numOfPlayers, String[] names, int turns){
        players = new AI[numOfPlayers-1];
        user = new User(0, names[0], numOfPlayers);
        for(int i=1; i < players.length + 1; i++)
            players[i-1] = new AI(i, names[i], numOfPlayers);
        board = new Board(width, height);
        scoreBoard = new ScoreBoard(turns, names, height/2);
        this.width = width;
        this.height = height;
        this.turns = turns;
        currentTurn = 1;
        playingTurn = 0;
        playerToPlay = 0;
        typeOfPlay = 0;
        secondChancePhase = -1;
        sharingPlayer = 0;
        sharingCard = 0;
        endGameCard = 0;
        AISpeed = 2000;
        endGameSpeed = 1000;
        phase1 = true; phase2 = false;
        ready = false; canContinue = true;
        openStackChoosed = false;
        shuffleMode = false;
        changePlayer = true; endGame = false;
        goToNextTurn = false;
        isRunning = true;
        lastTurn = new boolean[numOfPlayers];
        for(int i = 0; i < numOfPlayers; i++)
            lastTurn[i] = false;
        gameThread = new Thread(this);
        ltButton = new Button(width, height, user, Button.lastTurnButton);
    }
    
    public void start(){
        gameThread.start();
    }
    
    public void forceClose(){
        isRunning = false;
    }
    
    public User getUser(){
        return(user);
    }
    
    public Board getBoard(){
        return(board);
    }
    
    public AI[] getPlayers(){
        return(players);
    }
    
    private boolean cardMovement(){
        for (AI player : players) {
            for (int j = 0; j<4; j++) {
                if (player.seeCard(j) != null && player.seeCard(j).isMoving()) {
                    return(true);
                }
            }
        }
        for(int j=0; j<4; j++)
            if(user.seeCard(j) != null && user.seeCard(j).isMoving())
                return(true);
        
        if(board.seeCard(Card.closedCardsStack) != null && board.seeCard(Card.closedCardsStack).isMoving())
            return(true);
        
        if(board.seeCard(Card.openCardsStack) != null && board.seeCard(Card.openCardsStack).isMoving())
            return(true);
        
        return(false);
    }
        
    private void shareCards(){
        if(!cardMovement()){
            if(sharingPlayer < players.length + 1){
                Card tempcard = board.getCard(Card.closedCardsStack);
                tempcard.goTo(sharingPlayer, sharingCard);
                if(sharingPlayer == 0){
                    user.setCard(tempcard);
                    if(playingTurn == 0 && sharingCard == 1)
                        user.setNamePos();
                    sharingCard++;
                }
                else{
                    players[sharingPlayer - 1].setCard(tempcard);
                    if(playingTurn == 0 && sharingCard == 1)
                        players[sharingPlayer - 1].setNamePos();
                    sharingCard++;
                }
            }
            else{
                if(!scoreBoard.isCreated()){
                    int x = players[0].seeCard(0).getBound(1) + ((width - players[0].seeCard(0).getBound(1))/2);
                    scoreBoard.Create(x);
                }
                if(!ltButton.isCreated())
                    ltButton.Create();
                board.openCard();
                sharingCard = 0;
                sharingPlayer = 0;
                ready = true;
            }
            
            if(sharingCard > 3){
                sharingCard = 0;
                sharingPlayer++;
            }
        }
    }
    
    private void begin(){
        if(phase1){
            if(!user.seeCard(0).isOpened() && !canContinue){
                phase2 = true;
                canContinue = true;
                phase1 = false;
            }
            if(phase1){
                user.seeCard(0).setOpened(180);
                user.seeCard(3).setOpened(180);
                for(AI ai:players){
                    ai.learnCard(ai.getID(), 0, ai.seeCard(0).getType());
                    ai.learnCard(ai.getID(), 3, ai.seeCard(3).getType());
                }
                canContinue = false;
            }
        }
        
        else if(phase2 && !cardMovement() && !ltButton.isShown() && !shuffleMode){
            board.setUpdRenderAll(false);
            for(AI ai:players)
                for(int i=0; i<4; i++)
                    ai.seeCard(i).setIsPeeked(false);
            
            if(lastTurn[playerToPlay])
                endGame = true;
            if(!endGame){
                if(playerToPlay == 0)
                    user.setIsPlaying(true);
                playTurn();
                if(changePlayer){
                    if(board.needToShuffle())
                        shuffleMode = true;
                    lastTurn();
                }
                if(changePlayer){
                    if(playerToPlay == 0)
                        user.setIsPlaying(false);
                    else
                        players[playerToPlay - 1].setIsPlaying(false);
                    
                    if(++playerToPlay > players.length){
                        playerToPlay = 0;
                    }
                    
                    if(playerToPlay == 0)
                        user.setIsPlaying(true);
                    else
                        players[playerToPlay - 1].setIsPlaying(true);
                }
                else
                    changePlayer = true;
            }
        }
        
        if(shuffleMode)
            board.shuffleCards(false);
        if(!board.isShuffling() && shuffleMode){
            shuffleMode = false;
            try {
                Thread.sleep(AISpeed);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void playTurn(){
        int choice = 0;
        
        if(canContinue){
            if(playerToPlay == 0 && secondChancePhase == 2){
                secondChancePhase = -1;
                choice = Card.closedCardsStack;
            }
            else if(playerToPlay == 0 && board.seeCard(Card.openCardsStack).getType() <= 9)
                choice = user.chooseStack(false);
            else if(playerToPlay == 0 && board.seeCard(Card.openCardsStack).getType() > 9)
                choice = user.chooseStack(true);
            
            else if(playerToPlay > 0 && secondChancePhase == 2){
                secondChancePhase = -1;
                choice = players[playerToPlay -1].chooseStack(true);
            }
            else if(playerToPlay > 0 && board.seeCard(Card.openCardsStack).getType() <= 9){
                players[playerToPlay -1].learnCard(0, Card.openCardsStack, board.seeCard(Card.openCardsStack).getType());
                choice = players[playerToPlay -1].chooseStack(false);
            }
            else
                choice = players[playerToPlay -1].chooseStack(true);
            
            chooseStack(choice);
        }
        if(!canContinue){
            if(playerToPlay > 0){
                canContinue = players[playerToPlay - 1].decide();
                if(canContinue)
                    decisionProccess();
                if(typeOfPlay == Player.secondChance)
                    secondChance();
            }
            else{
                canContinue = user.decide();
                if(canContinue)
                    decisionProccess();
                if(typeOfPlay == Player.secondChance)
                    secondChance();
            }
        }
        if(!canContinue)
            changePlayer = false;
    }
    
    private void chooseStack(int choice){
        if(choice == Card.closedCardsStack){
            openStackChoosed = false;
            if(playerToPlay == 0){
                if(board.seeCard(Card.openCardsStack).getType() <= 9)
                    for(AI ai:players)
                        ai.thrownCard(0, board.seeCard(Card.openCardsStack).getType());
                
                Card card = board.getCard(Card.closedCardsStack);
                card.goTo(Card.playersOpenCard, playerToPlay);
                card.setOpened(0);
                user.setOpenCard(card);
                if(card.getType() <= 9){
                    user.play(Player.changeCard);
                    typeOfPlay = Player.changeCard;
                }
                else if(card.getType() == Card.Swap){
                    user.play(Player.swapCard);
                    typeOfPlay = Player.swapCard;
                }
                else if(card.getType() == Card.Peek){
                    user.play(Player.peekCard);
                    typeOfPlay = Player.peekCard;
                }
                else{
                    typeOfPlay = Player.secondChance;
                    secondChancePhase = 0;
                }
            }
            else{
                if(board.seeCard(Card.openCardsStack).getType() <= 9)
                    for(AI ai:players)
                        ai.thrownCard(players[playerToPlay - 1].getID(), board.seeCard(Card.openCardsStack).getType());
                
                Card card = board.getCard(Card.closedCardsStack);
                card.goTo(Card.playersOpenCard, playerToPlay);
                if(card.getType() > 9)
                    card.setOpened(0);
                players[playerToPlay - 1].setOpenCard(card);
                if(card.getType() <= 9){
                    players[playerToPlay - 1].play(Player.changeCard);
                    typeOfPlay = Player.changeCard;
                }
                else if(card.getType() == Card.Swap){
                    players[playerToPlay - 1].play(Player.swapCard);
                    typeOfPlay = Player.swapCard;
                }
                else if(card.getType() == Card.Peek){
                    players[playerToPlay - 1].play(Player.peekCard);
                    typeOfPlay = Player.peekCard;
                }
                else{
                    typeOfPlay = Player.secondChance;
                    secondChancePhase = 0;
                }
                
                try {
                    Thread.sleep(AISpeed);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            canContinue = false; 
        }

        else if(choice == Card.openCardsStack){
            openStackChoosed = true;
            if(playerToPlay == 0){
                Card card = board.getCard(Card.openCardsStack);
                card.goTo(Card.playersOpenCard, playerToPlay);
                user.setOpenCard(card);
                user.play(Player.changeCard);
                typeOfPlay = Player.changeCard;
            }
            else{
                Card card = board.getCard(Card.openCardsStack);
                card.goTo(Card.playersOpenCard, playerToPlay);
                players[playerToPlay - 1].setOpenCard(card);
                players[playerToPlay - 1].play(Player.changeCard);
                typeOfPlay = Player.changeCard;
                
                try {
                    Thread.sleep(AISpeed);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            canContinue = false;
        }
        else if(choice == 0)
            changePlayer = false;
    }
    
    private void decisionProccess(){
        int[] dec;
        if(playerToPlay == 0){
            dec = user.getDesicion();
            if(dec != null && dec[0] == -1){
                Card card = user.getOpenCard();
                card.goTo(Card.openCardsStack, 0);
                board.giveCard(card, Card.openCardsStack);
                if(secondChancePhase == 1){
                    changePlayer = false;
                    secondChancePhase = 2;
                }
                
                if(card.getType() <= 9)
                    for(AI ai:players)
                        ai.thrownCard(0, card.getType());
            }
            else if(dec != null && typeOfPlay == Player.changeCard){
                Card card = user.getCard(dec[0]);
                card.setOpened(0);
                card.goTo(Card.openCardsStack, 0);
                board.giveCard(card, Card.openCardsStack);
                
                Card card2 = user.getOpenCard();
                card2.setClosed();
                card2.goTo(0, dec[0]);
                user.setCard(card2);
                
                if(openStackChoosed)
                    for(AI ai:players)
                        ai.learnCard(0, dec[0], card2.getType());
                else
                    for(AI ai:players)
                        ai.learnCard(0, dec[0], -5);
                
                for(AI ai:players)
                    ai.learnHalfCard(0, dec[0], card.getType());
            }
            else if(dec != null && typeOfPlay == Player.swapCard){
                Card card = user.getCard(dec[0]);
                Card card2 = players[dec[1]-1].getCard(dec[2]);
                card2.goTo(0, dec[0]);
                card.goTo(dec[1], dec[2]);
                user.setCard(card2);
                players[dec[1]-1].setCard(card);
                
                card = user.getOpenCard();
                card.goTo(Card.openCardsStack, 0);
                board.giveCard(card, Card.openCardsStack);
                
                for(AI ai:players)
                    ai.swapping(0, dec[0], dec[1], dec[2]);
            }
            else if(dec != null && typeOfPlay == Player.peekCard){
                user.seeCard(dec[0]).setOpened(180);
                Card card = user.getOpenCard();
                card.goTo(Card.openCardsStack, 0);
                board.giveCard(card, Card.openCardsStack);
                
                for(AI ai:players)
                    ai.isKnown(0, dec[0]);
            }
        }
        else{
            dec = players[playerToPlay - 1].getDesicion();
            if(dec != null && dec[0] == -1){
                Card card = players[playerToPlay - 1].getOpenCard();
                card.setOpened(0);
                card.goTo(Card.openCardsStack, 0);
                board.giveCard(card, Card.openCardsStack);
                
                if(card.getType() <= 9)
                    for(AI ai:players)
                        ai.thrownCard(players[playerToPlay - 1].getID(), card.getType());
                
                if(secondChancePhase == 1){
                    changePlayer = false;
                    secondChancePhase = 2;
                }
            }
            else if(dec != null && typeOfPlay == Player.changeCard){
                Card card = players[playerToPlay - 1].getCard(dec[0]);
                card.setOpened(0);
                card.goTo(Card.openCardsStack, 0);
                board.giveCard(card, Card.openCardsStack);
                
                Card card2 = players[playerToPlay - 1].getOpenCard();
                card2.setClosed();
                card2.goTo(players[playerToPlay - 1].getID(), dec[0]);
                players[playerToPlay - 1].setCard(card2);
                
                if(openStackChoosed)
                    for(AI ai:players)
                        ai.learnCard(players[playerToPlay - 1].getID(), dec[0], card2.getType());
                else
                    for(AI ai:players)
                        ai.learnCard(players[playerToPlay - 1].getID(), dec[0], -5);
                
                for(AI ai:players)
                    ai.learnHalfCard(players[playerToPlay - 1].getID(), dec[0], card.getType());
            }
            else if(dec != null && typeOfPlay == Player.swapCard){
                Card card = players[playerToPlay - 1].getCard(dec[0]);
                Card card2;
                if(dec[1] == 0)
                    card2 = user.getCard(dec[2]);
                else
                    card2 = players[dec[1]-1].getCard(dec[2]);
                card2.goTo(players[playerToPlay - 1].getID(), dec[0]);
                card.goTo(dec[1], dec[2]);
                players[playerToPlay - 1].setCard(card2);
                if(dec[1] == 0)
                    user.setCard(card);
                else
                    players[dec[1]-1].setCard(card);
                
                card = players[playerToPlay - 1].getOpenCard();
                card.goTo(Card.openCardsStack, 0);
                board.giveCard(card, Card.openCardsStack);
                
                for(AI ai:players)
                    ai.swapping(players[playerToPlay - 1].getID(), dec[0], dec[1], dec[2]);
            }
            else if(dec != null && typeOfPlay == Player.peekCard){
                players[playerToPlay - 1].seeCard(dec[0]).setIsPeeked(true);
                players[playerToPlay - 1].learnCard(players[playerToPlay - 1].getID(), dec[0], 0);
                Card card = players[playerToPlay - 1].getOpenCard();
                card.goTo(Card.openCardsStack, 0);
                board.giveCard(card, Card.openCardsStack);
                
                for(AI ai:players)
                    ai.isKnown(players[playerToPlay - 1].getID(), dec[0]);
            }
            
            try {
                Thread.sleep(AISpeed);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(secondChancePhase == 1)
            secondChancePhase = -1;
    }
    
    private void secondChance(){
        int choice = 0;
        if(secondChancePhase == 0){
            if(playerToPlay == 0){
                choice = user.chooseStack(true);
                if(choice != 0){
                    secondChancePhase = 1;
                    Card card = user.getOpenCard();
                    card.goTo(Card.openCardsStack, 0);
                    board.giveCard(card, Card.openCardsStack);
                    chooseStack(choice);
                }
            }
            else{
                choice = players[playerToPlay - 1].chooseStack(true);
                if(choice != 0){
                    secondChancePhase = 1;
                    Card card = players[playerToPlay - 1].getOpenCard();
                    card.goTo(Card.openCardsStack, 0);
                    board.giveCard(card, Card.openCardsStack);
                    chooseStack(choice);
                }
            }
        }
    }
    
    private void lastTurn(){
        boolean con = true;
        for(int i=0; i<lastTurn.length; i++)
            if(lastTurn[i])
                con = false;
        
        if(con){
            if(playerToPlay == 0)
                ltButton.setShown(180);
            else{
                lastTurn[playerToPlay] = players[playerToPlay - 1].sayLastTurn();
                if(lastTurn[playerToPlay]){
                    players[playerToPlay - 1].saidLastTurn(true);
                    for(AI ai:players)
                        ai.setLastTurn(players[playerToPlay - 1].getID());
                    ltButton.setIsPressed(true, players[playerToPlay - 1]);
                }
            }
        }
    }
    
    private void endGame(){
        if(board.needToShuffle())
            shuffleMode = true;
        if(shuffleMode)
            board.shuffleCards(false);
        if(!board.isShuffling())
            shuffleMode = false;
        
        if(!cardMovement() && !board.isShuffling()){
            if(playerToPlay == 0){
                user.seeCard(endGameCard).setOpened(0);
                if(user.seeCard(endGameCard).getType() > 9){
                    Card card = user.getCard(endGameCard);
                    board.giveCard(card, Card.openCardsStack);
                    card.goTo(Card.openCardsStack, 0);
                    
                    card = board.getCard(Card.closedCardsStack);
                    user.setCard(card);
                    card.goTo(0, endGameCard);
                    card.setOpened(0);
                }
                else{
                    try {
                        Thread.sleep(endGameSpeed);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    if(++endGameCard == 4){
                        updatePlayerPoints();
                        endGameCard = 0;
                        if(++playerToPlay > players.length)
                            playerToPlay = 0;
                        if(lastTurn[playerToPlay]){
                            int[] scores = new int[players.length+1];
                            for(int i=0; i<scores.length; i++){
                                if(i == 0)
                                    scores[i] = user.getPoints();
                                else
                                    scores[i] = players[i-1].getPoints();
                            }
                            scoreBoard.setNewScores(scores);
                            if(++currentTurn > turns){
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                System.exit(1);
                            }
                            else
                                goToNextTurn = true;
                        }
                    }
                }
            }
            else{
                players[playerToPlay - 1].seeCard(endGameCard).setOpened(0);
                if(players[playerToPlay - 1].seeCard(endGameCard).getType() > 9){
                    Card card = players[playerToPlay - 1].getCard(endGameCard);
                    board.giveCard(card, Card.openCardsStack);
                    card.goTo(Card.openCardsStack, 0);
                    
                    card = board.getCard(Card.closedCardsStack);
                    players[playerToPlay - 1].setCard(card);
                    card.goTo(players[playerToPlay - 1].getID(), endGameCard);
                    card.setOpened(0);
                }
                else{
                    try {
                        Thread.sleep(endGameSpeed);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    if(++endGameCard == 4){
                        updatePlayerPoints();
                        endGameCard = 0;
                        if(++playerToPlay > players.length)
                            playerToPlay = 0;
                        if(lastTurn[playerToPlay]){
                            int[] scores = new int[players.length+1];
                            for(int i=0; i<scores.length; i++){
                                if(i == 0)
                                    scores[i] = user.getPoints();
                                else
                                    scores[i] = players[i-1].getPoints();
                            }
                            scoreBoard.setNewScores(scores);
                            if(++currentTurn > turns){
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                System.exit(1);
                            }
                            else
                                goToNextTurn = true;
                        }
                    }
                }
            }
        }
    }
    
    private void prepareNextTurn(){
        if(!cardMovement() && user.seeCard(0) != null){
            for(int i=0; i<4; i++){
                Card card = user.getCard(i);
                board.giveCard(card, Card.openCardsStack);
                card.goTo(Card.openCardsStack, 0);
            }
            for(AI ai:players)
                for(int i=0; i<4; i++){
                    Card card = ai.getCard(i);
                    board.giveCard(card, Card.openCardsStack);
                    card.goTo(Card.openCardsStack, 0);
                }
            while(board.seeCard(Card.closedCardsStack) != null){
                Card card = board.getCard(Card.closedCardsStack);
                board.giveCard(card, Card.openCardsStack);
                card.setOpened(0);
                card.goTo(Card.openCardsStack, 0);
            }
            board.setUpdRenderAll(true);
            
            try {
                Thread.sleep(endGameSpeed);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        else if(!cardMovement() && !shuffleMode)
            shuffleMode = true;
        
        else if(shuffleMode){
            board.shuffleCards(true);
            if(!board.isShuffling()){
                shuffleMode = false;
                endGame = false;
                goToNextTurn = false;
                phase1 = true; 
                phase2 = false;
                ready = false; 
                canContinue = true;
                ltButton.setIsPressed(false, null);
                playerToPlay = 0;
                playingTurn++;
                for(int i = 0; i < players.length + 1; i++)
                    lastTurn[i] = false;
                for(AI ai:players){
                    ai.forgetTurn();
                    ai.setIsPlaying(false);
                    ai.saidLastTurn(false);
                }
                user.setIsPlaying(false);
                user.saidLastTurn(false);
                    
                try {
                    Thread.sleep(endGameSpeed);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private void updatePlayerPoints(){
        int score = 0;
        if(playerToPlay == 0){
            for(int i=0; i<4; i++)
                score += user.seeCard(i).getType();
            user.setPoints(score);
        }
        else{
            for(int i=0; i<4; i++)
                score += players[playerToPlay - 1].seeCard(i).getType();
            players[playerToPlay - 1].setPoints(score);
        }
    }
    
    private void setShineCards(boolean[] shine){
        for(int i=0; i<players.length*4; i++)
            if(players[i/4].seeCard(i%4) != null)
                players[i/4].seeCard(i%4).canBeChoosed(shine[i]);
    }
    
    public void render(Graphics g, boolean sa){
        ltButton.render(g);
        board.render(g,sa);
        user.render(g,sa);
        for (AI player : players) {
            player.render(g,sa);
        }
        scoreBoard.render(g);
    }
    
    public void update(){
        ltButton.update();
        scoreBoard.update();
        board.update();
        user.update();
        for (AI player : players) {
            player.update();
        }
    }

    @Override
    public void run() {
        while(isRunning){
            if(!ready)
                shareCards();
            else if(goToNextTurn)
                prepareNextTurn();
            else if(!endGame)
                begin();
            else if(endGame)
                endGame();
            
            for (AI player : players) {
                player.evaluatingSituation();
            }
            
            board.setShineCard(user.getShineStackCards());
            setShineCards(user.getShineCards());
        }
    }
    
    public void mouseClicked(MouseEvent me, int player, int pos) {
        if(ltButton.isShown() && ltButton.insideBounds(me.getX(), me.getY())){
            ltButton.setCanBePressed(false);
            ltButton.setIsPressed(true, user);
            lastTurn[0] = true;
            user.saidLastTurn(true);
            for(AI ai:players)
                ai.setLastTurn(0);
        }
        
        user.mouseClicked(me, player, pos);
    }
    
    public void mousePressed(MouseEvent me) {
        if(scoreBoard.insideSmallBounds(me.getX(), me.getY()))
            scoreBoard.setIsMoving(true, me.getY());
    }

    public void mouseReleased(MouseEvent me) {
        scoreBoard.setIsMoving(false, -1);
    }
    
    public void mouseDragged(MouseEvent me) {
        scoreBoard.move(me.getY());
    }
    
    public void mouseMoved(MouseEvent me, int player, int pos) {
        if(ltButton.isShown() && ltButton.insideBounds(me.getX(), me.getY()))
            ltButton.setCanBePressed(true);
        else
            ltButton.setCanBePressed(false);
        
        if(scoreBoard.insideBounds(me.getX(), me.getY()))
            scoreBoard.Descend();
        else
            scoreBoard.Ascend();
        user.mouseMoved(me, player, pos);
        
    }
    
    public void mouseWheelMoved(int numOfScrolls){
        scoreBoard.scrollEffect(numOfScrolls);
    }
    
    public void mouseExited(MouseEvent me) {
        user.mouseExited(me);
    }
}
