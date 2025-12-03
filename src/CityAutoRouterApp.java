import Controller.Controller;
import UI.RouterGUI;

import java.io.IOException;
import java.sql.SQLException;

public class CityAutoRouterApp {
    public static void main(String[] args) throws SQLException, IOException {
        Controller car = new Controller();
        RouterGUI ui = new RouterGUI(car);
        ui.setVisible(true);
    }
}
