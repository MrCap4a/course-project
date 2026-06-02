package ru.denis.Calculator.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Token-bucket rate limiter: 60 req/min per IP.
 * Resets the counter at the start of each 60-second window.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 60;
    private static final long WINDOW_MS = 60_000L;

    private final ConcurrentMap<String, long[]> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            chain.doFilter(request, response);
            return;
        }
        long now = System.currentTimeMillis();
        long[] state = counters.computeIfAbsent(ip, k -> new long[]{now, 0});
        synchronized (state) {
            if (now - state[0] >= WINDOW_MS) {
                state[0] = now;
                state[1] = 0;
            }
            state[1]++;
            if (state[1] > MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too Many Requests\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
