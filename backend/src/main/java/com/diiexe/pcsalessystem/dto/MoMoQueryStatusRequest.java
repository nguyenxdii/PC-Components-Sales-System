package com.diiexe.pcsalessystem.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoMoQueryStatusRequest {
    private String partnerCode;
    private String requestId;
    private String orderId;
    private String signature;
    private String lang;
}
