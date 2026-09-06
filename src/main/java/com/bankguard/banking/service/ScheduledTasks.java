package com.bankguard.banking.service;

import com.bankguard.banking.entity.Account;
import com.bankguard.banking.entity.AccountStatus;
import com.bankguard.banking.entity.AccountType;
import com.bankguard.banking.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ScheduledTasks {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
	private final AccountRepository accountRepository;

	public ScheduledTasks(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Scheduled(cron = "0 0 1 * * ?") // Every day at 1:00 AM
	@Transactional
	public void calculateDailyInterest() {
		log.info("Starting daily interest calculation...");

		List<Account> savingsAccounts = accountRepository.findByAccountTypeAndStatus(AccountType.SAVINGS,
				AccountStatus.ACTIVE);

		int count = 0;
		for (Account account : savingsAccounts) {
			// 4% annual interest, calculated daily
			BigDecimal dailyInterest = account.getBalance().multiply(new BigDecimal("0.04"))
					.divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);

			if (dailyInterest.compareTo(BigDecimal.ZERO) > 0) {
				account.setBalance(account.getBalance().add(dailyInterest));
				count++;
			}
		}

		log.info("Interest calculation completed. Updated {} accounts.", count);
	}

	@Scheduled(cron = "0 0 2 1 * ?") // 1st of every month at 2:00 AM
	public void generateMonthlyReports() {
		log.info("Generating monthly reports...");
		// Generate reports for all customers
		// Could send emails with monthly statements
	}
}