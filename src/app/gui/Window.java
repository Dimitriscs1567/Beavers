package app.gui;

import java.awt.Dimension;
import javax.swing.JFrame;

public class Window extends JFrame{
    
    private boolean isVisible;
    
    public Window(String title, boolean isVisible, boolean focus){
        this.isVisible = isVisible;
        //this.setSize(new Dimension(1500, 800));
        this.setResizable(false);
        this.setTitle(title);
        this.setLocationRelativeTo(null);
        this.setUndecorated(true);
        this.setVisible(isVisible);
        this.requestFocus(focus);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.createBufferStrategy(2);
    }
}
