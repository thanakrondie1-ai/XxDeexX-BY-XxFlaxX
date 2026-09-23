package com.example

import com.example.data.model.GitConfigValidator
import org.junit.Assert.*
import org.junit.Test

class GitConfigValidatorTest {

    @Test
    fun usernameValidation_emptyOrBlank_returnsError() {
        assertNotNull(GitConfigValidator.validateUsername(""))
        assertNotNull(GitConfigValidator.validateUsername("   "))
        assertNotNull(GitConfigValidator.validateUsername("\t\n"))
        assertEquals("Username cannot be empty", GitConfigValidator.validateUsername(""))
        assertEquals("Username cannot be empty", GitConfigValidator.validateUsername("  "))
    }

    @Test
    fun usernameValidation_validUsername_returnsNull() {
        assertNull(GitConfigValidator.validateUsername("octocat"))
        assertNull(GitConfigValidator.validateUsername("ThanakronDie1"))
        assertNull(GitConfigValidator.validateUsername("  john_doe  "))
    }

    @Test
    fun emailValidation_emptyOrBlank_returnsError() {
        assertNotNull(GitConfigValidator.validateEmail(""))
        assertNotNull(GitConfigValidator.validateEmail("   "))
        assertEquals("Email cannot be empty", GitConfigValidator.validateEmail(""))
        assertEquals("Email cannot be empty", GitConfigValidator.validateEmail("  "))
    }

    @Test
    fun emailValidation_invalidFormat_returnsFormatError() {
        val invalidEmails = listOf(
            "not-an-email",
            "user@",
            "@domain.com",
            "user@domain",
            "user@.com",
            "user@domain.",
            "user@@domain.com",
            "user space@domain.com"
        )
        for (invalid in invalidEmails) {
            val error = GitConfigValidator.validateEmail(invalid)
            assertNotNull("Expected error for email: $invalid", error)
            assertEquals("Please enter a valid email address (e.g. thanakrondie1@gmail.com)", error)
        }
    }

    @Test
    fun emailValidation_validFormat_returnsNull() {
        val validEmails = listOf(
            "ThanakronDie1@gmail.com",
            "octocat@github.com",
            "dev.engineer+tag@example.org",
            "user123@subdomain.domain.co"
        )
        for (valid in validEmails) {
            assertNull("Expected null error for valid email: $valid", GitConfigValidator.validateEmail(valid))
        }
    }

    @Test
    fun isValid_bothValid_returnsTrue() {
        assertTrue(GitConfigValidator.isValid("octocat", "octocat@github.com"))
        assertTrue(GitConfigValidator.isValid("Thanakron", "ThanakronDie1@gmail.com"))
    }

    @Test
    fun isValid_eitherInvalid_returnsFalse() {
        // Empty username
        assertFalse(GitConfigValidator.isValid("", "valid@example.com"))
        assertFalse(GitConfigValidator.isValid("   ", "valid@example.com"))

        // Invalid email
        assertFalse(GitConfigValidator.isValid("octocat", ""))
        assertFalse(GitConfigValidator.isValid("octocat", "invalid-email"))
        assertFalse(GitConfigValidator.isValid("octocat", "user@domain"))

        // Both invalid
        assertFalse(GitConfigValidator.isValid("", "invalid"))
    }
}
