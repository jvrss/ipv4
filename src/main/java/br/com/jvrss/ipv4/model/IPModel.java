package br.com.jvrss.ipv4.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IPModel {

    public String getPublicIPv4() {
        return fetchFromApi("https://api.ipify.org", false);
    }

    public String getPublicIPv6() {
        return fetchFromApi("https://api64.ipify.org", true);
    }

    public String getVpsIP(String vpsArg) {
        return (vpsArg != null && !vpsArg.isBlank())
                ? vpsArg
                : "IP VPS não fornecido como argumento.";
    }

    public List<String> getLocalIPv4Addresses() {
        return getLocalIPAddresses(true);
    }

    public List<String> getLocalIPv6Addresses() {
        return getLocalIPAddresses(false);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String fetchFromApi(String apiUrl, boolean preferIPv6) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (preferIPv6) {
                connection.setRequestProperty("Accept", "application/json; q=1.0, text/plain; q=0.8");
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String ip = in.readLine();
                if (preferIPv6 && ip != null && ip.contains(":")) {
                    return ip;
                } else if (preferIPv6) {
                    return "Erro: IPv6 não detectado. Retornado: " + ip;
                }
                return ip;
            }
        } catch (Exception e) {
            return "Erro ao obter IP: " + e.getMessage();
        }
    }

    private List<String> getLocalIPAddresses(boolean ipv4) {
        List<String> result = new ArrayList<>();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr.isLoopbackAddress()) continue;
                    if (ipv4 && addr instanceof Inet4Address) {
                        result.add(addr.getHostAddress());
                    } else if (!ipv4 && addr instanceof Inet6Address) {
                        String raw = addr.getHostAddress();
                        int zoneIdx = raw.indexOf('%');
                        result.add(zoneIdx >= 0 ? raw.substring(0, zoneIdx) : raw);
                    }
                }
            }
        } catch (Exception e) {
            result.add("Erro ao obter IPs locais: " + e.getMessage());
        }
        return result;
    }
}

