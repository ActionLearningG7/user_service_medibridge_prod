package com.medibridge.user_service_medibridge.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Set;

/**
 * Enterprise-grade credential generator for healthcare system.
 * 
 * Security: Uses cryptographically secure random number generation
 * Compliance: Follows NIST password guidelines
 * Audit: All generation events are logged (credentials are NOT logged)
 */
@Component
@Slf4j
public class CredentialGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Password character sets
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    // Username character set (alphanumeric only)
    private static final String ALPHANUMERIC = UPPERCASE + LOWERCASE + DIGITS;

    // Configuration
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 16;
    private static final int USERNAME_SUFFIX_LENGTH = 4;
    private static final int MAX_USERNAME_GENERATION_ATTEMPTS = 10;

    /**
     * Generate a unique username from first and last name.
     * Format: firstname.lastname + random alphanumeric suffix
     * 
     * @param firstName         User's first name
     * @param lastName          User's last name
     * @param existingUsernames Set of existing usernames to avoid collisions
     * @return Generated unique username
     * @throws IllegalStateException if unable to generate unique username
     */
    public String generateUsername(String firstName, String lastName, Set<String> existingUsernames) {
        log.info("Generating username for: {} {}", firstName, lastName);

        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("First name and last name are required for username generation");
        }

        // Normalize names: lowercase, remove special characters, trim
        String normalizedFirst = normalizeForUsername(firstName);
        String normalizedLast = normalizeForUsername(lastName);

        // Base username: firstname.lastname
        String baseUsername = normalizedFirst + "." + normalizedLast;

        // Try to generate unique username with random suffix
        for (int attempt = 0; attempt < MAX_USERNAME_GENERATION_ATTEMPTS; attempt++) {
            String suffix = generateAlphanumericSuffix(USERNAME_SUFFIX_LENGTH);
            String candidateUsername = baseUsername + suffix;

            if (existingUsernames == null || !existingUsernames.contains(candidateUsername)) {
                log.info("Username generated successfully (attempt {})", attempt + 1);
                return candidateUsername;
            }
        }

        throw new IllegalStateException(
                "Failed to generate unique username after " + MAX_USERNAME_GENERATION_ATTEMPTS + " attempts");
    }

    /**
     * Generate a cryptographically secure temporary password.
     * 
     * Requirements:
     * - 12-16 characters (random length for additional security)
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     * - Cryptographically secure random generation
     * 
     * @return Generated temporary password
     */
    public String generateTemporaryPassword() {
        log.info("Generating temporary password");

        // Random length between min and max for additional unpredictability
        int length = MIN_PASSWORD_LENGTH + SECURE_RANDOM.nextInt(MAX_PASSWORD_LENGTH - MIN_PASSWORD_LENGTH + 1);

        StringBuilder password = new StringBuilder(length);

        // Ensure at least one character from each required category
        password.append(getRandomChar(UPPERCASE));
        password.append(getRandomChar(LOWERCASE));
        password.append(getRandomChar(DIGITS));
        password.append(getRandomChar(SPECIAL_CHARS));

        // Fill remaining positions with random characters from all categories
        String allChars = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
        for (int i = 4; i < length; i++) {
            password.append(getRandomChar(allChars));
        }

        // Shuffle the password to avoid predictable patterns
        String shuffledPassword = shuffleString(password.toString());

        log.info("Temporary password generated successfully (length: {})", length);
        // SECURITY: Never log the actual password

        return shuffledPassword;
    }

    /**
     * Normalize a name component for username generation.
     * Converts to lowercase, removes non-alphanumeric characters.
     * 
     * @param name Name component to normalize
     * @return Normalized name
     */
    private String normalizeForUsername(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .trim();
    }

    /**
     * Generate a random alphanumeric suffix.
     * 
     * @param length Length of suffix
     * @return Random alphanumeric string
     */
    private String generateAlphanumericSuffix(int length) {
        StringBuilder suffix = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            suffix.append(getRandomChar(ALPHANUMERIC));
        }
        return suffix.toString();
    }

    /**
     * Get a random character from the given character set.
     * Uses cryptographically secure random number generator.
     * 
     * @param charSet Character set to choose from
     * @return Random character
     */
    private char getRandomChar(String charSet) {
        int index = SECURE_RANDOM.nextInt(charSet.length());
        return charSet.charAt(index);
    }

    /**
     * Shuffle a string using Fisher-Yates algorithm with secure random.
     * 
     * @param input String to shuffle
     * @return Shuffled string
     */
    private String shuffleString(String input) {
        char[] characters = input.toCharArray();

        for (int i = characters.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            // Swap characters[i] and characters[j]
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }

        return new String(characters);
    }

    /**
     * Validate password strength (for testing/verification purposes).
     * 
     * @param password Password to validate
     * @return true if password meets all requirements
     */
    public boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> SPECIAL_CHARS.indexOf(ch) >= 0);

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
}
