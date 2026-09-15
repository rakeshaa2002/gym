package com.fitnexus.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFixer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void fixCheckConstraint() {
        try {
            // Drop the check constraint that prevents CORPORATE_HR role from being saved
            jdbcTemplate.execute("ALTER TABLE user_accounts DROP CONSTRAINT IF EXISTS user_accounts_role_check;");
            System.out.println("✅ Database check constraint 'user_accounts_role_check' successfully dropped.");
        } catch (Exception e) {
            System.err.println("⚠️ Failed to drop check constraint: " + e.getMessage());
        }
    }
}
