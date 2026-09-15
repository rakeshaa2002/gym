package com.fitnexus.backend.dto;

import lombok.Data;

/**
 * Payload sent by the fingerprint terminal when it matches an enrolled member.
 * Any one identifier is enough; the server resolves the member in this order:
 * fingerprintId -> userId -> email.
 */
@Data
public class DeviceCheckInRequest {
    private String fingerprintId;
    private Long userId;
    private String email;
    private String deviceId;
}
