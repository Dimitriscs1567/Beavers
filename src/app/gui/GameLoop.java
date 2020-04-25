package app.gui;

import app.components.Game;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import javax.swing.JPanel;

public class GameLoop extends JPanel implements Runnable {
    
    private Renderer render;
    private Game newGame;
    private Window mainWindow;
    private Thread mainThread;
    private LoadingScreen ls;
    private boolean isLoading, isRunning;
    private String[] names;
    private int numOfPlayers, turns;

    public GameLoop(){
        mainWindow = new Window("Beaver Game", true, true);
        ls = new LoadingScreen(mainWindow.getBufferStrategy());
        isLoading = true;
        isRunning = true;
        mainThread = new Thread(this);
        mainThread.start();
    }
 
    public void start(){
        numOfPlayers = 4;
        turns = 20;
        names = new String[numOfPlayers];
        names[0] = "MeThePlayer";
        for(int i=1; i<numOfPlayers; i++)
            names[i] = "Player" + String.valueOf(i);
        newGame = new Game(mainWindow.getWidth(), mainWindow.getHeight(), numOfPlayers, names, turns);
        render = new Renderer(mainWindow.getWidth(), mainWindow.getHeight(), mainWindow.getBufferStrategy(), newGame);
        mainWindow.addMouseListener(render.getMouseListener());
        mainWindow.addMouseMotionListener(render.getMouseListener());
        mainWindow.addMouseWheelListener(render.getMouseListener());
        mainWindow.addKeyListener(render.getKeyboardListener());
        newGame.start();
        isLoading = false;
    }
    
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfUps = 60.0;
        double amountOfFps = 60.0;
        double ns1 = 1000000000 / amountOfUps;
        double ns2 = 1000000000 / amountOfFps;
        double delta1 = 0;
        double delta2 = 0;
        long timer = System.currentTimeMillis();
        int updates = 0;
        int frames = 0;
        while(isRunning){
            long now = System.nanoTime();
            delta1 += (now - lastTime) / ns1;
            delta2 += (now - lastTime) / ns2;
            lastTime = now;
            
            while(delta1 >= 1){
                if(newGame != null)
                    newGame.update();
                updates++;
                delta1--;
            }
            
            //Unlimited fps
            // if(!isLoading)
            //     render.render();
            // else 
            //     ls.render();
            // frames++;
            
            //Limited fps 
            while(delta2 >= 1){
                if(!isLoading)
                    render.render();
                else
                    ls.render();
                frames++;
                delta2--;
            }
            
            if(System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                //System.out.println("FPS: " + frames + " UPS: " + updates);
                frames = 0;
                updates = 0;
            }
            
            if(render != null && render.getAppEnd())
                isRunning = false;
        }
        System.exit(0);
    }
    
    private class LoadingScreen extends JPanel{
        private BufferStrategy bf;
        
        public LoadingScreen(BufferStrategy bf){
            this.bf = bf;
        }
        
        public void render(){
            Graphics g = bf.getDrawGraphics();
            g.clearRect(0, 0, mainWindow.getWidth(), mainWindow.getHeight());
            g.setColor(Color.black);
            g.drawRect(mainWindow.getWidth()/2 - 300, mainWindow.getHeight()/2 - 300, 300, 300);
            g.dispose();
            bf.show();
            Toolkit.getDefaultToolkit().sync();
        }
    }
}
