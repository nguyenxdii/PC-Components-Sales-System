package com.diiexe.pcsalessystem.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MoMoQueryStatusResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private String message;
    private Integer resultCode;
    private String payType;
    private Long transId;
    private String extraData;
}
