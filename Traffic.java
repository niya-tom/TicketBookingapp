import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Traffic extends JFrame implements ActionListener {
    JRadioButton redButton, yellowButton, greenButton;
    JPanel lightPanel;
    Color currentColor = Color.GRAY;
    JPanel calcPanel;

    public Traffic() {
        setTitle("Traffic Light");
        setSize(350, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Using absolute positioning for the frame

        // Main container panel
        calcPanel = new JPanel();
        calcPanel.setLayout(null);   // Manual bounds inside panel
        calcPanel.setBounds(20, 20, 300, 420); // Position of the panel
        calcPanel.setBackground(Color.LIGHT_GRAY);
        add(calcPanel);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Select Light"));
        buttonPanel.setBounds(50, 320, 200, 80); // position inside calcPanel

        redButton = new JRadioButton("Red");
        yellowButton = new JRadioButton("Yellow");
        greenButton = new JRadioButton("Green");

        // Group buttons
        ButtonGroup group = new ButtonGroup();
        group.add(redButton);
        group.add(yellowButton);
        group.add(greenButton);

        // Add to buttonPanel
        buttonPanel.add(redButton);
        buttonPanel.add(yellowButton);
        buttonPanel.add(greenButton);

        calcPanel.add(buttonPanel);

        // Panel for light
        lightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Background rectangle (traffic box)
                g.setColor(Color.BLACK);
                g.fillRect(70, 30, 100, 260);

                // Red light
                g.setColor(currentColor == Color.RED ? Color.RED : Color.DARK_GRAY);
                g.fillOval(90, 40, 60, 60);

                // Yellow light
                g.setColor(currentColor == Color.YELLOW ? Color.YELLOW : Color.DARK_GRAY);
                g.fillOval(90, 120, 60, 60);

                // Green light
                g.setColor(currentColor == Color.GREEN ? Color.GREEN : Color.DARK_GRAY);
                g.fillOval(90, 200, 60, 60);
            }
        };
        lightPanel.setBounds(0, 0, 300, 300);
        calcPanel.add(lightPanel);

        // Add listeners
        redButton.addActionListener(this);
        yellowButton.addActionListener(this);
        greenButton.addActionListener(this);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == redButton) {
            currentColor = Color.RED;
        } else if (e.getSource() == yellowButton) {
            currentColor = Color.YELLOW;
        } else if (e.getSource() == greenButton) {
            currentColor = Color.GREEN;
        }
        lightPanel.repaint();
    }

    public static void main(String[] args) {
        new Traffic();
    }
}
