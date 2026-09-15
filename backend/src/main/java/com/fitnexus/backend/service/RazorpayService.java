package com.fitnexus.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RazorpayService {

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Pattern ORDER_ID = Pattern.compile("\"id\"\\s*:\\s*\"(order_[^\"]+)\"");

    public boolean isConfigured() {
        return keyId != null && !keyId.isBlank() && keySecret != null && !keySecret.isBlank();
    }

    public String getKeyId() {
        return keyId;
    }

    /** Creates a Razorpay order and returns its id. Amount is in the smallest currency unit (paise). */
    public String createOrder(long amountPaise, String currency, String receipt) {
        if (!isConfigured()) {
            throw new IllegalStateException("Razorpay is not configured on the server");
        }
        try {
            String auth = Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
            String body = "{\"amount\":" + amountPaise + ",\"currency\":\"" + currency
                    + "\",\"receipt\":\"" + receipt + "\",\"payment_capture\":1}";
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.razorpay.com/v1/orders"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new RuntimeException("Razorpay order creation failed (" + response.statusCode() + "): " + response.body());
            }
            Matcher m = ORDER_ID.matcher(response.body());
            if (!m.find()) {
                throw new RuntimeException("Razorpay response did not contain an order id");
            }
            return m.group(1);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Could not reach Razorpay: " + e.getMessage(), e);
        }
    }

    /** Verifies the payment signature: HMAC_SHA256(orderId + "|" + paymentId, keySecret) == signature. */
    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (orderId == null || paymentId == null || signature == null) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString().equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
