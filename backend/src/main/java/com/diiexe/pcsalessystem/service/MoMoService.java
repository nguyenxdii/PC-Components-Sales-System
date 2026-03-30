package com.diiexe.pcsalessystem.service;

import com.diiexe.pcsalessystem.dto.MoMoPaymentRequest;
import com.diiexe.pcsalessystem.dto.MoMoPaymentResponse;
import com.diiexe.pcsalessystem.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.UUID;

@Service
public class MoMoService {

    @Value("${app.momo.partner-code}")
    private String partnerCode;

    @Value("${app.momo.access-key}")
    private String accessKey;

    @Value("${app.momo.secret-key}")
    private String secretKey;

    @Value("${app.momo.endpoint}")
    private String endpoint;

    @Value("${app.momo.redirect-url}")
    private String redirectUrl;

    @Value("${app.momo.ipn-url}")
    private String ipnUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public MoMoPaymentResponse createPayment(Order order) throws Exception {
        String requestId = UUID.randomUUID().toString();
        String orderId = order.getOrderCode();
        String amount = String.valueOf(order.getFinalPrice().longValue());
        String orderInfo = "Thanh toán đơn hàng " + orderId;
        String requestType = "captureWallet";
        String extraData = "";

        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = hmacSha256(rawSignature, secretKey);

        MoMoPaymentRequest request = MoMoPaymentRequest.builder()
                .partnerCode(partnerCode)
                .partnerName("PC Components Sales System")
                .storeId("PC_SHOP")
                .requestId(requestId)
                .amount(amount)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(redirectUrl)
                .ipnUrl(ipnUrl)
                .requestType(requestType)
                .extraData(extraData)
                .signature(signature)
                .lang("vi")
                .build();

        return restTemplate.postForObject(endpoint, request, MoMoPaymentResponse.class);
    }

    public com.diiexe.pcsalessystem.dto.MoMoQueryStatusResponse queryPaymentStatus(String orderCode) throws Exception {
        String requestId = UUID.randomUUID().toString();
        
        String rawSignature = "accessKey=" + accessKey +
                "&orderId=" + orderCode +
                "&partnerCode=" + partnerCode +
                "&requestId=" + requestId;

        String signature = hmacSha256(rawSignature, secretKey);

        com.diiexe.pcsalessystem.dto.MoMoQueryStatusRequest request = com.diiexe.pcsalessystem.dto.MoMoQueryStatusRequest.builder()
                .partnerCode(partnerCode)
                .requestId(requestId)
                .orderId(orderCode)
                .signature(signature)
                .lang("vi")
                .build();

        String queryEndpoint = endpoint.replace("/create", "/query");
        if (queryEndpoint.equals(endpoint)) {
             queryEndpoint = "https://test-payment.momo.vn/v2/gateway/api/query";
        }

        return restTemplate.postForObject(queryEndpoint, request, com.diiexe.pcsalessystem.dto.MoMoQueryStatusResponse.class);
    }

    private String hmacSha256(String data, String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return toHexString(sha256_HMAC.doFinal(dataBytes));
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        try (Formatter formatter = new Formatter(sb)) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
        }
        return sb.toString();
    }
}
