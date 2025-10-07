import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TrafficLights extends JFrame implements ActionListener, Escape{
    JRadioButton Rbutton, Gbutton, Ybutton;
    JPanel mainPanel;

    public TrafficLights(){
        setTitle("Traffic Lights Simulation");
        setSize(400,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setBounds(100,100,400,400);
        setVisible(true);

    }

    public static void main(String[] args) {
        new TrafficLights();
    }
}