package com.fitnexus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MembershipResponse {
    private String plan;        // plan code: BASIC | PREMIUM | STAFF | ...
    private LocalDate expiry;   // null when no expiry
    private Long daysLeft;      // null when no expiry
    private boolean premium;    // legacy flag: true for paid/premium-style plans
    private boolean applicable; // true only for member (USER) accounts

    private Long planId;            // configured plan id, null if none assigned
    private String planName;        // human-friendly plan name
    private boolean unlimitedAccess;// true = can check in any time
    private String accessStartTime; // "HH:mm" assigned daily window start (null if none/unlimited)
    private String accessEndTime;   // "HH:mm" assigned daily window end
    private int maxSessionMinutes;  // max minutes per visit; 0 = unlimited
}
