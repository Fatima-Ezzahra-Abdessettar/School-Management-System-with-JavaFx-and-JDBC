package com.ensa.v2school.sm.Models;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Subject {
    private int id;
    private String name;
    private Major major;
}
