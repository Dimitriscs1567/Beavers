package app.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class ScoreBoard {
    private int turns, passedTurn, firstShowingTurn, firstShowingTurnLimit, turnShowLimit, x, bigx, extend, limit, scoreHeight, movingY;
    private int[] scorePos, smallBounds, bigBounds;
    private int[][] scores;
    private String[] names;
    private boolean isCreated, isDescending, isAscending, isDescended, runOnce, canGrow, isMoving;
    
    public ScoreBoard(int turns, String[] names, int limit){
        this.names = names;
        this.limit = limit;
        this.turns = turns;
        passedTurn = 0;
        turnShowLimit = turns;
        firstShowingTurn = 0;
        firstShowingTurnLimit = 0;
        movingY = -1;
        scorePos = new int[names.length + 2];
        scores = new int[turns][names.length];
        smallBounds = new int[4];
        bigBounds = new int[4];
        isCreated = false;
        isDescending = false;
        isAscending = false;
        isDescended = false; 
        runOnce = false;
        canGrow = false;
        isMoving = false;
    }
    
    private void createLimit(Graphics2D g2d){
        Font scoreFont = new Font("Sans Serif", Font.BOLD, 16);
        FontMetrics metrics = g2d.getFontMetrics(scoreFont);
        scoreHeight = metrics.getHeight() + 8;
        int sum = scoreHeight*2;
        int i = 0;
        while(sum <= limit && i<turns){
            sum += scoreHeight;
            i++;
        }
        
        if(sum > limit){
            turnShowLimit = i;
            firstShowingTurnLimit = turns - turnShowLimit;
            canGrow = true;
        }
        else{
            turnShowLimit = turns;
            limit = sum;
            canGrow = false;
        }
        
        int temp = 500 / (names.length + 1);
        scorePos[0] = bigx;
        scorePos[scorePos.length - 1] = bigx + 500;
        for(i=1; i<scorePos.length - 1; i++)
            scorePos[i] = scorePos[i-1] + temp;
        firstShowingTurn = 0;
        runOnce = true;
    }
    
    private void updateBounds(){
        if(isCreated){
            smallBounds[0] = x;
            bigBounds[0] = bigx;
            smallBounds[1] = x + 200;
            bigBounds[1] = bigx + 500;
            smallBounds[2] = extend;
            bigBounds[2] = -1;
            smallBounds[3] = extend + 50;
            bigBounds[3] = extend;
        }
    }
    
    public boolean insideBounds(int x, int y){
        if(x >= smallBounds[0] && x <= smallBounds[1] && y >= smallBounds[2] && y <= smallBounds[3])
            return(true);
        if(x >= bigBounds[0] && x <= bigBounds[1] && y >= bigBounds[2] && y <= bigBounds[3])
            return(true);
        return(false);
    }
    
    public boolean insideSmallBounds(int x, int y){
        if(x >= smallBounds[0] && x <= smallBounds[1] && y >= smallBounds[2] && y <= smallBounds[3])
            return(true);
        return(false);
    }
    
    public void Create(int x){
        this.x = x - 100;
        this.bigx = x - 250;
        extend = -1;
        isCreated = true;
        updateBounds();
    }
    
    public void Ascend(){
        isDescending = false;
        isAscending = true;
        isDescended = false;
    }
    
    public void Descend(){
        isAscending = false;
        isDescending = true;
    }
    
    public boolean isCreated(){
        return(isCreated);
    }
    
    public void scrollEffect(int numOfScrolls){
        if(isDescended){
            if(firstShowingTurn + numOfScrolls > firstShowingTurnLimit)
                firstShowingTurn = firstShowingTurnLimit;
            else if(firstShowingTurn + numOfScrolls < 0)
                firstShowingTurn = 0;
            else
                firstShowingTurn += numOfScrolls;
        }
    }
    
    public void setIsMoving(boolean b, int y){
        isMoving = b;
        movingY = y;
    }
    
    public void move(int y){
        int sum = y - movingY;
        if(isDescended && isMoving){
            if(sum >= 0 && canGrow){
                limit += sum;
                extend = limit;
                runOnce = false;
            }
            else if(limit + sum < 0 ){
                limit = 1;
                extend = limit;
                runOnce = false;
            }
            else{
                limit += sum;
                extend = limit;
                runOnce = false;
            }
            updateBounds();
        }
        movingY = y;
    }
    
    public void setNewScores(int[]scores){
        this.scores[passedTurn] = scores;
        passedTurn++;
    }
    
    private void manageFirstShowingTurn(){
        if(passedTurn >= turnShowLimit)
            firstShowingTurn = (passedTurn - turnShowLimit)+1;
        else
            firstShowingTurn = 0;
    }
    
    public void render(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        if(isCreated){
            g2d.setColor(Color.ORANGE);
            g2d.fillRect(x, extend, 200, 50);
            g2d.setColor(Color.GREEN);
            g2d.fillRect(bigx, 0, 500, extend);
            g2d.setColor(Color.BLACK);
            Font font = new Font("Sans Serif", Font.BOLD, 25);
            FontMetrics metrics = g2d.getFontMetrics(font);
            g2d.setFont(font);
            g2d.drawString("Score Board", x + 100 - (metrics.stringWidth("Score Board")/2), extend + 25 + (metrics.getHeight()/2));
            
            if(!runOnce)
                createLimit(g2d);
            
            int size = 16;
            font = new Font("Sans Serif", Font.BOLD, size);
            metrics = g2d.getFontMetrics(font);
            g2d.setColor(Color.BLACK);
            g2d.setFont(font);
            int tx = scorePos[0] + ((scorePos[1] - scorePos[0])/2) - (metrics.stringWidth("TURN")/2);
            if(scoreHeight - 4 < extend)
                g2d.drawString("TURN", tx, scoreHeight - 4);
            for(int i=0; i<names.length; i++){
                font = new Font("Sans Serif", Font.BOLD, size);
                while(metrics.stringWidth(names[i]) > scorePos[i+2] - scorePos[i+1]){
                    size--;
                    font = new Font("Sans Serif", Font.BOLD, size);
                    metrics = g2d.getFontMetrics(font);
                }
                g2d.setFont(font);
                tx = scorePos[i+1] + ((scorePos[i+2] - scorePos[i+1])/2) - (metrics.stringWidth(names[i])/2);
                if(scoreHeight - 4 < extend)
                    g2d.drawString(names[i], tx, scoreHeight - 4);
                size = 16;
            }
            
            for(int i=0; i<turnShowLimit; i++){
                font = new Font("Sans Serif", Font.BOLD, size);
                metrics = g2d.getFontMetrics(font);
                g2d.setFont(font);
                tx = scorePos[0] + ((scorePos[1] - scorePos[0])/2) - (metrics.stringWidth(String.valueOf(firstShowingTurn+i+1)+".")/2);
                if((i+2)*(scoreHeight) - 4 < extend)
                    g2d.drawString(String.valueOf(firstShowingTurn+i+1)+".", tx, (i+2)*(scoreHeight) - 4);
                if(firstShowingTurn+i+1<=passedTurn)
                    for(int j=0; j<names.length; j++){
                        font = new Font("Sans Serif", Font.BOLD, size);
                        g2d.setFont(font);
                        tx = scorePos[j+1] + ((scorePos[j+2] - scorePos[j+1])/2) - (metrics.stringWidth(String.valueOf(scores[firstShowingTurn+i][j]))/2);
                        if((i+2)*(scoreHeight) - 4 < extend)
                            g2d.drawString(String.valueOf(scores[firstShowingTurn+i][j]), tx, (i+2)*(scoreHeight) - 4);
                        font = new Font("Sans Serif", Font.PLAIN, size);
                        g2d.setFont(font);
                        if(firstShowingTurn+i+1<passedTurn)
                            if((i+2)*(scoreHeight) - 4 < extend)
                                g2d.drawLine(tx - 2, (i+2)*(scoreHeight) - 2, tx+metrics.stringWidth(String.valueOf(scores[firstShowingTurn+i][j]))+2, (i+2)*(scoreHeight) - 4 - (scoreHeight - 8) - 2);
                    }
            }
        }
    }
    
    public void update(){
        if(isCreated){
            if(isDescending && extend < limit)
                extend += 10;
            if(isDescending && extend >= limit){
                isDescending = false;
                isDescended = true;
                extend = limit;
            }

            if(isAscending && extend > -1)
                extend -= 10;
            if(isAscending && extend <= -1){
                isAscending = false;
                manageFirstShowingTurn();
                extend = -1;
            }
            updateBounds();
        }
    }
}
