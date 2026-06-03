package ru.denis.Calculator.Filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    // ── localhost bypass ──────────────────────────────────────────────────────

    @Test
    void localhostIPv4_bypassesRateLimit() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest req = request("127.0.0.1");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        filter.doFilter(req, resp, chain);

        verify(chain).doFilter(req, resp);
        assertThat(resp.getStatus()).isNotEqualTo(429);
    }

    @Test
    void localhostIPv6_bypassesRateLimit() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest req = request("0:0:0:0:0:0:0:1");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        filter.doFilter(req, resp, chain);

        verify(chain).doFilter(req, resp);
        assertThat(resp.getStatus()).isNotEqualTo(429);
    }

    @Test
    void localhostIPv4_manyRequests_allPass() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 200; i++) {
            MockHttpServletRequest req = request("127.0.0.1");
            MockHttpServletResponse resp = new MockHttpServletResponse();
            filter.doFilter(req, resp, chain);
            assertThat(resp.getStatus()).isNotEqualTo(429);
        }
        verify(chain, times(200)).doFilter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    // ── rate limiting ─────────────────────────────────────────────────────────

    @Test
    void externalIp_under60Requests_allPass() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        String ip = "10.0.0.1";

        for (int i = 0; i < 60; i++) {
            MockHttpServletRequest req = request(ip);
            MockHttpServletResponse resp = new MockHttpServletResponse();
            filter.doFilter(req, resp, chain);
            assertThat(resp.getStatus()).as("request %d should pass", i + 1).isNotEqualTo(429);
        }
    }

    @Test
    void externalIp_61stRequest_gets429() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        String ip = "10.0.0.2";

        for (int i = 0; i < 60; i++) {
            filter.doFilter(request(ip), new MockHttpServletResponse(), chain);
        }

        MockHttpServletRequest req = request(ip);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, chain);

        assertThat(resp.getStatus()).isEqualTo(429);
        assertThat(resp.getContentAsString()).contains("Too Many Requests");
    }

    @Test
    void differentIps_haveIndependentCounters() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        String ip1 = "10.0.1.1";
        String ip2 = "10.0.1.2";

        for (int i = 0; i < 60; i++) {
            filter.doFilter(request(ip1), new MockHttpServletResponse(), chain);
        }

        MockHttpServletRequest req = request(ip2);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, chain);

        assertThat(resp.getStatus()).isNotEqualTo(429);
    }

    @Test
    void rateLimited_response_hasJsonContentType() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        String ip = "10.0.0.3";

        for (int i = 0; i < 60; i++) {
            filter.doFilter(request(ip), new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(request(ip), resp, chain);

        assertThat(resp.getContentType()).contains("application/json");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MockHttpServletRequest request(String remoteAddr) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr(remoteAddr);
        return req;
    }
}
