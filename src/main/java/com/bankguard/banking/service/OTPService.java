package com.bankguard.banking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

	private static final Logger log = LoggerFactory.getLogger(OTPService.class);
	private static final int OTP_EXPIRY_MINUTES = 5;
	private final Map<String, OTPData> otpCache = new ConcurrentHashMap<>();

	public String generateOTP(String email) {
		String otp = String.format("%06d", new Random().nextInt(999999));
		otpCache.put(email, new OTPData(otp, LocalDateTime.now()));
		log.info("OTP generated for {}: {}", email, otp);
		return otp;
	}

	public boolean verifyOTP(String email, String otp) {
		OTPData data = otpCache.get(email);
		if (data == null) {
			log.warn("No OTP found for {}", email);
			return false;
		}

		if (data.getTimestamp().plusMinutes(OTP_EXPIRY_MINUTES).isBefore(LocalDateTime.now())) {
			otpCache.remove(email);
			log.warn("OTP expired for {}", email);
			return false;
		}

		boolean valid = data.getOtp().equals(otp);
		if (valid) {
			otpCache.remove(email);
			log.info("OTP verified for {}", email);
		}
		return valid;
	}

	private static class OTPData {
		private final String otp;
		private final LocalDateTime timestamp;

		public OTPData(String otp, LocalDateTime timestamp) {
			this.otp = otp;
			this.timestamp = timestamp;
		}

		public String getOtp() {
			return otp;
		}

		public LocalDateTime getTimestamp() {
			return timestamp;
		}
	}
}