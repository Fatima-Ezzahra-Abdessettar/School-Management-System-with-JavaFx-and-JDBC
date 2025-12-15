package com.ensa.v2school.sm.Controllers;
import com.ensa.v2school.sm.DAO.StudentRepository;
import com.ensa.v2school.sm.DAO.UserRepository;
import com.ensa.v2school.sm.Models.ROLE;
import com.ensa.v2school.sm.Models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.ensa.v2school.sm.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private Label ErrMsg;

    @FXML
    private Button loginBtn;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField usernameField;

    UserRepository userRepository = new UserRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    public void Handlelogin(){
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            ErrMsg.setText("Please fill in all fields");
            ErrMsg.setVisible(true);
            return;
        }
        try{Optional<User> user= userRepository.authenticate(username,password);
            if (user.isPresent()) {
                if (user.get().isAdmin()) {
                    navigateTo("AdminDashboard");
                }

            }
            else {
                ErrMsg.setText("Invalid Credentials, please try again !");
                ErrMsg.setVisible(true);
            }
        }
        catch (SQLException e) {
            ErrMsg.setText("Database error. Try again.");
            ErrMsg.setVisible(true);
            e.printStackTrace();
        }
    }

    public void navigateTo(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ensa/v2school/sm/" + viewName + ".fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
