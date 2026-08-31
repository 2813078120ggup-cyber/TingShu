package com.atguigu.tingshu.order.helper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
class SignHelperTest {

    private static final String PROPERTY = "tingshu.order.sign-key";
    private String originalKey;

    @BeforeEach
    void rememberConfiguration() {
        originalKey = System.getProperty(PROPERTY);
    }

    @AfterEach
    void restoreConfiguration() {
        if (originalKey == null) {
            System.clearProperty(PROPERTY);
        } else {
            System.setProperty(PROPERTY, originalKey);
        }
    }

    @Test
    void signsWithExternalKeyWithoutLoggingSecret(CapturedOutput output) {
        System.setProperty(PROPERTY, "unit-test-sign-key");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", 123L);
        parameters.put("amount", 42);

        assertEquals("1f1e0d91ebd5158da65266f7628cda4b", SignHelper.getSign(parameters));
        assertFalse(output.getAll().contains("unit-test-sign-key"));
        assertFalse(output.getAll().contains("42|123|"));
    }

    @Test
    void refusesBlankKeyRatherThanUsingAnEmbeddedDefault() {
        System.setProperty(PROPERTY, " ");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> SignHelper.getSign(new HashMap<>()));
        assertTrue(error.getMessage().contains("ORDER_SIGN_KEY"));
    }
}
