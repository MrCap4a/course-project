package ru.denis.Calculator.Filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    RateLimitFilter filter;

    @BeforeEach
    void setUp() { filter = new RateLimitFilter(); }

    @Test
    void localhostIPv4_bypassesRateLimit() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest req = req("127.0.0.1");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, chain);
        verify(chain).doFilter(req, resp);
        assertThat(resp.getStatus()).isNotEqualTo(429);
    }

    @Test
    void localhostIPv6_bypassesRateLimit() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest req = req("0:0:0:0:0:0:0:1");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req, resp, chain);
        verify(chain).doFilter(req, resp);
        assertThat(resp.getStatus()).isNotEqualTo(429);
    }

    @Test
    void localhostIPv4_manyRequests_allPass() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 200; i++) {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            filter.doFilter(req("127.0.0.1"), resp, chain);
            assertThat(resp.getStatus()).isNotEqualTo(429);
        }
        verify(chain, times(200)).doFilter(any(), any());
    }

    @Test
    void externalIp_under60Requests_allPass() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 60; i++) {
            MockHttpServletResponse resp = new MockHttpServletResponse();
            filter.doFilter(req("10.0.0.1"), resp, chain);
            assertThat(resp.getStatus()).as("request %d should pass", i + 1).isNotEqualTo(429);
        }
    }

    @Test
    void externalIp_61stRequest_gets429() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 60; i++) {
            filter.doFilter(req("10.0.0.2"), new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req("10.0.0.2"), resp, chain);
        assertThat(resp.getStatus()).isEqualTo(429);
        assertThat(resp.getContentAsString()).contains("Too Many Requests");
    }

    @Test
    void differentIps_haveIndependentCounters() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 60; i++) {
            filter.doFilter(req("10.0.1.1"), new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req("10.0.1.2"), resp, chain);
        assertThat(resp.getStatus()).isNotEqualTo(429);
    }

    @Test
    void rateLimited_response_hasJsonContentType() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        for (int i = 0; i < 60; i++) {
            filter.doFilter(req("10.0.0.3"), new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filter.doFilter(req("10.0.0.3"), resp, chain);
        assertThat(resp.getContentType()).contains("application/json");
    }

    private MockHttpServletRequest req(String ip) {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRemoteAddr(ip);
        return r;
    }
}
