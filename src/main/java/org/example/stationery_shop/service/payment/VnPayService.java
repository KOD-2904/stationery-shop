package org.example.stationery_shop.service.payment;

import lombok.RequiredArgsConstructor;
import org.example.stationery_shop.entity.order.Order;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VnPayService {
    private final VnPayProperties properties;

    public String createPaymentUrl(Order order, String clientIp) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", properties.getVersion());
        params.put("vnp_Command", properties.getCommand());
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_Amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", properties.getCurrency());
        params.put("vnp_TxnRef", order.getId());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
        params.put("vnp_OrderType", properties.getOrderType());
        params.put("vnp_Locale", properties.getLocale());
        params.put("vnp_ReturnUrl", properties.getReturnUrl());
        params.put("vnp_IpAddr", clientIp == null ? "127.0.0.1" : clientIp);
        params.put("vnp_CreateDate", formatVnPayTime(Instant.now()));
        params.put("vnp_ExpireDate", formatVnPayTime(Instant.now().plusSeconds(15 * 60)));

        String query = buildQuery(params);
        String secureHash = hmacSha512(properties.getHashSecret(), buildHashData(params));
        return properties.getPaymentUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    public boolean verifySignature(Map<String, String> requestParams) {
        String receivedHash = requestParams.get("vnp_SecureHash");
        if (receivedHash == null) {
            return false;
        }
        Map<String, String> params = new TreeMap<>(requestParams);
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String expectedHash = hmacSha512(properties.getHashSecret(), buildHashData(params));
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    private String formatVnPayTime(Instant instant) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return formatter.format(Date.from(instant));
    }

    public String buildQuery(Map<String, String> params) {
        return params.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }
    public String buildHashData(Map<String, String> params) {
        return params.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(
                    new SecretKeySpec(
                            key.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA512"));
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b & 0xff));
            }
            return hash.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot create VNPAY secure hash", exception);
        }
    }
}
