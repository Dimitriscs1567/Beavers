package app.components;

import java.awt.event.MouseEvent;

public class User extends Player{
    
    private boolean canChooseStack, canChooseCloseStack;
    private boolean[] shineStackCards, shineCards;
    
    public User(int id, String name, int numOfPlayers){
        super(id,name);
        canChooseStack = false;
        canChooseCloseStack = false;
        shineStackCards = new boolean[2];
        shineCards = new boolean[4*(numOfPlayers - 1)];
    }
    
    public void play(int type){
        canChooseStack = false;
        canChooseCloseStack = false;
        this.typeOfDecision = type;
    }
    
    public int[] getDesicion(){
        if(this.typeOfDecision == Player.changeCard){
            this.typeOfDecision = 0;
            return(decisionChangePeek);
        }
        else if(this.typeOfDecision == Player.swapCard){
            this.typeOfDecision = 0;
            return(decisionSwap);
        }
        else if(this.typeOfDecision == Player.peekCard){
            this.typeOfDecision = 0;
            return(decisionChangePeek);
        }
        return(null);
    }
    
    public int chooseStack(boolean onlyClose){
        if(!onlyClose){
            canChooseStack = true;
            canChooseCloseStack = true;
        }
        else{
            canChooseCloseStack = true;
            canChooseStack = false;
        }
        if(chosenStack != 0){
            int temp = chosenStack;
            chosenStack = 0;
            return(temp);
        }
        return(chosenStack);
    }
    
    public boolean[] getShineStackCards(){
        return(shineStackCards);
    }
    
    public boolean[] getShineCards(){
        return(shineCards);
    }
    
    private boolean hasChosenCard(){
        for(Card card:cards)
            if(card != null && card.isChosen())
                return(true);
        return(false);
    }
    
    public void mouseClicked(MouseEvent me, int player, int pos) {
        if((canChooseStack || canChooseCloseStack) && player == Card.closedCardsStack)
            chosenStack = Card.closedCardsStack;
        else if(canChooseStack && player == Card.openCardsStack)
            chosenStack = Card.openCardsStack;
        
        if((typeOfDecision == Player.changeCard || typeOfDecision == Player.peekCard) && player == this.id && pos >= 0){
            decisionChangePeek[0] = pos;
            this.hasDecided = true;
        }
        else if(typeOfDecision == Player.swapCard && pos >= 0){
            if(!hasChosenCard() && player == this.id){
                decisionSwap[0] = pos;
                this.cards[pos].setChosen(true);
            }
            else if(hasChosenCard() && player == this.id && this.cards[pos].isChosen())
                this.cards[pos].setChosen(false);
            else if(hasChosenCard() && player > 0){
                decisionSwap[1] = player;
                decisionSwap[2] = pos;
                this.hasDecided = true;
            }
        }
        
        if(openCard != null && openCard.getType() != Card.SecondChance && player == this.id && pos == Card.playersOpenCard){
            decisionChangePeek[0] = -1;
            decisionSwap[0] = -1;
            for(Card card:cards)
                if(card.isChosen())
                    card.setChosen(false);
            this.hasDecided = true;
        }
    }
    
    public void mouseExited(MouseEvent me) {
        for(Card card:cards)
                if(card != null)
                    card.canBeChoosed(false);
            for(int i = 0; i < shineCards.length; i++)
                shineCards[i] = false;
    }

    public void mouseMoved(MouseEvent me, int player, int pos) {
        if((canChooseStack || canChooseCloseStack) && player == Card.closedCardsStack)
            shineStackCards[0] = true;
        else if(canChooseStack && player == Card.openCardsStack)
            shineStackCards[1] = true;
        else{
            shineStackCards[0] = false;
            shineStackCards[1] = false;
        }
        
        if((typeOfDecision == Player.changeCard || typeOfDecision == Player.peekCard) && player == this.id && pos >= 0)
            this.cards[pos].canBeChoosed(true);
        else if(typeOfDecision == Player.swapCard && !hasChosenCard() && player == this.id && pos >= 0)
            this.cards[pos].canBeChoosed(true);
        else if(typeOfDecision == Player.swapCard && hasChosenCard() && player > 0 && pos >= 0)
            shineCards[4*(player-1)+pos] = true;
        else{
            for(Card card:cards)
                if(card != null)
                    card.canBeChoosed(false);
            for(int i = 0; i < shineCards.length; i++)
                shineCards[i] = false;
        }
        
        if(openCard != null && openCard.getType() != Card.SecondChance && player == this.id && pos == Card.playersOpenCard)
            openCard.canBeChoosed(true);
        else
            if(openCard != null)
                openCard.canBeChoosed(false);
    }
}
