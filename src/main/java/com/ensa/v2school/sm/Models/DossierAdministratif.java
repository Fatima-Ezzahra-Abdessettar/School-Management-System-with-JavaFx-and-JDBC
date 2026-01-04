package com.ensa.v2school.sm.Models;

import lombok.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class DossierAdministratif {
    private int id;
    private String numeroInscription;
    private LocalDate dateCreation;
    private String eleveId; // Foreign key to Student
}