package configurator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import static utils.Utilities.*;

public class BaseClass extends ApiService {

    public static final Logger log = LogManager.getLogger(BaseClass.class);
    public static final HashMap<String, Object> testData = new HashMap<>();

    public static String customerType;
    private static String userSelectedCityProperty;


    public static void initializeTestData() {
        testData.clear();

        // Load user configuration properties
        addToTestDataFromProperties("userConfigurations.properties", testData);

        // Read customerType and city
        customerType = (String) testData.get("customerType");
        userSelectedCityProperty = String.valueOf(testData.get("userSelectedCityProperty"));
        if (customerType == null || userSelectedCityProperty == null) {
            log.error("❌ customerType or userSelectedCityProperty is missing in userConfigurations.properties");
            return;
        }

        // Load standard, customer-specific, and city configurations
        loadStandardConfig(customerType, testData);
        loadCustomerConfig(customerType, testData);
        loadCityConfig(userSelectedCityProperty, testData);

        log.info("✅ Test data initialized. CustomerType: {}, CityCode: {}", customerType, userSelectedCityProperty);
    }


    // public synchronized static void addToTestDataFromProperties(String fileName, Map<String, Object> testData) {
    //     Properties prop = new Properties();
    //     InputStream inputStream = null;
    //     try {
    //         // Try to locate file in filesystem first (for cloud and local compatibility)
    //         Path filePath = locateFile(fileName);
    //         if (filePath != null) {
    //             inputStream = Files.newInputStream(filePath);
    //             log.info("📄 Loading properties from filesystem: {}", filePath.toAbsolutePath());
    //         } else {
    //             // Fallback to classpath if file not found in filesystem
    //             inputStream = openClasspathStream(fileName);
    //             if (inputStream == null) {
    //                 log.error("❌ Properties file not found in filesystem or classpath: {}", fileName);
    //                 return;
    //             }
    //         }
            
    //         prop.load(inputStream);
    //         for (String name : prop.stringPropertyNames()) {
    //             testData.put(name, prop.getProperty(name));
    //         }
    //         log.info("✅ Properties loaded successfully from {}", fileName);
    //     } catch (IOException e) {
    //         log.error("❌ Failed to read properties {}: {}", fileName, e.getMessage(), e);
    //     } finally {
    //         if (inputStream != null) try { inputStream.close(); } catch (IOException ignored) {}
    //     }
    // }

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
