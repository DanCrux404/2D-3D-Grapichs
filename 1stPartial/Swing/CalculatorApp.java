import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class CalculatorApp extends JFrame {

    private JTextField display;

    public CalculatorApp() {

        // Window configuration
        setTitle("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel();

        // Display configuration
        display = new JTextField("0");
        display.setEditable(false);
        display.setHorizontalAlignment(SwingConstants.LEFT);

        mainPanel.setLayout(new java.awt.BorderLayout());
        mainPanel.add(display, java.awt.BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(1, 1, 1, 1);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        //addButton(panel,constraints,text,column,row,width,height);

        // First row
        addButton(buttonPanel, constraints, "C", 0, 0, 1, 1);
        addButton(buttonPanel, constraints, "/", 1, 0, 1, 1);
        addButton(buttonPanel, constraints, "*", 2, 0, 1, 1);
        addButton(buttonPanel, constraints, "-", 3, 0, 1, 1);

        // Second row
        addButton(buttonPanel, constraints, "7", 0, 1, 1, 1);
        addButton(buttonPanel, constraints, "8", 1, 1, 1, 1);
        addButton(buttonPanel, constraints, "9", 2, 1, 1, 1);

        // Plus button occupies two rows
        addButton(buttonPanel, constraints, "+", 3, 1, 1, 2);

        // Third row
        addButton(buttonPanel, constraints, "4", 0, 2, 1, 1);
        addButton(buttonPanel, constraints, "5", 1, 2, 1, 1);
        addButton(buttonPanel, constraints, "6", 2, 2, 1, 1);

        // Fourth row
        addButton(buttonPanel, constraints, "1", 0, 3, 1, 1);
        addButton(buttonPanel, constraints, "2", 1, 3, 1, 1);
        addButton(buttonPanel, constraints, "3", 2, 3, 1, 1);

        // Equals button occupies two rows
        addButton(buttonPanel, constraints, "=", 3, 3, 1, 2);

        // Fifth row
        // Zero button occupies two columns
        addButton(buttonPanel, constraints, "0", 0, 4, 2, 1);
        addButton(buttonPanel, constraints, ".", 2, 4, 1, 1);

        mainPanel.add(buttonPanel, java.awt.BorderLayout.CENTER);

        // Window size and position
        add(mainPanel);
        setSize(205, 240);
        setLocationRelativeTo(null);
    }

    // Creates and adds a button to the grid
    private void addButton(
            JPanel panel,
            GridBagConstraints constraints,
            String text,
            int column,
            int row,
            int width,
            int height) {

        JButton button = new JButton(text);

        constraints.gridx = column;
        constraints.gridy = row;
        constraints.gridwidth = width;
        constraints.gridheight = height;

        panel.add(button, constraints);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            CalculatorApp calculator = new CalculatorApp();
            calculator.setVisible(true);
        });
    }
}
