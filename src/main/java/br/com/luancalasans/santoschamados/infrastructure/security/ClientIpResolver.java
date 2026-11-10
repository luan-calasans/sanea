package br.com.luancalasans.santoschamados.infrastructure.security;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

@Component
public class ClientIpResolver {

    private final List<String> trustedProxies;

    public ClientIpResolver(AppProperties appProperties) {
        this.trustedProxies = Arrays.stream(appProperties.getTrustedProxies().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (!isTrustedProxy(remoteAddr)) {
            return remoteAddr;
        }

        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isBlank()) {
            return cfConnectingIp.trim();
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return remoteAddr;
    }

    private boolean isTrustedProxy(String ip) {
        for (String trusted : trustedProxies) {
            if (matchesCidrOrExact(ip, trusted)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesCidrOrExact(String ip, String trusted) {
        if (!trusted.contains("/")) {
            return trusted.equals(ip);
        }
        try {
            String[] parts = trusted.split("/");
            InetAddress target = InetAddress.getByName(ip);
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            byte[] targetBytes = target.getAddress();
            byte[] networkBytes = network.getAddress();
            if (targetBytes.length != networkBytes.length) {
                return false;
            }
            int fullBytes = prefix / 8;
            int remBits = prefix % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (targetBytes[i] != networkBytes[i]) {
                    return false;
                }
            }
            if (remBits == 0) {
                return true;
            }
            int mask = (~0) << (8 - remBits);
            return (targetBytes[fullBytes] & mask) == (networkBytes[fullBytes] & mask);
        } catch (UnknownHostException | NumberFormatException ex) {
            return false;
        }
    }
}
