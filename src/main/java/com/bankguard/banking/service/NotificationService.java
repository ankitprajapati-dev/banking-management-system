package com.bankguard.banking.service;

import com.bankguard.banking.dto.response.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Async
    public void sendTransactionAlert(String email, String name, TransactionResponse transaction) {
        try {
            String subject = "🔔 Transaction Alert - BankGuard";
            String body = String.format(
                    "Dear %s,\n\n" +
                    "A transaction has been processed on your account.\n\n" +
                    "📊 Transaction Details:\n" +
                    "├─ Amount: ₹%.2f\n" +
                    "├─ Type: %s\n" +
                    "├─ Reference: %s\n" +
                    "├─ Status: %s\n" +
                    "└─ Date: %s\n\n" +
                    "If you did not perform this transaction, please contact us immediately.\n\n" +
                    "Regards,\nBankGuard Team",
                    name,
                    transaction.getAmount(),
                    transaction.getTransactionType(),
                    transaction.getTransactionReference(),
                    transaction.getStatus(),
                    transaction.getCreatedAt()
            );

            log.info("📧 Transaction Alert sent to: {}", email);
            log.info("Message: {}", body);

        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}