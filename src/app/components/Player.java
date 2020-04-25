package app.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class Player {
    
    public static final int changeCard = 1;
    public static final int swapCard = 2;
    public static final int peekCard = 3;
    public static final int secondChance = 4;
    
    protected Card[] cards;
    protected Card openCard;
    protected int points, id, typeOfDecision, chosenStack, xname, yname, degreesname;
    protected boolean hasDecided, isPlaying, saidLastTurn; 
    protected int[] decisionChangePeek, decisionSwap;
    protected String name;
    
    public Player(int id, String name){
        cards = new Card[4];
        points = 0;
        this.id = id;
        this.name = name;
        xname = -1;
        openCard = null;
        hasDecided = false;
        isPlaying = false;
        saidLastTurn = false;
        typeOfDecision = 0;
        chosenStack = 0;
        decisionChangePeek = new int[1]; 
        decisionSwap = new int [3]; 
        
        
    }
    
    public int getPoints(){
        return(points);
    }
    
    public int getID(){
        return(id);
    }
    
    public String getName(){
        return(name);
    }
    
    public void setPoints(int score){
        points += score;
    }
    
    public void setCard(Card card){
        int i=0;
        while(cards[i] != null)
            i++;
        cards[i] = card;
    }
    
    public void setOpenCard(Card card){
        openCard = card;
    }
    
    public Card seeCard(int i){
        if(cards[i]!=null)
            return(cards[i]);
        return(null);
    }
    
    public Card seeOpenCard(){
        if(openCard != null)
                return(openCard);
        return(null);
    }
    
    public Card getCard(int i){
        if(cards[i]!=null){
            Card card = cards[i];
            cards[i] = null;
            return(card);
        }
        return(null);
    }
    
    public Card getOpenCard(){
        if(openCard != null){
            Card card = openCard;
            openCard = null;
            return(card);
        }
        return(null);
    }
    
    public boolean decide(){
        if(hasDecided){
            hasDecided = false;
            return(true);
        }
        return(hasDecided);
    }
    
    public void setNamePos(){
        if(cards[0] != null){
            switch(id){
                case 0: xname = cards[0].getBound(0); 
                    yname = cards[0].getBound(2) - 50; 
                    degreesname = 0;
                    break;
                case 1: xname = cards[0].getBound(0);
                    yname = cards[0].getBound(3) + 50; 
                    degreesname = 0;
                    break;
                case 2: xname = cards[0].getBound(1) + 50;
                    yname = cards[0].getBound(2); 
                    degreesname = 90;
                    break;
                case 3: xname = cards[0].getBound(0) - 50;
                    yname = cards[0].getBound(3); 
                    degreesname = -90;
                    break;
                default: break;
            }
        }
    }
    
    public void setIsPlaying(boolean b){
        isPlaying = b;
    }
    
    public void saidLastTurn(boolean b){
        saidLastTurn = b;
    }
    
    public void render(Graphics g,boolean sa){
        Graphics2D g2d = (Graphics2D) g;
        Font font = new Font("Sans Serif", Font.PLAIN, 20);
        FontMetrics metrics = g2d.getFontMetrics(font);
        int width = metrics.stringWidth(name), height = metrics.getHeight();
        if(xname > -1){
            g2d.rotate(Math.toRadians(degreesname), xname, yname);
            if(isPlaying && !saidLastTurn){
                g2d.setColor(Color.lightGray);
                g2d.fillOval(xname - 15, yname - height - 2, width + 25, height + 15);
            }
            if(saidLastTurn){
                g2d.setColor(Color.RED);
                g2d.fillOval(xname - 15, yname - height - 2, width + 25, height + 15);
            }
            g2d.setColor(Color.BLACK);
            g2d.setFont(font);
            g2d.drawString(name, xname, yname);
            g2d.rotate(Math.toRadians(-degreesname), xname, yname);
        }
        for(int i=0; i<4; i++)
            if(cards[i]!=null)
                cards[i].render(g,sa);
        if(openCard != null)
            openCard.render(g,sa);
    }
    
    public void update(){
        for(int i=0; i<4; i++)
            if(cards[i]!=null)
                cards[i].update();
        if(openCard != null)
            openCard.update();
    }
}
