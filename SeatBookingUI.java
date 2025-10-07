import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

// -----------------------------------------------------------
// DATA MODEL: Booking Class (must be Serializable)
// -----------------------------------------------------------
class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public String userId;
    public String seatId;

    public Booking(String name, String userId, String seatId) {
        this.name = name;
        this.userId = userId;
        this.seatId = seatId;
    }
    
    @Override
    public String toString() {
        return "Booking [name=" + name + ", userId=" + userId + ", seatId=" + seatId + "]";
    }
}

// -----------------------------------------------------------
// MAIN APPLICATION CLASS
// -----------------------------------------------------------
public class SeatBookingUI extends JFrame implements ActionListener {
    
    // --- UI Components ---
    private final char[] ROW_LETTERS = {'A', 'B', 'C', 'D', 'E', 'F'}; 
    private JTextField nameField, idField, seatField;
    private JButton selectButton, cancelButton;

    // --- Data Structures for Persistence ---
    private HashMap<String, Booking> bookings; // Key: SeatID ("A1") -> Stores confirmed bookings
    private Queue<Booking> waitingList;       // Stores users waiting for a specific seat
    private final String DATA_FILE = "bookings_data.ser"; 
    
    // --- Color Constants ---
    private final Color AVAILABLE_COLOR = new Color(200, 220, 240);
    private final Color BOOKED_COLOR = Color.GREEN.darker();
    
    // Helper to store all seat buttons for easy lookup/updating (T1.4)
    private HashMap<String, JButton> seatButtons = new HashMap<>();

    public SeatBookingUI() {
        // --- DATA INITIALIZATION ---
        loadData(); // Initializes/loads bookings and waitingList

        // --- FRAME SETUP ---
        setTitle("Seat Booking System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // T3.2: Attach hook to save data before application closes
        attachShutdownHook();

        // --- UI BUILD ---
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createSeatLayoutPanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.EAST);
        add(createFooterLabel(), BorderLayout.SOUTH);

        // T1.4: Restore UI colors based on loaded data
        restoreSeatColors();

        setVisible(true);
    }

    // ==========================================================
    // UI COMPONENT CREATION METHODS
    // ==========================================================
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        headerPanel.setBackground(Color.DARK_GRAY);
        String[] menuItems = {"Home", "About", "Contact", "Location"};
        for (String item : menuItems) {
            JLabel label = new JLabel(item);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            headerPanel.add(label);
        }
        return headerPanel;
    }

    private JPanel createSeatLayoutPanel() {
        JPanel seatLayoutWrapper = new JPanel(new BorderLayout(5, 5));
        seatLayoutWrapper.setBorder(BorderFactory.createTitledBorder("SEAT LAYOUT"));
        
        JPanel mainSeatPanel = new JPanel(new BorderLayout(4, 4));

        // --- Column numbers (top) ---
        JPanel colLabelPanel = new JPanel(new GridLayout(1, 12, 4, 4));
        colLabelPanel.add(new JLabel("")); // empty corner
        for (int i = 1; i <= 10; i++) {
            if (i == 6) {
                colLabelPanel.add(new JLabel("")); // Aisle gap
            }
            JLabel lbl = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            colLabelPanel.add(lbl);
        }
        mainSeatPanel.add(colLabelPanel, BorderLayout.NORTH);

        // --- Row letters + seats ---
        JPanel rowSeatPanel = new JPanel(new GridLayout(ROW_LETTERS.length, 12, 4, 4)); 
        
        for (char row : ROW_LETTERS) {
            JLabel rowLbl = new JLabel(String.valueOf(row), SwingConstants.CENTER);
            rowLbl.setOpaque(true);
            rowLbl.setBackground(Color.DARK_GRAY.darker());
            rowLbl.setForeground(Color.WHITE);
            rowSeatPanel.add(rowLbl);

            for (int c = 1; c <= 10; c++) {
                if (c == 6) {
                    JPanel gap = new JPanel();
                    gap.setBackground(getBackground()); 
                    rowSeatPanel.add(gap);
                }
                
                String seatID = row + String.valueOf(c);
                JButton seatBtn = createSeatButton(seatID); 
                seatBtn.addActionListener(this); 
                seatButtons.put(seatID, seatBtn); // Store button reference
                rowSeatPanel.add(seatBtn);
            }
        }
        
        // --- Seat Layout and Gap Wrapper ---
        JPanel seatAndGapWrapper = new JPanel();
        seatAndGapWrapper.setLayout(new BoxLayout(seatAndGapWrapper, BoxLayout.Y_AXIS)); 
        rowSeatPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        seatAndGapWrapper.add(rowSeatPanel);
        
        // Gap between seats and screen
        seatAndGapWrapper.add(Box.createRigidArea(new Dimension(0, 30))); 
        mainSeatPanel.add(seatAndGapWrapper, BorderLayout.CENTER);


        // --- Screen at bottom ---
        JLabel screenLabel = new JLabel("SCREEN", SwingConstants.CENTER);
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.BLACK);
        screenLabel.setForeground(Color.WHITE);
        screenLabel.setFont(new Font("Arial", Font.BOLD, 18));
        screenLabel.setPreferredSize(new Dimension(mainSeatPanel.getWidth(), 50)); 
        
        mainSeatPanel.add(screenLabel, BorderLayout.SOUTH);
        
        seatLayoutWrapper.add(mainSeatPanel, BorderLayout.CENTER);
        return seatLayoutWrapper;
    }

    private JPanel createFormPanel() {
        JPanel formWrapperPanel = new JPanel(new BorderLayout()); 
        formWrapperPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20)); 

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("NAME:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; nameField = new JTextField(15); formPanel.add(nameField, gbc);

        // ID
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; idField = new JTextField(15); formPanel.add(idField, gbc);

        // Seat No
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("SEAT NO:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; seatField = new JTextField(15); seatField.setEditable(false); formPanel.add(seatField, gbc);
        
        // Buttons
        selectButton = new JButton("SELECT");
        selectButton.setBackground(BOOKED_COLOR);
        selectButton.setForeground(Color.WHITE);
        selectButton.addActionListener(this); 
        
        cancelButton = new JButton("CANCEL");
        cancelButton.setBackground(Color.RED.darker());
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(this); 

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST; formPanel.add(selectButton, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST; formPanel.add(cancelButton, gbc);

        formWrapperPanel.add(formPanel, BorderLayout.NORTH);
        return formWrapperPanel;
    }

    private JLabel createFooterLabel() {
        JLabel footerLabel = new JLabel("Contact: 9876543210 | About: Simple Seat Booking System", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        return footerLabel;
    }
    
    private JButton createSeatButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(50, 50));
        btn.setBackground(AVAILABLE_COLOR);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        return btn;
    }

    // ==========================================================
    // PERSISTENCE METHODS (Serialization)
    // ==========================================================
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            bookings = (HashMap<String, Booking>) in.readObject();
            waitingList = (Queue<Booking>) in.readObject();
            System.out.println("Data loaded successfully. " + bookings.size() + " bookings and " + waitingList.size() + " waiting entries restored.");

        } catch (FileNotFoundException e) {
            bookings = new HashMap<>();
            waitingList = new LinkedList<>();
            System.out.println("No saved data found. Starting fresh.");
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            bookings = new HashMap<>();
            waitingList = new LinkedList<>();
        }
    }

    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(bookings);
            out.writeObject(waitingList);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void attachShutdownHook() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                saveData();
                // Note: The JFrame's EXIT_ON_CLOSE handles System.exit(0)
            }
        });
    }

    // ==========================================================
    // UI RESTORATION & CORE LOGIC
    // ==========================================================
    
    private void restoreSeatColors() {
        for (String seatID : bookings.keySet()) {
            JButton seatBtn = seatButtons.get(seatID);
            if (seatBtn != null) {
                seatBtn.setBackground(BOOKED_COLOR);
                seatBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                seatBtn.setForeground(Color.WHITE);
            }
        }
    }
    
    // T2.2 & T2.3: SELECT Button Logic
    private void handleBooking() {
        String seatID = seatField.getText();
        String name = nameField.getText().trim();
        String userId = idField.getText().trim();

        // T2.2 (Validation)
        if (seatID.isEmpty() || name.isEmpty() || userId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a seat and fill out Name and ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (bookings.containsKey(seatID)) {
            // T2.3: Seat is BOOKED - Ask about Waiting List
            int option = JOptionPane.showConfirmDialog(this,
                    "Seat " + seatID + " is already booked. Do you want to join the waiting list?",
                    "Seat Booked",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                Booking newWait = new Booking(name, userId, seatID);
                waitingList.offer(newWait); // Add to queue (FIFO)
                JOptionPane.showMessageDialog(this, "You have been added to the waiting list for seat " + seatID + ".", "Waiting List", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // T2.2: Seat is FREE - Finalize Booking
            Booking newBooking = new Booking(name, userId, seatID);
            bookings.put(seatID, newBooking);

            // UI Update: Change button color to green (Booked)
            JButton seatBtn = seatButtons.get(seatID);
            if (seatBtn != null) {
                seatBtn.setBackground(BOOKED_COLOR);
                seatBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                seatBtn.setForeground(Color.WHITE);
            }

            JOptionPane.showMessageDialog(this, "Successfully booked seat " + seatID + " for " + name + "!", "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
        }
        
        // Clear the form after action
        nameField.setText("");
        idField.setText("");
        seatField.setText("");
    }

    // T2.4 & T2.5: CANCEL Button Logic
    private void handleCancellation() {
        String seatID = seatField.getText();

        if (seatID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a booked seat to cancel.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!bookings.containsKey(seatID)) {
            JOptionPane.showMessageDialog(this, "Seat " + seatID + " is not currently booked.", "Cancellation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // T2.4: Remove booking
        Booking cancelledBooking = bookings.remove(seatID);

        // UI Update: Reset button color to default (Available)
        JButton seatBtn = seatButtons.get(seatID);
        if (seatBtn != null) {
            seatBtn.setBackground(AVAILABLE_COLOR);
            seatBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            seatBtn.setForeground(Color.BLACK);
        }

        JOptionPane.showMessageDialog(this, "Booking for seat " + seatID + " (User: " + cancelledBooking.name + ") has been canceled.", "Cancellation Confirmed", JOptionPane.INFORMATION_MESSAGE);

        // T2.5: Check Waiting List for next assignment
        assignSeatFromWaitingList(seatID);
        
        // Clear the form after action
        nameField.setText("");
        idField.setText("");
        seatField.setText("");
    }

    // T2.5 Helper method for Queue processing
    private void assignSeatFromWaitingList(String releasedSeatID) {
        
        Booking nextInQueue = null;
        Queue<Booking> tempQueue = new LinkedList<>();
        
        // Use a temporary queue to find and extract the first matching waiting user
        while (!waitingList.isEmpty()) {
            Booking waiting = waitingList.poll();
            if (waiting.seatId.equals(releasedSeatID) && nextInQueue == null) {
                nextInQueue = waiting; // Found the user!
            } else {
                tempQueue.offer(waiting); // Put non-matching and subsequent users back
            }
        }
        
        // Restore remaining waiting users to the main queue
        waitingList.addAll(tempQueue);
        
        if (nextInQueue != null) {
            // Assign the seat automatically.
            bookings.put(releasedSeatID, nextInQueue);
            
            // UI Update: Mark the seat as booked (Green)
            JButton seatBtn = seatButtons.get(releasedSeatID);
            if (seatBtn != null) {
                seatBtn.setBackground(BOOKED_COLOR);
                seatBtn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                seatBtn.setForeground(Color.WHITE);
            }

            JOptionPane.showMessageDialog(this,
                "Seat " + releasedSeatID + " has been automatically assigned to " + nextInQueue.name + " (Waiting List).",
                "Seat Assigned",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // ==========================================================
    // EVENT HANDLING
    // ==========================================================
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton) {
            JButton clickedButton = (JButton) e.getSource();
            String buttonText = clickedButton.getText();

            if (seatButtons.containsKey(buttonText)) {
                // Seat button clicked (T2.1)
                seatField.setText(buttonText); 
            } else if (clickedButton == selectButton) {
                handleBooking(); 
            } else if (clickedButton == cancelButton) {
                handleCancellation(); 
            }
        }
    }

    public static void main(String[] args) {
        // Ensure all Swing operations are run on the Event Dispatch Thread
        SwingUtilities.invokeLater(SeatBookingUI::new);
    }
}