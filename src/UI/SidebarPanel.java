package UI;

import Controller.Controller;
import Routing.Route;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SidebarPanel extends JPanel {
    private static final Insets TOP_PADDING = new Insets(5, 5, 0, 5);
    private static final Insets NO_TOP_PADDING = new Insets(0, 5, 0, 5);

    private final RouterGUI myGUI;
    private final Controller myController;
    private final JFormattedTextField myStartInput;
    private final JFormattedTextField myDestinationInput;
    private final JPanel myRoutesPanel;

    public SidebarPanel(RouterGUI gui, Controller controller) {
        this.myGUI = gui;
        this.myController = controller;
        this.setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        // route creation options
        c.gridy = 0;
        c.insets = TOP_PADDING;
        this.add(new JLabel("Start intersection"), c);
        this.myStartInput = new JFormattedTextField(NumberFormat.getNumberInstance());
        this.myStartInput.setValue(1);
        c.gridy = 1;
        c.insets = NO_TOP_PADDING;
        this.add(this.myStartInput, c);
        c.gridy = 2;
        c.insets = TOP_PADDING;
        this.add(new JLabel("Destination intersection"), c);
        this.myDestinationInput = new JFormattedTextField(NumberFormat.getNumberInstance());
        this.myDestinationInput.setValue(56);
        c.gridy = 3;
        c.insets = NO_TOP_PADDING;
        this.add(this.myDestinationInput, c);

        final JButton computeRoutesButton = new JButton("Calculate routes");
        computeRoutesButton.addActionListener(theEvent -> {
            // TODO: validate that an intersection with these IDs exists, show error dialog if not
            myGUI.computeRoutes(((Number) this.myStartInput.getValue()).intValue(), ((Number) this.myDestinationInput.getValue()).intValue());
        });
        c.gridy = 4;
        c.insets = TOP_PADDING;
        this.add(computeRoutesButton, c);

        // calculated routes display
        c.gridy = 5;
        c.insets = TOP_PADDING;
        this.add(new JLabel("Routes:"), c);

        this.myRoutesPanel = new JPanel();
        c.gridy = 6;
        c.insets = TOP_PADDING;
        this.add(this.myRoutesPanel, c);
        this.myRoutesPanel.setLayout(new GridBagLayout());
        this.myRoutesPanel.add(new JLabel("(no computed routes yet)"));

        // consume the remaining space
        c.gridy = 99;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.PAGE_END;
        this.add(new JLabel(), c);
    }

    public void setRoutes(final Route[] theRoutes) {
        myRoutesPanel.removeAll();
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1.0;

        c.gridy = 0;
        final String countText = theRoutes.length == 1 ? "There is one optimal route!" : String.format("%d routes", theRoutes.length);
        myRoutesPanel.add(new JLabel(countText), c);

        for(final Route route : theRoutes) {
            final double routeTime = myController.routeTime(route);
            final double routeSafety = myController.routeSafety(route);

            final JLabel routeSummary = new JLabel(String.format("%.1f minutes, safety risk: %.4f", routeTime, routeSafety));
            routeSummary.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            c.gridy += 1;
            myRoutesPanel.add(routeSummary, c);

            final JButton viewRouteDetails = new JButton("details");
            viewRouteDetails.addActionListener(theEvent ->
                JOptionPane.showMessageDialog(this, route.toDirections(), "Route details", JOptionPane.INFORMATION_MESSAGE)
            );
            c.gridy += 1;
            myRoutesPanel.add(viewRouteDetails, c);
        }
    }
}
