package com.atguigu.tingshu.live.util;

import com.atguigu.tingshu.vo.live.TencentLiveAddressVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiveAutoAddressUtilTest {

    private static final String PROPERTY = "tingshu.live.push-key";
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
    void generatesUrlsUsingTheExternalKey() {
        System.setProperty(PROPERTY, "unit-test-push-key");

        TencentLiveAddressVo urls = LiveAutoAddressUtil.getAddressUrl("review-stream", 1700000000L);
        String signedPath = "/live/review-stream?txSecret=2e2bfe6b186a4613beecbca9e635a7b6&txTime=6553F100";
        assertTrue(urls.getPushWebRtcUrl().endsWith(signedPath));
        assertTrue(urls.getPullWebRtcUrl().endsWith(signedPath));
        assertFalse(urls.getPushWebRtcUrl().contains("unit-test-push-key"));
    }

    @Test
    void refusesBlankKeyRatherThanGeneratingAnUnsignedUrl() {
        System.setProperty(PROPERTY, "");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> LiveAutoAddressUtil.getAddressUrl("review-stream", 1700000000L));
        assertTrue(error.getMessage().contains("LIVE_PUSH_KEY"));
    }
}
