package com.ensa.v2school.sm.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML
    private AnchorPane centerAnchor;

    @FXML
    private AnchorPane sideBar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadView("Stats");
    }
    public void loadView(String viewName){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/ensa/v2school/sm/"+viewName+".fxml"));
            AnchorPane view = fxmlLoader.load();
            centerAnchor.getChildren().clear();
            centerAnchor.getChildren().add(view);
            AnchorPane.setTopAnchor(view,0.0);
            AnchorPane.setLeftAnchor(view,0.0);
            AnchorPane.setRightAnchor(view,0.0);
            AnchorPane.setBottomAnchor(view,0.0);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    public void handleStats(ActionEvent e){
        setActive((Button) e.getSource());
        loadView("Stats");
    }
    public void handleLogout(){
        navigateTo("Login");
    }
    public void handleMajors(ActionEvent e){
        setActive((Button) e.getSource());
        loadView("Majors");
    }
    public void navigateTo(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ensa/v2school/sm/" + viewName + ".fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) sideBar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setActive(Button activeBtn) {
        sideBar.getChildren().stream()
                .filter(n -> n instanceof Button)
                .forEach(n -> n.getStyleClass().remove("sidebar-active"));

        activeBtn.getStyleClass().add("sidebar-active");
    }

    @FXML
    private void handleStudents(ActionEvent e) {
        setActive((Button) e.getSource());
        loadView("Students");
    }

    @FXML
    public void handleSubjects(ActionEvent e) {
        setActive((Button) e.getSource());
        loadView("subjects");
    }
}
