package com.ensa.v2school.sm.Models;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Subject {
    private int id;
    private String name;
    private List<Major> majors = new ArrayList<>();
    @Override
    public String toString() {
        return this.name;
    }

}
