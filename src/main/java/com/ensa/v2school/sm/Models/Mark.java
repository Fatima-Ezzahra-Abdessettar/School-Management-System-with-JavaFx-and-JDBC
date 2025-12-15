package com.ensa.v2school.sm.Models;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Mark {
    private int id;
    private Student student;
    private Subject subject;
    private float value;

}
