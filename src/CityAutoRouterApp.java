import Controller.Controller;
import UI.RouterGUI;

import java.io.IOException;
import java.sql.SQLException;

public class CityAutoRouterApp {
    public static void main(String[] args) throws SQLException, IOException {
        Controller car;
        try {
            car = new Controller();
        } catch (SQLException e) {
            car = new Controller(1, 1);
        }
        RouterGUI ui = new RouterGUI(car);
        ui.setVisible(true);
    }
}
