package org.dita.dost.invoker;


import org.apache.tools.ant.BuildEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class PerformanceTrackerTest {

    private PerformanceTracker tracker;

    @Before
    public void before() {
        tracker = new PerformanceTracker();
    }

     @Test
    public void loadCookie_missingCookieFile() {
        // given

        // when
        Optional<MongoCookie> mongoCookie = tracker.loadCookie();

        // then
        assertFalse(mongoCookie.isPresent());
    }

    @Test
    public void fixKeys() {
        // given
        String key1 = "key1.host";
        String key2 = "some-key";
        Map<String, Long> map = new HashMap<>();
        map.put(key1, 1L);
        map.put(key2, 2L);

        // when
        Map<String, Long> result = tracker.fixKeys(map);

        // then
        assertEquals(2, result.size());
        assertTrue(result.containsKey("key1_host"));
        assertTrue(result.containsKey("some-key"));
        assertEquals(Long.valueOf(1), result.get("key1_host"));
        assertEquals(Long.valueOf(2), result.get("some-key"));
    }

    @Test
    public void buildFinished_skip() {
        // given
        tracker = spy(new PerformanceTracker());
        System.setProperty("skipPerformanceTracking","true");

        // when
        tracker.buildFinished(mock(BuildEvent.class));

        // then
        verify(tracker,times(0)).loadCookie();
    }
}