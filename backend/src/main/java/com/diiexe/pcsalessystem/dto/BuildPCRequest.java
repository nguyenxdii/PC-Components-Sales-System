package com.diiexe.pcsalessystem.dto;

import lombok.Data;

@Data
public class BuildPCRequest {
    private Double budget;
    private String usage; // GAMING, OFFICE, WORKING
    private Boolean includeGear;
}
