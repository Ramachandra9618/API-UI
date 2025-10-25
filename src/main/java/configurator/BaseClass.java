package configurator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BaseClass extends ApiService {

    public static final Logger log = LogManager.getLogger(BaseClass.class);
    public static final HashMap<String, Object> testData = new HashMap<>();

    public static String customerType;
    private static String userSelectedCityProperty;


    public static void initializeTestData() {
        testData.clear();

        // Load user configuration properties
        addToTestDataFromProperties("configuration/userConfigurations.properties", testData);

        // Read customerType and city
        customerType = (String) testData.get("customerType");
        userSelectedCityProperty = String.valueOf(testData.get("userSelectedCityProperty"));

        if (customerType == null || userSelectedCityProperty == null) {
            log.error("❌ customerType or userSelectedCityProperty is missing in userConfigurations.properties");
            return;
        }

        // Load standard, customer-specific, and city configurations
        loadStandardConfig(customerType);
        loadCustomerConfig(customerType);
        loadCityConfig(userSelectedCityProperty);

        log.info("✅ Test data initialized. CustomerType: {}, CityCode: {}", customerType, userSelectedCityProperty);
    }

    private static void loadStandardConfig(String customerType) {
        String configFile = switch (customerType) {
            case "HL", "LUXE", "HFN" -> "configuration/HL_standardConfigurations.yml";
            case "DC" -> "configuration/DC_standardConfigurations.yml";
            default -> null;
        };
        if (configFile != null) {
            addToTestDataFromYml(configFile, testData);
        } else {
            log.warn("Unknown customer type: {}", customerType);
        }
    }

    private static void loadCustomerConfig(String customerType) {
        String configFile = switch (customerType) {
            case "HL", "LUXE" -> "configuration/HL_config.yml";
            case "HFN" -> "configuration/HFN_config.yml";
            case "DC" -> "configuration/DC_config.yml";
            default -> null;
        };
        if (configFile != null) {
            addToTestDataFromYml(configFile, testData);
        } else {
            log.warn("Unknown customer type: {}", customerType);
        }
    }

    private static void loadCityConfig(String citycode) {
        String cityFile = switch (citycode) {
            case "1" -> "configuration/CitiesData/homelane/1-Bengaluru.properties";
            case "2" -> "configuration/CitiesData/homelane/2-Chennai.properties";
            case "3" -> "configuration/CitiesData/homelane/3-Mumbai.properties";
            case "4" -> "configuration/CitiesData/homelane/4-Kolkata.properties";
            case "5" -> "configuration/CitiesData/homelane/5-Kochi.properties";
            case "6" -> "configuration/CitiesData/homelane/6-Visakhapatnam.properties";
            case "7" -> "configuration/CitiesData/homelane/7-Delhi.properties";
            case "8" -> "configuration/CitiesData/homelane/8-Hyderabad.properties";
            case "9" -> "configuration/CitiesData/homelane/9-Gurgaon.properties";
            case "10" -> "configuration/CitiesData/homelane/10-Pune.properties";
            case "11" -> "configuration/CitiesData/homelane/11-Thane.properties";
            case "12" -> "configuration/CitiesData/homelane/12-Lucknow.properties";
            case "13" -> "configuration/CitiesData/homelane/13-Mangalore.properties";
            case "14" -> "configuration/CitiesData/homelane/14-Mysore.properties";
            case "15" -> "configuration/CitiesData/homelane/15-Patna.properties";
            case "16" -> "configuration/CitiesData/franchise/16-Thirupathi.properties";
            case "17" -> "configuration/CitiesData/franchise/17-Guwahati.properties";
            case "18" -> "configuration/CitiesData/franchise/18-Vijayawada.properties";
            case "19" -> "configuration/CitiesData/franchise/19-Nizamabad.properties";
            case "20" -> "configuration/CitiesData/franchise/20-Shivamogga.properties";
            case "21" -> "configuration/CitiesData/franchise/21-Siliguri.properties";
            case "22" -> "configuration/CitiesData/franchise/22-Trivendrum.properties";
            case "23" -> "configuration/CitiesData/franchise/23-Warangal.properties";
            case "24" -> "configuration/CitiesData/franchise/24-Karimnagar.properties";
            case "25" -> "configuration/CitiesData/franchise/25-Jamshedpur.properties";
            case "26" -> "configuration/CitiesData/homelane/26-Noida.properties";
            case "27" -> "configuration/CitiesData/homelane/27-Coimbatore.properties";
            case "28" -> "configuration/CitiesData/homelane/28-Bhubaneswar.properties";
            case "29" -> "configuration/CitiesData/homelane/29-Salem.properties";
            case "30" -> "configuration/CitiesData/homelane/30-Nagpur.properties";
            case "31" -> "configuration/CitiesData/homelane/31-Surat.properties";
            case "32" -> "configuration/CitiesData/homelane/32-Ranchi.properties";
            case "33" -> "configuration/CitiesData/homelane/33-Ghaziabad.properties";
            case "34" -> "configuration/CitiesData/homelane/34-Nashik.properties";
            case "35" -> "configuration/CitiesData/homelane/35-Madurai.properties";
            case "36" -> "configuration/CitiesData/homelane/36-Tiruchirappalli.properties";
            case "37" -> "configuration/CitiesData/homelane/37-Jaipur.properties";
            case "38" -> "configuration/CitiesData/homelane/38-Ahmedabad.properties";
            default -> null;
        };
        if (cityFile != null) {
            addToTestDataFromProperties(cityFile, testData);
        } else {
            log.warn("Unknown city code: {}", citycode);
        }
    }

    public static void addToTestDataFromYml(String fileName, Map<String, Object> testData) {
        Yaml yaml = new Yaml();
        InputStream inputStream = null;

        try {
            Path filePath = locateFile(fileName);
            if (filePath == null) {
                log.error("❌ YAML file not found: {}", fileName);
                return;
            }

            inputStream = Files.newInputStream(filePath);
            System.out.println("📄 Loading YAML from: " + filePath.toAbsolutePath());

            Map<String, Object> yamlData = yaml.load(inputStream);
            if (yamlData != null) {
                testData.putAll(yamlData);
                log.info("✅ Loaded YAML test data from {}", filePath);
            } else {
                log.warn("⚠️ YAML file {} was empty or invalid", filePath);
            }

        } catch (IOException e) {
            log.error("❌ Failed to read YAML {}: {}", fileName, e.getMessage(), e);
        } finally {
            if (inputStream != null) try { inputStream.close(); } catch (IOException ignored) {}
        }
    }

    public synchronized static void addToTestDataFromProperties(String fileName, Map<String, Object> testData) {
        Properties prop = new Properties();
        InputStream inputStream = null;

        try {
            Path filePath = locateFile(fileName);
            if (filePath == null) {
                log.error("❌ Properties file not found: {}", fileName);
                return;
            }

            inputStream = Files.newInputStream(filePath);
            System.out.println("📄 Loading properties from: " + filePath.toAbsolutePath());

            prop.load(inputStream);
            for (String name : prop.stringPropertyNames()) {
                testData.put(name, prop.getProperty(name));
            }

            log.info("✅ Properties loaded successfully from {}", filePath);

        } catch (IOException e) {
            log.error("❌ Failed to read properties {}: {}", fileName, e.getMessage(), e);
        } finally {
            if (inputStream != null) try { inputStream.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Locate a file in multiple fallback locations: user.dir, jpackage app folder, user home
     */
    private static Path locateFile(String fileName) {
        // 1️⃣ Try folder where IDE/JAR/EXE runs
        Path exeDir = Paths.get(System.getProperty("user.dir")).resolve(fileName);
        if (Files.exists(exeDir)) return exeDir;

        // 2️⃣ Try jpackage app folder
        Path appPath = Paths.get(System.getProperty("java.home"))
                .getParent()
                .resolve("app")
                .resolve(fileName);
        if (Files.exists(appPath)) return appPath;

        // 3️⃣ Fallback: user home
        Path homePath = Paths.get(System.getProperty("user.home"), "", fileName);
        if (Files.exists(homePath)) return homePath;

        return null; // not found
    }

    public  void captureOutput(ITestContext context, String key, String message) {
        List<String> output = (List<String>) context.getAttribute(key);
        if (output == null) {
            output = new ArrayList<>();
        }
        output.add(message);
        context.setAttribute(key, output);
    }

    public void captureFailure(ITestContext context, String key, String message) {
        List<String> output = (List<String>) context.getAttribute(key);
        if (output == null) output = new ArrayList<>();
        output.add(message);
        context.setAttribute(key, output);
    }

}
