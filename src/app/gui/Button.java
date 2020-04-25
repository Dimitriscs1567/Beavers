package app.gui;

import app.components.Player;
import app.components.User;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Button {
    public static final int lastTurnButton = 0;
            
    private int x, y, type;
    private int[] bounds;
    private boolean isPressed, canBePressed, isShown, isCreated;
    private User user;
    private Player player;
    private int bwidth, bheight, showingTime;
    private String timeRem;
    
    public Button(int width, int height, User user, int type){
        bwidth = width;
        bheight = height;
        this.user = user;
        this.type = type;
        isPressed = false;
        canBePressed = false;
        isShown = false;
        isCreated = false;
        showingTime = 0;
        timeRem = String.valueOf(showingTime);
        bounds = new int[4];
    }
    
    public boolean insideBounds(int x, int y){
        if(x >= bounds[0] && x <= bounds[1] && y >= bounds[2] && y <= bounds[3])
            return(true);
        return(false);
    } 
    
    public void setIsPressed(boolean b, Player player){
        isPressed = b;
        isShown = false;
        showingTime = 0;
        canBePressed = false;
        this.player = player;
    }
    
    public void setCanBePressed(boolean b){
        canBePressed = b;
    }
    
    public void Create(){
        x = ((bwidth - user.seeCard(3).getBound(1)) / 2) + user.seeCard(3).getBound(1) - 75;
        y = ((bheight - user.seeCard(3).getBound(2)) / 2) + user.seeCard(3).getBound(2) - 50;
        bounds[0] = x;
        bounds[1] = x + 150;
        bounds[2] = y;
        bounds[3] = y + 100;
        isCreated = true;
    }
    
    public boolean isCreated(){
        return(isCreated);
    }
    
    public void setShown(int time){
        showingTime = time;
        timeRem = String.valueOf(showingTime/60+1);
        isShown = true;
    }
    
    public boolean isShown(){
        return(isShown);
    }
    
    public void render(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        if(isShown || isPressed){
            if(type == lastTurnButton){
                if(canBePressed){
                    g2d.setColor(Color.RED);
                    g2d.fillRoundRect(x, y, 150, 100, 30, 30);
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Sans Serif", Font.BOLD, 20));
                    g2d.drawString("LAST", x + 30, y + 40);
                    g2d.drawString("TURN", x + 30, y + 70);
                    g2d.setFont(new Font("Sans Serif", Font.BOLD, 40));
                    g2d.drawString(timeRem, x + 100, y + 60);
                }
                else if(isPressed){
                    FontMetrics metrics;
                    Font font;
                    g2d.setColor(Color.GREEN);
                    g2d.setColor(Color.RED);
                    g2d.fillRoundRect(x, y, 150, 100, 30, 30);
                    g2d.setColor(Color.BLACK);
                    font = new Font("Sans Serif", Font.PLAIN, 22);
                    g2d.setFont(font);
                    metrics = g2d.getFontMetrics(font);
                    g2d.drawString("LAST", x + 75 - (metrics.stringWidth("LAST")/2), y + 30);
                    g2d.drawString("TURN", x + 75 - (metrics.stringWidth("TURN")/2), y + 50);
                    font = new Font("Sans Serif", Font.BOLD, 16);
                    g2d.setFont(font);
                    metrics = g2d.getFontMetrics(font);
                    g2d.drawString("DECLARED BY ", x + 75 - (metrics.stringWidth("DECLARED BY")/2), y + 70);
                    int size = 16;
                    font = new Font("Sans Serif", Font.BOLD, size);
                    metrics = g2d.getFontMetrics(font);
                    if(player!=null){
                        while(metrics.stringWidth(player.getName()) > 130){
                            size--;
                            font = new Font("Sans Serif", Font.BOLD, size);
                            metrics = g2d.getFontMetrics(font);
                        }
                        g2d.setFont(font);
                        g2d.drawString(player.getName() + " ", x + 75 - (metrics.stringWidth(player.getName())/2), y + 90);
                    }
                }
                else{
                    g2d.setColor(Color.CYAN);
                    g2d.fillRoundRect(x, y, 150, 100, 30, 30);
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Sans Serif", Font.BOLD, 20));
                    g2d.drawString("LAST", x + 30, y + 40);
                    g2d.drawString("TURN", x + 30, y + 70);
                    g2d.setFont(new Font("Sans Serif", Font.BOLD, 40));
                    g2d.drawString(timeRem, x + 100, y + 60);
                }
                
            }
        }
    }
    
    public void update(){
        if(showingTime > 0){
            showingTime--;
            timeRem = String.valueOf(showingTime/60+1);
        }
        else
            isShown = false;
    }
}
