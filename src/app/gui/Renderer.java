package app.gui;

import app.components.AI;
import app.components.Card;
import app.components.Game;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import javax.swing.JPanel;

public class Renderer extends JPanel{
    
    private Game game;
    private BufferStrategy bf;
    private int width, height;
    private Mouse mouse;
    private Keyboard keyboard;
    private boolean showAll, appEnd;
    
    public Renderer(int width, int height, BufferStrategy bf, Game game){
        this.setPreferredSize(new Dimension(width, height));
        this.width = width;
        this.height = height;
        this.bf = bf;
        this.game = game;
        mouse = new Mouse();
        keyboard = new Keyboard();
        showAll = false;
        appEnd = false;
    }
    
    public Mouse getMouseListener(){
        return(mouse);
    }
    
    public Keyboard getKeyboardListener(){
        return(keyboard);
    }
    
    public boolean getAppEnd(){
        return(appEnd);
    }
    
    public void render(){
        Graphics g = bf.getDrawGraphics();
        g.clearRect(0, 0, width, height);
        game.render(g,showAll);
        g.dispose();
        bf.show();
        Toolkit.getDefaultToolkit().sync();
    }
    
    private class Mouse implements MouseListener, MouseMotionListener, MouseWheelListener{

        @Override
        public void mouseClicked(MouseEvent me) {
            int player = -5, pos = -5;
            if(game.getBoard().seeCard(Card.closedCardsStack) != null && game.getBoard().seeCard(Card.closedCardsStack).insideBounds(me.getX(), me.getY())){
                player =  Card.closedCardsStack;
                pos = 0;
            }
            else if(game.getBoard().seeCard(Card.openCardsStack) != null && game.getBoard().seeCard(Card.openCardsStack).insideBounds(me.getX(), me.getY())){
                player =  Card.openCardsStack;
                pos = 0;
            }
            else{
                for(AI ai:game.getPlayers()){
                    for(int j=0; j<4; j++){
                        if(ai.seeCard(j) != null && ai.seeCard(j).insideBounds(me.getX(), me.getY())){
                            player = ai.getID();
                            pos = j;
                        }
                    }
                }
                for(int j=0; j<4; j++)
                    if(game.getUser().seeCard(j) != null && game.getUser().seeCard(j).insideBounds(me.getX(), me.getY())){
                        player = game.getUser().getID();
                        pos = j;
                    }
                if(game.getUser().seeOpenCard() != null && game.getUser().seeOpenCard().insideBounds(me.getX(), me.getY())){
                    player = game.getUser().getID();
                    pos = Card.playersOpenCard;
                }
            }
            game.mouseClicked(me, player, pos);
        }

        @Override
        public void mousePressed(MouseEvent me) {
            game.mousePressed(me);
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            game.mouseReleased(me);
        }

        @Override
        public void mouseEntered(MouseEvent me) {
        }

        @Override
        public void mouseExited(MouseEvent me) {
            game.mouseExited(me);
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            game.mouseDragged(me);
        }

        @Override
        public void mouseMoved(MouseEvent me) {
            int player = -5, pos = -5;
            if(game.getBoard().seeCard(Card.closedCardsStack) != null && game.getBoard().seeCard(Card.closedCardsStack).insideBounds(me.getX(), me.getY())){
                player =  Card.closedCardsStack;
                pos = 0;
            }
            else if(game.getBoard().seeCard(Card.openCardsStack) != null && game.getBoard().seeCard(Card.openCardsStack).insideBounds(me.getX(), me.getY())){
                player =  Card.openCardsStack;
                pos = 0;
            }
            else{
                for(AI ai:game.getPlayers()){
                    for(int j=0; j<4; j++){
                        if(ai.seeCard(j) != null && ai.seeCard(j).insideBounds(me.getX(), me.getY())){
                            player = ai.getID();
                            pos = j;
                        }
                    }
                }
                for(int j=0; j<4; j++)
                    if(game.getUser().seeCard(j) != null && game.getUser().seeCard(j).insideBounds(me.getX(), me.getY())){
                        player = game.getUser().getID();
                        pos = j;
                    }
                if(game.getUser().seeOpenCard() != null && game.getUser().seeOpenCard().insideBounds(me.getX(), me.getY())){
                    player = game.getUser().getID();
                    pos = Card.playersOpenCard;
                }
            }
            game.mouseMoved(me, player, pos);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent mwe) {
            game.mouseWheelMoved(mwe.getWheelRotation());
        }
    }
    
    private class Keyboard implements KeyListener{

        @Override
        public void keyTyped(KeyEvent ke) {
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            if(ke.getKeyCode() == KeyEvent.VK_Q)
                showAll = true;
            if(ke.getKeyCode() == KeyEvent.VK_ESCAPE)
                appEnd = true;
        }

        @Override
        public void keyReleased(KeyEvent ke) {
            if(ke.getKeyCode() == KeyEvent.VK_Q)
                showAll = false;
        }
        
    }
}
