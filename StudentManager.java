import java.io.*;
import java.util.*;

public class StudentManager {
    private static final String FILE_NAME = "students.ser";

    public static void main(String[] args) {
        // HashMap to store student details
        HashMap<Integer, String> students = new HashMap<>();

        // Try to load old data (if file exists)
        students = loadData();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Student Manager ---");
            System.out.println("1. Add Student");
            System.out.println("2. View Students");
            System.out.println("3. Save & Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter Roll No: ");
                    int roll = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Enter Name: ");
                    String name = sc.nextLine();
                    students.put(roll, name);
                    System.out.println("Student added!");
                    break;

                case 2:
                    System.out.println("Current Students:");
                    for (Map.Entry<Integer, String> entry : students.entrySet()) {
                        System.out.println("Roll No: " + entry.getKey() +
                                           ", Name: " + entry.getValue());
                    }
                    break;

                case 3:
                    saveData(students);
                    System.out.println("Data saved. Exiting...");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // Method to save HashMap into file
    public static void saveData(HashMap<Integer, String> students) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(students);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to load HashMap from file
    @SuppressWarnings("unchecked")
    public static HashMap<Integer, String> loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (HashMap<Integer, String>) ois.readObject();
        } catch (FileNotFoundException e) {
            // If file doesn't exist, return empty HashMap
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
