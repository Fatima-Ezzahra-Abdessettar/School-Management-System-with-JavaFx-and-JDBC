package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.Models.Major;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MajorsController implements Initializable {

    @FXML
    public Button editBtn;

    @FXML
    public Button deleteBtn;

    @FXML
    private Button addBtn;

    @FXML
    private TextField searchId;

    @FXML
    private Button searchBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private TableView<Major> TableView;

    @FXML
    private TableColumn<Major, Integer> idCol;

    @FXML
    private TableColumn<Major, String> nameCol;

    @FXML
    private TableColumn<Major, String> descriptionCol;

    @FXML
    private TableColumn<Major, Void> actionsCol;

    MajorRepository majorRepository = new MajorRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Controller initialized!");
        System.out.println("TableView: " + (TableView != null ? "OK" : "NULL"));
        System.out.println("MajorRepository: " + (majorRepository != null ? "OK" : "NULL"));

        // Set up the columns to match Major properties
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("majorName"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Set up the actions column with "View Details" button
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button viewDetailsBtn = new Button("details");

            {
                viewDetailsBtn.setOnAction(event -> {
                    Major major = getTableView().getItems().get(getIndex());
                    handleViewDetails(major);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewDetailsBtn);
                }
            }
        });

        // Load data
        loadTableView();
    }

    private void handleViewDetails(Major major) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/ensa/v2school/sm/major-details.fxml")
            );

            Parent root = loader.load();

            MajorDetailsController controller = loader.getController();
            controller.setMajor(major);

            Stage stage = new Stage();
            stage.setTitle("Major Details - " + major.getMajorName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error loading major details: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Cannot open details", "Error: " + e.getMessage());
        }
    }

    public void handleSearch(){
        String id = searchId.getText();
        if (id == null || id.trim().isEmpty()) {
            showAlert("Error", "Empty Search", "Please enter a major ID!");
            return;
        }
        try{
            Optional<Major> major = majorRepository.get(Integer.parseInt(id));
            if(major.isPresent()){
                TableView.getItems().clear();
                TableView.getItems().add(major.get());
            }
            else {
                showAlert("Error", "Major not found", "Major ID: " + id + " is not found, please make sure you entered a correct id!");
            }
        }catch(NumberFormatException e){
            showAlert("Error", "Invalid ID", "Please enter a valid numeric ID!");
        }catch(SQLException e){
            System.err.println("Error searching major: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not search for major!");
        }
    }

    public void handleCancelSearch(){
        searchId.clear();
        loadTableView();
    }

    private void showAlert(String title, String header, String body) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(body);
        alert.showAndWait();
    }

    public void loadTableView(){
        try{
            List<Major> majors = majorRepository.getAll();
            TableView.getItems().clear();
            if (!majors.isEmpty()) {
                TableView.getItems().addAll(majors);
            }
        }
        catch(SQLException e){
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void handleAdd() {
        try {
            openMajorForm(null);
        } catch (IOException e) {
            System.err.println("Failed to open add form: " + e.getMessage());
        }
    }

    @FXML
    public void handleEdit() {
        Major selected = TableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Select a major first", "");
            return;
        }

        try {
            openMajorForm(selected);
        } catch (IOException e) {
            System.err.println("Failed to open edit form: " + e.getMessage());
        }
    }

    private void openMajorForm(Major major) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/ensa/v2school/sm/Majors-form.fxml")
            );

            Parent root = loader.load();

            MajorsFormController controller = loader.getController();
            controller.setMajor(major);

            Stage stage = new Stage();
            stage.setTitle(major == null ? "Add Major" : "Edit Major");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadTableView();
        } catch (IOException e) {
            System.err.println("Error loading major form: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Cannot open form", "Make sure major-form.fxml exists in resources folder!");
        }
    }

    public void handleDelete(){
        Major selected = TableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select a major first", "");
        }
        else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Major");
            alert.setHeaderText("Delete Major");
            alert.setContentText("Are you sure you want to delete this major?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    majorRepository.delete(selected);
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Major successfully deleted!");
                    successAlert.setContentText("Major successfully deleted!");
                    successAlert.showAndWait();
                    loadTableView();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    showAlert("Database Error", "Error", "Could not delete major!");
                }
            }
        }
    }
}