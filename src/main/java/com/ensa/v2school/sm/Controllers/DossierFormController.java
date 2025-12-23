// DossierFormController.java
package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.DossierAdministratifRepository;
import com.ensa.v2school.sm.Models.DossierAdministratif;
import com.ensa.v2school.sm.Models.Student;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class DossierFormController {

    @FXML private TextField numeroInscriptionField;
    @FXML private DatePicker dateCreationPicker;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private Student student;
    private DossierAdministratif dossier;
    private final DossierAdministratifRepository repo = new DossierAdministratifRepository();

    public void setStudent(Student student) {
        this.student = student;
        loadOrInit();
    }

    private void loadOrInit() {
        try {
            Optional<DossierAdministratif> existing = repo.findByStudentId(student.getId());
            if (existing.isPresent()) {
                dossier = existing.get();
                numeroInscriptionField.setText(dossier.getNumeroInscription());
                dateCreationPicker.setValue(dossier.getDateCreation());
            } else {
                dossier = new DossierAdministratif();
                dossier.setEleveId(student.getId());
                dossier.setDateCreation(LocalDate.now());
                dossier.setNumeroInscription(repo.generateNumeroInscription());
                numeroInscriptionField.setText(dossier.getNumeroInscription());
                dateCreationPicker.setValue(dossier.getDateCreation());
                dateCreationPicker.setDisable(true);
            }
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        try {
            if (dossier.getId() == 0) {
                repo.create(dossier);
            } else {
                repo.update(dossier);
            }
            close();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
