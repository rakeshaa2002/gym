package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response for GET /api/calls/ice-servers. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IceServerResponse {
    private String[] urls;
    private String username;
    private String credential;
}
