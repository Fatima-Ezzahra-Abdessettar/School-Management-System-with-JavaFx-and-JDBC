package com.ensa.v2school.sm.Models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Major {
    private int id;
    private String majorName;
    @Override
    public String toString() {
        return majorName;
    }
}
