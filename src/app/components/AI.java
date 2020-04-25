package app.components;

import java.util.Random;

public class AI extends Player{
    private int[] knownCards, halfKnownCards;
    private boolean[] known;
    private boolean needChooseStack, runOnce;
    private int lastChosenStack, lastTurn;
    
    public AI(int id, String name, int numOfPlayers){
        super(id, name);
        knownCards = new int[4*numOfPlayers + 1];
        for(int i=0; i<knownCards.length; i++)
            knownCards[i] = -5;
        halfKnownCards = new int[4*numOfPlayers];
        for(int i=0; i<halfKnownCards.length; i++)
            halfKnownCards[i] = -5;
        known = new boolean[4*numOfPlayers];
        for(int i=0; i<known.length; i++){
            if(i%4 == 0 || i%4 == 3)
                known[i] = true;
            else
                known[i] = false;
        }
        needChooseStack = false;
        lastChosenStack = 0;
        runOnce = false;
        lastTurn = -1;
    }
    
    public void play(int type){
        runOnce = true;
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
        if(onlyClose){
            lastChosenStack = Card.closedCardsStack;
            return(Card.closedCardsStack);
        }
        else{
            needChooseStack = true;
            if(chosenStack != 0){
                int temp = chosenStack;
                lastChosenStack = chosenStack;
                chosenStack = 0;
                needChooseStack = false;
                return(temp);
            }
            return(chosenStack);
        }
    }
    
    private int evaluateWeight(float weight){
        Random chance = new Random();
        int i = 0, success = 0;
        
        if(weight < 0)
            return(Card.closedCardsStack);
        
        while(i<20){
            if(chance.nextFloat() <= weight)
                success++;
            i++;
        }
        if(success > 10)
            return(Card.openCardsStack);
        return(Card.closedCardsStack);
    }
    
    public void evaluatingSituation(){
        int[] max = maxCard(), min = minCard(), halfmin = minHalfCard(), halfmax = maxHalfCard(), mnum = maxNumber();
        int unknown = unknownCards();
        float weight = 0;
        if(needChooseStack){
            if(max[0] == -5 || (max[0] > 9 && mnum[0] <= 5) || (max[0] <= 5 && unknown > 0)){
                if(halfmax[0] == -5)
                    weight = evaluateNum(knownCards[knownCards.length - 1], unknown);
                else
                    weight = evaluateNums(halfmax[0]-1, knownCards[knownCards.length - 1], unknown);
            }
            else if(max[0] > 9 && mnum[0] > 5)
                    weight = evaluateNums(mnum[0], knownCards[knownCards.length - 1], unknown);
            else 
                weight = evaluateNums(max[0], knownCards[knownCards.length - 1], unknown);
            
            chosenStack = evaluateWeight(weight);
        }
        
        if(this.typeOfDecision != 0 && runOnce && lastChosenStack != 0){
            weight = 0;
            if(this.typeOfDecision == Player.changeCard && lastChosenStack == Card.openCardsStack)
                decisionChangePeek[0] = cardToBeChanged(max, mnum, halfmax, unknown);
            
            else if(this.typeOfDecision == Player.changeCard && lastChosenStack == Card.closedCardsStack){
                if(max[0] == -5 || (max[0] > 9 && mnum[0] <= 5) || (max[0] <= 5 && unknown > 0)){
                    if(halfmax[0] == -5)
                        weight = evaluateNum(openCard.getType(), unknown);
                    else
                        weight = evaluateNums(halfmax[0]-1, openCard.getType(), unknown);
                }
                else if(max[0] > 9 && mnum[0] > 5)
                    weight = evaluateNums(mnum[0], openCard.getType(), unknown);
                else 
                    weight = evaluateNums(max[0], openCard.getType(), unknown);
                
                if(evaluateWeight(weight) == Card.openCardsStack)
                    decisionChangePeek[0] = cardToBeChanged(max, mnum, halfmax, unknown);
                else
                    decisionChangePeek[0] = -1;
                
                if(decisionChangePeek[0] == -1){
                    if(openCard.getType() < mnum[0])
                        decisionChangePeek[0] = mnum[1];
                }
            }
            
            else if(this.typeOfDecision == Player.swapCard){
                int lastTurnPos = -1;
                float maxweight;
                float[] tempweights = new float[3];
                for(int i=0; i<3; i++)
                    tempweights[i] = 0;
                
                if(lastTurn != -1 && lastTurn != this.id){
                    for(int i=4*lastTurn; i < 4*lastTurn+4; i++){
                        if(lastTurnPos == -1 && known[i])
                            lastTurnPos = i%4;
                        else if(lastTurnPos != -1 && known[i])
                            if(halfKnownCards[i] != -5 && (halfKnownCards[i] <= 4 || halfKnownCards[i] < halfKnownCards[lastTurnPos]))
                                lastTurnPos = i%4;
                    }
                }
                                
                if(min[0] != -5){
                    if(max[0] == -5 || (max[0] > 9 && mnum[0] <= 5) || (max[0] <= 5 && unknown > 0)){
                        if(halfmax[0] == -5)
                            tempweights[0] = evaluateNum(min[0], unknown);
                        else
                            tempweights[0] = evaluateNums(halfmax[0]-1, min[0], unknown);
                    }
                    else if(max[0] > 9 && mnum[0] > 5)
                        tempweights[0] = evaluateNums(mnum[0], min[0], unknown);
                    else 
                        tempweights[0] = evaluateNums(max[0], min[0], unknown);
                }
                
                if(lastTurnPos != -1){
                    if(max[0] == -5 || (max[0] > 9 && mnum[0] <= 5) || (max[0] <= 5 && unknown > 0)){
                        if(halfmax[0] == -5)
                            tempweights[1] = evaluateNum(4, unknown);
                        else
                            tempweights[1] = evaluateNums(halfmax[0]-1, 4, unknown);
                    }
                    else if(max[0] > 9 && mnum[0] > 5)
                        tempweights[1] = evaluateNums(mnum[0], 4, unknown);
                    else 
                        tempweights[1] = evaluateNums(max[0], 4, unknown);
                }
                    
                if(halfmin[0] != -5){
                    if(max[0] == -5 || (max[0] > 9 && mnum[0] <= 5) || (max[0] <= 5 && unknown > 0)){
                        if(halfmax[0] == -5)
                            tempweights[2] = evaluateNum(halfmin[0]-1, unknown);
                        else
                            tempweights[2] = evaluateNums(halfmax[0]-1, halfmin[0]-1, unknown);
                    }
                    else if(max[0] > 9 && mnum[0] > 5)
                        tempweights[2] = evaluateNums(mnum[0], halfmin[0]-1, unknown);
                    else 
                        tempweights[2] = evaluateNums(max[0], halfmin[0]-1, unknown);
                }
                
                maxweight = tempweights[0];
                for(int i=1; i<3; i++)
                    if(maxweight < tempweights[i])
                        maxweight = tempweights[i];
                
                if(evaluateWeight(maxweight) == Card.openCardsStack){
                    if(maxweight == tempweights[0]){           
                        decisionSwap[0] = cardToBeChanged(max, mnum, halfmax, unknown);
                        decisionSwap[1] = min[1];
                        decisionSwap[2] = min[2];
                    }
                    else if(maxweight == tempweights[1]){
                        decisionSwap[0] = cardToBeChanged(max, mnum, halfmax, unknown);
                        decisionSwap[1] = lastTurn;
                        decisionSwap[2] = lastTurnPos;
                    }
                    else if(maxweight == tempweights[2]){
                        decisionSwap[0] = cardToBeChanged(max, mnum, halfmax, unknown);
                        decisionSwap[1] = halfmin[1];
                        decisionSwap[2] = halfmin[2];
                    }
                }
                else
                    decisionSwap[0] = -1;
            }
            
            else if(this.typeOfDecision == Player.peekCard){
                decisionChangePeek[0] = -1;
                if(lastTurn == -1){
                    for(int i=4*this.id; i < 4*this.id+4; i++)
                        if(knownCards[i] == -5)
                            decisionChangePeek[0] = i - (4*this.id);
                }
                else
                    decisionChangePeek[0] = -1;
            }
            
            runOnce = false;
            lastChosenStack = 0;
            this.hasDecided = true;
        }
    }
    
    private float evaluateNum(int num, int unknown){
        float weight = 0;
        switch(num){
            case 9: weight -= 0.1; break;
            case 8: weight = 0; break;
            case 7: weight += 0.15; break;
            case 6: weight += 0.45; break;
            case 5:weight += 0.65; break;
            default: weight += 1.0; break;
        }
        weight += 0.05*unknown;
        return(weight);
    }
    
    private float evaluateNums(int mine, int other, int unknown){
        float weight = 0;
        if(other >= mine)
            weight -= 2.0;
        else if(mine >= 8){
            switch(mine - other){
                case 1: weight += 0.1; break; // 7,8
                case 2: weight += 0.35; break; // 6,7
                case 3: weight += 0.6; break; // 5,6
                default: weight += 1.0; break; // <= 5
            }
        }
        else if(mine >= 6){
            switch(mine - other){
                case 1: weight += 0.6; break; //5,6
                case 2: weight += 0.8; break; // 4,5
                default: weight += 1.0; break; // <=4
            }
        }
        else if(mine > other)
            weight += 1.0;
            
        weight += 0.05*unknown;
        return(weight);
    }
    
    private int cardToBeChanged(int[] max, int[] mnum, int[] halfmax, int unknown){
        if(max[0] <= 5 && unknown > 0){
            if(halfmax[0] == -5){
                for(int i=4*this.id; i < 4*this.id+4; i++)
                    if(knownCards[i] == -5)
                        return(i%4);
            }
            else
                return(halfmax[1]);
        }
        else if(max[0] > 9 && mnum[0] > 5)
            return(mnum[1]);
        else
            return(max[1]);
        
        return(-1);
    }
    
    private int[] maxCard(){
        int[] max = new int[2];
        max[0] = -5;
        max[1] = 0;
        for(int i=4*this.id; i < 4*this.id+4; i++){
            if(max[0] == -5 && knownCards[i]!=-5){
                max[0] = knownCards[i];
                max[1] = i%4;
            }
            else if(max[0] < knownCards[i]){
                max[0] = knownCards[i];
                max[1] = i%4;
            }
        }
        return(max);
    }
    
    private int[] maxHalfCard(){
        int[] max = new int[2];
        max[0] = -5;
        max[1] = 0;
        for(int i=4*this.id; i < 4*this.id+4; i++){
            if(max[0] == -5 && halfKnownCards[i]!=-5 && knownCards[i] == -5){
                max[0] = halfKnownCards[i];
                max[1] = i%4;
            }
            else if(max[0] < halfKnownCards[i] && knownCards[i] == -5){
                max[0] = halfKnownCards[i];
                max[1] = i%4;
            }
        }
        return(max);
    }
    
    private int[] maxNumber(){
        int[] max = new int[2];
        max[0] = -5;
        max[1] = 0;
        for(int i=4*this.id; i < 4*this.id+4; i++){
            if(max[0] == -5 && knownCards[i]!=-5 && knownCards[i] <= 9){
                max[0] = knownCards[i];
                max[1] = i%4;
            }
            else if(max[0] < knownCards[i] && knownCards[i] <= 9){
                max[0] = knownCards[i];
                max[1] = i%4;
            }
        }
        return(max);
    }
    
    private int[] minCard(){
        int[] min = new int[3];
        min[0] = -5;
        min[1] = 0;
        min[2] = 0;
        for(int i = 0; i < knownCards.length - 1; i++){
            if((i < 4*this.id || i > 4*this.id+3) && min[0] == -5 && knownCards[i]!=-5){
                min[0] = knownCards[i];
                min[1] = i/4;
                min[2] = i%4;
            }
            else if((i < 4*this.id || i > 4*this.id+3) && min[0] > knownCards[i] && knownCards[i]!=-5){
                min[0] = knownCards[i];
                min[1] = i/4;
                min[2] = i%4;
            }
        }
        return(min);
    }
    
    private int[] minHalfCard(){
        int[] min = new int[3];
        min[0] = -5;
        min[1] = 0;
        min[2] = 0;
        for(int i = 0; i < halfKnownCards.length - 1; i++){
            if((i < 4*this.id || i > 4*this.id+3) && min[0] == -5 && halfKnownCards[i]!=-5){
                min[0] = halfKnownCards[i];
                min[1] = i/4;
                min[2] = i%4;
            }
            else if((i < 4*this.id || i > 4*this.id+3) && min[0] > halfKnownCards[i] && halfKnownCards[i]!=-5){
                min[0] = halfKnownCards[i];
                min[1] = i/4;
                min[2] = i%4;
            }
        }
        return(min);
    }
    
    private int unknownCards(){
        int unknown = 0;
        for(int i=4*this.id; i < 4*this.id+4; i++){
            if(knownCards[i] == -5)
                unknown++;
        }
        return(unknown);
    }
    
    public void learnCard(int player, int pos, int type){
        if(player == this.id)
            knownCards[4*player + pos] = cards[pos].getType();
        else if(pos > -1)
            knownCards[4*player + pos] = type;
        else
            knownCards[knownCards.length - 1] = type;
    }
    
    public void learnHalfCard(int player, int pos, int type){
        if(known[4*player + pos] && (halfKnownCards[4*player + pos] > type || halfKnownCards[4*player + pos] == -5)){
            if(type > 9)
                halfKnownCards[4*player + pos] = 9;
            else
                halfKnownCards[4*player + pos] = type;
            
            thrownCard(player, type);
        }
        else if(!known[4*player + pos])
            halfKnownCards[4*player + pos] = -5;
        known[4*player + pos] = true;
    }
    
    public void thrownCard(int player, int type){
        if(type <= 5)
            for(int i=4*player; i<4*player+4; i++)
                if((halfKnownCards[i] > type || halfKnownCards[i] == -5) && known[i])
                    halfKnownCards[i] = type;
    }
    
    public void isKnown(int player, int pos){
        known[4*player + pos] = true;
    }
    
    public void swapping(int player, int pos, int player2, int pos2){
        int temp = knownCards[4*player + pos];
        knownCards[4*player + pos] = knownCards[4*player2 + pos2];
        knownCards[4*player2 + pos2] = temp;
        
        int temp2 = halfKnownCards[4*player + pos];
        halfKnownCards[4*player + pos] = halfKnownCards[4*player2 + pos2];
        halfKnownCards[4*player2 + pos2] = temp2;
        
        known[4*player + pos] = false;
        known[4*player2 + pos2] = false;
    }
    
    public void setLastTurn(int player){
        lastTurn = player;
    }
    
    public boolean sayLastTurn(){
        int[] scores = new int[halfKnownCards.length/4];
        int[] unkonwns = new int[halfKnownCards.length/4];
        int[] minScore = new int[2] , minHalfScore = new int[2];
        boolean checkminScore = true;
        minScore[1] = -1;
        minHalfScore[1] = -1;
        for(int i=0; i<scores.length; i++){
            scores[i] = estimatedScore(i);
            unkonwns[i] = absoluteUnknownCards(i);
            
            if(unkonwns[i] > 0)
                checkminScore = false;
            
            if(minScore[1] == -1 && unkonwns[i] == 0){
                minScore[0] = scores[i];
                minScore[1] = i;
            }
            else if(minScore[1] != -1 && unkonwns[i] == 0 && minScore[0] > scores[i]){
                minScore[0] = scores[i];
                minScore[1] = i;
            }
            
            if(minHalfScore[1] == -1 && unkonwns[i] <= 2){
                minHalfScore[0] = scores[i];
                minHalfScore[1] = i;
            }
            else if(minHalfScore[1] != -1 && unkonwns[i] <= 2 && minHalfScore[0] > scores[i]){
                minHalfScore[0] = scores[i];
                minHalfScore[1] = i;
            }
        }
        
        if(minScore[1] == this.id && checkminScore)
            return(true);
        if(scores[this.id] < 10 && unkonwns[this.id] == 0 && !checkminScore)
            return(true);
        if(minHalfScore[1] == this.id && minHalfScore[0] <= 3 && unkonwns[this.id] == 2 && !checkminScore)
            return(true);
        if(minHalfScore[1] == this.id && minHalfScore[0] <= 5 && unkonwns[this.id] == 1 && !checkminScore)
            return(true);
        
        return(false);
    }
    
    private int estimatedScore(int player){
        int score = 0;
        for(int i=4*player; i<4*player+4; i++){
            if(knownCards[i] != -5){
                if(knownCards[i] <= 9)
                    score += knownCards[i];
            }
            else if(halfKnownCards[i] != -5)
                score += halfKnownCards[i]-1;
        }
        return(score);
    }
    
    private int absoluteUnknownCards(int player){
        int unknown = 0;
        for(int i=4*player; i<4*player+4; i++){
            if((knownCards[i] == -5 && halfKnownCards[i] == -5) || knownCards[i] > 9)
                unknown++;
        }
        return(unknown);
    }
    
    public void forgetTurn(){
        for(int i=0; i<knownCards.length; i++)
            knownCards[i] = -5;
        
        for(int i=0; i<halfKnownCards.length; i++){
            halfKnownCards[i] = -5;
            if(i%4 == 0 || i%4 == 3)
                known[i] = true;
            else
                known[i] = false;
        }
        
        lastTurn = -1;
    }
    
    public void print(){
        for(int i=0; i<knownCards.length - 1; i++){
            System.out.print(knownCards[i] + "  ");
            if((i+1)%4 == 0)
                System.out.println();
        }
        System.out.println();
        for(int i=0; i<known.length; i++){
            System.out.print(known[i] + "  ");
            if((i+1)%4 == 0)
                System.out.println();
        }
        System.out.println();
        for(int i=0; i<halfKnownCards.length; i++){
            System.out.print(halfKnownCards[i] + "  ");
            if((i+1)%4 == 0)
                System.out.println();
        }
        System.out.println();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!");
    }
}
