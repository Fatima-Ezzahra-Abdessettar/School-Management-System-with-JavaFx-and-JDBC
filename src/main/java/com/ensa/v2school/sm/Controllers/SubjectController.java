package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.DAO.SubjectRepository;
import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Subject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SubjectController implements Initializable {

    @FXML
    public Button editBtn;

    @FXML
    public Button deleteBtn;
    public Button addBtn;

    @FXML
    private TextField searchId;

    @FXML
    private TableView<Subject> TableView;

    @FXML
    private TableColumn<Subject, Integer> idCol;

    @FXML
    private TableColumn<Subject, String> nameCol;

    @FXML
    private TableColumn<Subject, String> majorCol;

    @FXML
    private ComboBox<Major> filterMajorBtn;

    SubjectRepository subjectRepository = new SubjectRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        majorCol.setCellValueFactory(cellData -> {
            Major major = cellData.getValue().getMajor();
            return new javafx.beans.property.SimpleStringProperty(major != null ? major.getMajorName() : "");
        });

        filterMajorBtn.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Major item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Filter by major");
                } else {
                    setText(item.getMajorName());
                }
            }
        });

        MajorRepository majorRepository = new MajorRepository();
        try {
            List<Major> majors = majorRepository.getAll();
            filterMajorBtn.getItems().addAll(majors);
        } catch (SQLException e) {
            System.err.println("Error loading majors: " + e.getMessage());
        }

        loadTableView();
    }

    public void handleSearch() {
        String id = searchId.getText();
        if (id == null || id.trim().isEmpty()) {
            showAlert("Error", "Empty Search", "Please enter a subject ID!");
            return;
        }
        try {
            Optional<Subject> subject = subjectRepository.get(Integer.parseInt(id));
            if (subject.isPresent()) {
                TableView.getItems().clear();
                TableView.getItems().add(subject.get());
            } else {
                showAlert("Error", "Subject not found", "Subject ID: " + id + " is not found!");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid ID", "Please enter a valid numeric ID!");
        } catch (SQLException e) {
            System.err.println("Error searching subject: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not search for subject!");
        }
    }

    public void handleCancelSearch() {
        searchId.clear();
        filterMajorBtn.getSelectionModel().clearSelection();
        filterMajorBtn.setValue(null);
        loadTableView();
    }

    public void loadMajorTableView() {
        Major major = filterMajorBtn.getSelectionModel().getSelectedItem();
        if (major == null) {
            return;
        }
        try {
            List<Subject> subjects = subjectRepository.findByMajor(major.getId());
            TableView.getItems().clear();
            TableView.getItems().addAll(subjects);
            if (subjects.isEmpty()) {
                showAlert("Info", "No subjects found", "No subjects found for this major!");
            }
        } catch (SQLException e) {
            System.err.println("Error filtering subjects by major: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not filter subjects!");
        }
    }

    private void showAlert(String title, String header, String body) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(body);
        alert.showAndWait();
    }

    public void loadTableView() {
        try {
            List<Subject> subjects = subjectRepository.getAll();
            TableView.getItems().clear();
            if (!subjects.isEmpty()) {
                TableView.getItems().addAll(subjects);
            }
        } catch (SQLException e) {
            System.err.println("Error loading subjects: " + e.getMessage());
        }
    }

    @FXML
    public void handleAdd() {
        try {
            openSubjectForm(null);
        } catch (IOException e) {
            System.err.println("Failed to open add form: " + e.getMessage());
        }
    }

    @FXML
    public void handleEdit() {
        Subject selected = TableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Select a subject first", "");
            return;
        }

        try {
            openSubjectForm(selected);
        } catch (IOException e) {
            System.err.println("Failed to open edit form: " + e.getMessage());
        }
    }

    private void openSubjectForm(Subject subject) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/ensa/v2school/sm/SubjectForm.fxml")
            );

            Parent root = loader.load();

            SubjectFormController controller = loader.getController();
            controller.setSubject(subject);

            Stage stage = new Stage();
            stage.setTitle(subject == null ? "Add Subject" : "Edit Subject");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadTableView();
        } catch (IOException e) {
            System.err.println("Error loading subject form: " + e.getMessage());
            showAlert("Error", "Cannot open form", "Make sure subject-form.fxml exists!");
        }
    }

    public void handleDelete() {
        Subject selected = TableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select a subject first", "");
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Subject");
            alert.setHeaderText("Delete Subject");
            alert.setContentText("Are you sure you want to delete this subject?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    subjectRepository.delete(selected);
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Subject successfully deleted!");
                    successAlert.showAndWait();
                    loadTableView();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    showAlert("Database Error", "Error", "Could not delete subject!");
                }
            }
        }
    }
}