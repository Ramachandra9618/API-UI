package controller;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import services.RoasterService;
import utils.PropertiesReader;
import services.ControllerConfigService;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
public class TestHelperController extends ControllerConfigService {

    private PropertiesReader propertiesReader;
    private RoasterService roasterService;

    @GetMapping("/api/helper/loadConfig")
    public Map<String, String> getConfig(HttpServletRequest request) {
        Map<String, String> config = new HashMap<>();

        String proto = Optional.ofNullable(request.getHeader("X-Forwarded-Proto"))
                .orElse(request.getScheme());
        String hostHeader = Optional.ofNullable(request.getHeader("X-Forwarded-Host"))
                .orElse(Optional.ofNullable(request.getHeader("Host")).orElse(""));

        if (hostHeader.isEmpty()) {
            int port = request.getServerPort();
            String portPart = (port == 80 || port == 443) ? "" : ":" + port;
            hostHeader = request.getServerName() + portPart;
        }

        config.put("apiBaseUrl", proto + "://" + hostHeader);
        return config;
    }

    @PostMapping("/getShowroomsList")
    public Map<String, Object> getShowroomsList(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        System.out.println(request);
         customerType = request.getOrDefault("customerType", "");
         environment = request.getOrDefault("environment", "");
        // String city = request.getOrDefault("city", "");

        // Validation
        if (customerType.isEmpty() || environment.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Missing required fields: customerType, environment");
            response.put("showrooms", Collections.emptyList());
            return response;
        }

        try {
            initializeTestData();
            roasterService = new RoasterService();
            roasterService.initialize();

            List<Map<String, String>> showroomList = roasterService.getShowroomList();

            response.put("status", "success");
            response.put("customerType", customerType);
            response.put("environment", environment);
            response.put("showrooms", showroomList);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("showrooms", Collections.emptyList());
        }

        return response;
    }
}
