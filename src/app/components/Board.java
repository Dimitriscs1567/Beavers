package app.components;

import java.awt.Graphics;
import java.util.Random;
import java.util.Stack;

public class Board {
    
    private Stack<Card> closedCards;
    private Stack<Card> openCards;
    private int width, height;
    private boolean isShuffling, updRenderAll;
    
    public Board(int width, int height){
        this.width = width;
        this.height = height;
        closedCards = new Stack<>();
        openCards = new Stack<>();
        isShuffling = false;
        updRenderAll = false;
        fill();
    }
    
    private void fill(){
        int[] types = new int[13];
        int newCard;
        for(int i=0; i<13; i++)
            types[i] = 0;
        Random rand = new Random();
        while(closedCards.size() < 66){
            newCard = rand.nextInt(13);
            while(fillViolations(newCard, types[newCard]))
                newCard = rand.nextInt(13);
            types[newCard]++;
            closedCards.push(new Card(newCard, width, height));
        }
    }
    
    private boolean fillViolations(int newCard, int times){
        if(newCard >= 0 && newCard <= 8 && times == 4)
            return(true);
        else if(newCard == 9 && times == 9)
            return(true);
        else if(newCard >= 10 && times == 7)
            return(true);
        return(false);
    }
    
    public void render(Graphics g, boolean sa){
        if(!updRenderAll){
            if(closedCards.size() > 1)
                closedCards.get(closedCards.size() - 2).render(g,sa);
            if(openCards.size() > 1)
                openCards.get(openCards.size() - 2).render(g,sa);

            if(!closedCards.empty())
                closedCards.lastElement().render(g,sa);
            if(!openCards.empty())
                openCards.lastElement().render(g,sa);
        }
        else{
            if(!closedCards.empty())
                for(int i=0; i<closedCards.size(); i++)
                    closedCards.get(i).render(g, sa);
            if(!openCards.empty())
                for(int i=0; i<openCards.size(); i++)
                    openCards.get(i).render(g, sa);
        }
    }
    
    public void update(){
        if(!updRenderAll){
            if(closedCards.size() > 1)
                closedCards.get(closedCards.size() - 2).update();
            if(openCards.size() > 1)
                openCards.get(openCards.size() - 2).update();

            if(!closedCards.empty())
                closedCards.lastElement().update();
            if(!openCards.empty())
                openCards.lastElement().update();
        }
        else{
            if(!closedCards.empty())
                for(int i=0; i<closedCards.size(); i++)
                    closedCards.get(i).update();
            if(!openCards.empty())
                for(int i=0; i<openCards.size(); i++)
                    openCards.get(i).update();
        }
    }
    
    public Card getCard(int choice){
        if(choice == Card.closedCardsStack && !closedCards.empty())
            return(closedCards.pop());
        else if(choice == Card.openCardsStack && !openCards.empty())
            return(openCards.pop());
        return(null);
    }
    
    public Card seeCard(int choice){
        if(choice == Card.closedCardsStack && !closedCards.empty())
            return(closedCards.lastElement());
        else if(choice == Card.openCardsStack && !openCards.empty())
            return(openCards.lastElement());
        return(null);
    }
    
    public void giveCard(Card card, int choice){
        if(choice == Card.closedCardsStack)
            closedCards.push(card);
        else
            openCards.push(card);
    }
    
    public void openCard(){
        Card tempcard = closedCards.pop();
        tempcard.setOpened(0);
        tempcard.goTo(Card.openCardsStack, 0);
        openCards.push(tempcard);
    }
    
    public void setShineCard(boolean[] shine){
        if(!closedCards.empty()){
            closedCards.lastElement().canBeChoosed(shine[0]);
        }
        
        if(!openCards.empty()){
            openCards.lastElement().canBeChoosed(shine[1]);
        }
            
    }
    
    public void shuffleCards(boolean allCards){
        isShuffling = true;
        updRenderAll = true;
        Random nextcard = new Random();
        int temp1;
        if(!allCards)
            temp1 = nextcard.nextInt(openCards.size()-1);
        else
            temp1 = nextcard.nextInt(openCards.size());
            
        openCards.get(temp1).setClosed();
        openCards.get(temp1).goTo(Card.closedCardsStack, 0);
        closedCards.push(openCards.remove(temp1));
        if(openCards.size() == 1 && !allCards)
            isShuffling = false;
        if(openCards.size() == 0 && allCards)
            isShuffling = false;
    }
    
    public boolean needToShuffle(){
        int i = closedCards.size()-1;
        if(i < 0)
            return(true);
        else{
            while(i >= 0 && closedCards.get(i).getType() == Card.SecondChance){
                if(i < 2)
                    return(true);
                else if(closedCards.get(i-1).getType() == Card.SecondChance)
                    i--;
                else if(closedCards.get(i-2).getType() == Card.SecondChance)
                    i-=2;
                else
                    i=-1;
            }
        }
        return(false);
    }
    
    public boolean isShuffling(){
        return(isShuffling);
    }
    
    public void setUpdRenderAll(boolean b){
        updRenderAll = b;
    }
}
