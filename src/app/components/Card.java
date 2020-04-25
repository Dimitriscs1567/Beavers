package app.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import static java.lang.System.exit;
import javax.imageio.ImageIO;

public class Card {
    public static final int Swap = 10;
    public static final int SecondChance = 11;
    public static final int Peek = 12;
    public static final int closedCardsStack = -1;
    public static final int openCardsStack = -2;
    public static final int playersOpenCard = -3;
    
    private int type;
    private boolean tempOpened, opened, xIncr, yIncr, dIncr, openAnimation, closeAnimation, stage2;
    private boolean setChosen, canBeChoosed, isPeeked;
    private int openedTimer;
    private int x,y,tempx,tempy, animX;
    private int belongs, step, stepD, stepA;
    private int width, height, transDiff;
    private double degrees, tempDegrees;
    private int[] bounds;
    private BufferedImage image, backImage;
    
    public Card(int type, int width, int height){
        this.type = type;
        belongs = closedCardsStack;
        x = width/2 - 120; animX = x; tempx = x;
        y = height/2 - 70; tempy = y;
        degrees = 0; tempDegrees = degrees;
        this.width = width;
        this.height = height;
        bounds = new int[4];
        opened = false;
        step = 15;
        stepD = 7;
        stepA = 5;
        openedTimer = -1;
        xIncr = false; yIncr = false; dIncr = false;
        openAnimation = false; closeAnimation = false;
        stage2 = false; tempOpened = false;
        setChosen = false; canBeChoosed = false; isPeeked = false;
        if(width > height)
            transDiff = (width - height)/2;
        else
            transDiff = -(height - width)/2;
        
        imageToDraw();
        updateBounds();
    }
    
    private void imageToDraw(){
        String img = "app/resources/images/" + String.valueOf(type) + ".jpg";
        
        try{
            image = ImageIO.read(getClass().getClassLoader().getResourceAsStream(img));
        }catch(IOException ex){
            String t = ex.getMessage();
            System.out.println(t);
            exit(1);
        }
            
        try{
            backImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("app/resources/images/BACK.jpg"));
        }catch(IOException ex){
            String t = ex.getMessage();
            System.out.println(t);
            exit(1);
        }
    }
    
    public int getBound(int i){
        return(bounds[i]);
    }
    
    private void updateBounds(){
        int[] tb = new int[2];
        tb = rotate(x, y, x+50, y+70, degrees);
        if(belongs <= 0){
            bounds[0] = tb[0];
            bounds[1] = tb[0] + 100;
            bounds[2] = tb[1];
            bounds[3] = tb[1] + 140;
        }
        else if(belongs == 1){
            bounds[0] = tb[0] - 100;
            bounds[1] = tb[0];
            bounds[2] = tb[1] - 140;
            bounds[3] = tb[1];
        }
        else if(belongs == 2){
            bounds[0] = tb[0] - 140;
            bounds[1] = tb[0];
            bounds[2] = tb[1];
            bounds[3] = tb[1] + 100;
        }
        else if(belongs == 3){
            bounds[0] = tb[0];
            bounds[1] = tb[0] + 140;
            bounds[2] = tb[1] - 100;
            bounds[3] = tb[1];
        }
    }
    
    public int getType(){
        return(type);
    }
    
    public boolean isOpened(){
        return(opened);
    }
    
    public boolean isChosen(){
        return(setChosen);
    }
    
    public boolean insideBounds(int x, int y){
        if(x >= bounds[0] && x <= bounds[1] && y >= bounds[2] && y <= bounds[3])
            return(true);
        return(false);
    }
    
    public void goTo(int destination, int pos){
        if(destination >= 0){
            switch(pos){
                case 0: tempx = width/2 - 260; break;
                case 1: tempx = width/2 - 120; break;
                case 2: tempx = width/2 + 20; break;
                case 3: tempx = width/2 + 160; break;
                default: break;
            }
            tempy = height - 140; 
            tempDegrees = calculateDegrees(destination);
        }
        
        else if(destination == openCardsStack){
            tempx = width/2 + 20; 
            tempy = height/2 - 70; 
            tempDegrees = 0.0;
        }
        
        else if(destination == playersOpenCard){
            tempx = width/2 - 50;
            tempy = ((height + (height/2) - 70) / 2)-70;
            tempDegrees = calculateDegrees(pos);
        }
        
        else if(destination == closedCardsStack){
            tempx = width/2 - 120; 
            tempy = height/2 - 70; 
            tempDegrees = 0.0;
        }
        
        if(destination != playersOpenCard)
            belongs = destination;
        else
            belongs = pos;
        
        if(belongs > 0)
            calculateRealPos();
        
        xIncr = tempx > x;
        yIncr = tempy > y;
        dIncr = tempDegrees > degrees;
        setChosen = false;
        canBeChoosed = false;
    }
    
    private void calculateRealPos(){
        int[] realpos, correctpos;
        
        correctpos = rotate(tempx, tempy, tempx+50, tempy+70, tempDegrees);
        if(belongs == 2){
            tempx -= tempx - (correctpos[0]-140);
            tempy += tempy - correctpos[1];
        }
        else if(belongs == 3){
            tempx += tempx - correctpos[0];
            tempy -= tempy - (correctpos[1] - 100);
        }
        
        realpos = rotate(tempx, tempy, width/2, height/2, tempDegrees);
        if(belongs == 1){
            tempx = realpos[0] - 100;
            tempy = realpos[1] - 140;
        }
        else if(belongs == 2){
            tempx = realpos[0] - (transDiff+140);
            tempy = realpos[1];
        }
        else if(belongs == 3){
            tempx = realpos[0] + transDiff;
            tempy = realpos[1] - 100;
        }
    }
    
    private double calculateDegrees(int des){
       if(des == 0)
           return(0.0);
       else if(des == 1)
           return(180.0);
       else if(des == 2)
           return(90.0);
       else
           return(-90.0);
    }
    
    private int[] rotate(int x, int y, int cx, int cy, double degrees){
        int[] newpos = new int[2];
        double tx = (x - cx)*Math.cos(Math.toRadians(degrees)) - (y - cy)*Math.sin(Math.toRadians(degrees)) + cx;
        double ty = (y - cy)*Math.cos(Math.toRadians(degrees)) + (x - cx)*Math.sin(Math.toRadians(degrees)) + cy;
        newpos[0] = (int)tx;
        newpos[1] = (int)ty;
        return(newpos);
    }
    
    public void render(Graphics g, boolean sa){
        Graphics2D g2d = (Graphics2D) g;
        g2d.rotate(Math.toRadians(degrees), x+50, y+70);
        drawCard(g2d,sa);
        g2d.rotate(Math.toRadians(-degrees), x+50, y+70);
    }
    
    private void drawCard(Graphics2D g, boolean sa){
        g.setStroke(new BasicStroke(5));
        if(!sa){
            if(!tempOpened)
                g.drawImage(backImage, animX, y, 100 - 2*(animX - x), 140, null);
            else
                g.drawImage(image, animX, y, 100 - 2*(animX - x), 140, null);
        }
        else
            g.drawImage(image, animX, y, 100 - 2*(animX - x), 140, null);
        
        g.setColor(Color.WHITE);    
        if(setChosen)
            g.setColor(Color.GREEN);
        if(canBeChoosed)
            g.setColor(Color.RED);
        if(isPeeked)
            g.setColor(Color.BLUE);
        
        g.drawRoundRect(animX, y, 100 - 2*(animX - x) , 140, 30, 30);
    }
    
    public void update(){
        if(openedTimer > 0)
            openedTimer--;
        else if(openedTimer == 0){
            openedTimer = -1;
            closeAnimation = true;
            opened = false;
        }
        
        int xchange = x;
        if(tempx != x){
            if(!xIncr){
                if(x > tempx)
                    x-=step;
                else if(x <= tempx)
                    x=tempx;
            }
            else if(xIncr){
                if(x < tempx)
                    x+=step;
                else if(x >= tempx)
                    x=tempx;
            }
        }
        if(xchange < x)
            animX += x - xchange;
        else
            animX -= xchange - x;
        
        if(tempy != y){
            if(!yIncr){
                if(y > tempy)
                    y-=step;
                else if(y <= tempy)
                    y=tempy;
            }
            else if(yIncr){
                if(y < tempy)
                    y+=step;
                else if(y >= tempy)
                    y=tempy;
            }
        }
        
        if(tempDegrees != degrees){
            if(!dIncr){
                if(degrees > tempDegrees)
                    degrees-=stepD;
                else if(degrees <= tempDegrees)
                    degrees=tempDegrees;
            }
            else if(dIncr){
                if(degrees < tempDegrees)
                    degrees+=stepD;
                else if(degrees >= tempDegrees)
                    degrees=tempDegrees;
            }
        }
        openCloseAnimation();
        updateBounds();
    }
    
    public void setOpened(int time){
        if(!opened){
            openAnimation = true;
            opened = true;
            if(time == 0)
                openedTimer = -1;
            else
               openedTimer = time;
        }
    }
    
    public void setClosed(){
        if(opened){
            closeAnimation = true;
            opened = false;
        }
    }
    
    public boolean isMoving(){
        if(tempx!=x || tempy!=y || tempDegrees!=degrees || animX!=x || openedTimer > -1)
            return(true);
        return(false);
    }
    
    private void openCloseAnimation(){
        if(openAnimation || closeAnimation){
            if(!stage2){
                if(animX < x + 50)
                    animX += stepA;
                else{
                    stage2 = true;
                    if(openAnimation)
                        tempOpened = true;
                    else if(closeAnimation)
                        tempOpened = false;
                }
            }
            else{
                if(animX > x )
                    animX -= stepA;
                else{
                    animX = x;
                    stage2 = false;
                    openAnimation = false;
                    closeAnimation = false;
                }
            }
        }
    }
    
    public void setChosen(boolean c){
        setChosen = c;
    }
    
    public void canBeChoosed(boolean c){
        canBeChoosed = c;
    }
    
    public void setIsPeeked(boolean c){
        isPeeked = c;
    }
        
}
