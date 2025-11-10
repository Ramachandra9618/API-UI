package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Utilities {
    private static final Logger log = LogManager.getLogger(Utilities.class);
    private static final Random random = new Random();

    // ✅ Preferred base path (auto-adjusts for local vs. cloud)
    private static final String LOCAL_CONFIG_FOLDER = System.getProperty("user.home") + "/OneDrive/Documents/API-UI/input";
    private static final String CLOUD_CONFIG_FOLDER = System.getProperty("user.home") + "/API-UI/input";

    // ==============================================
    // 💠 SECTION 1: DATE TOKEN GENERATOR
    // ==============================================

    private static final String[] MONTHS = {
            "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
    };

    private static final String[] ONES = {
            "Zero","One","Two","Three","Four","Five","Six","Seven","Eight","Nine",
            "Ten","Eleven","Twelve","Thirteen","Fourteen","Fifteen","Sixteen","Seventeen","Eighteen","Nineteen"
    };

    private static final String[] TENS = {
            "","", "Twenty","Thirty","Forty","Fifty","Sixty","Seventy","Eighty","Ninety"
    };

    /**
     * Converts a LocalDate into a compact token:
     * Example: 2025-11-13 -> XIIINovTTFive
     */
    public static String formatDateToken(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("date cannot be null");
        return formatDateToken(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    public static String formatDateToken(int day, int month, int year) {
        if (day < 1 || day > 31) throw new IllegalArgumentException("day must be 1..31");
        if (month < 1 || month > 12) throw new IllegalArgumentException("month must be 1..12");
        String dayRoman = toRoman(day);
        String monthShort = MONTHS[month - 1];
        String yearToken = deriveYearToken(year);
        return dayRoman + monthShort + yearToken;
    }

    private static String toRoman(int num) {
        if (num <= 0) return "";
        int[] vals = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
        String[] syms = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (num > 0 && i < vals.length) {
            while (num >= vals[i]) {
                num -= vals[i];
                sb.append(syms[i]);
            }
            i++;
        }
        return sb.toString();
    }

    /**
     * Creates compact year token.
     * Example: 2025 -> TTFive, 5058 -> FFEight, 8098 -> ENEight
     */
    public static String deriveYearToken(int year) {
        int y = Math.abs(year);
        int thousandsDigit = (y / 1000) % 10;
        String thousandsWord = ONES[thousandsDigit];
        int lastTwo = y % 100;
        String tensWord;
        if (lastTwo < 10) {
            tensWord = "Zero";
        } else if (lastTwo < 20) {
            tensWord = ONES[lastTwo];
        } else {
            tensWord = TENS[(lastTwo / 10) % 10];
        }
        int unitsDigit = y % 10;
        String unitsWord = ONES[unitsDigit];
        char p1 = Character.toUpperCase(thousandsWord.charAt(0));
        char p2 = Character.toUpperCase(tensWord.charAt(0));
        String prefix = "" + p1 + p2;
        return prefix + unitsWord;
    }

    // ==============================================
    // 💠 SECTION 2: UTILITIES START
    // ==============================================

    public static String formatCurrentDate(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }

    private static Path getConfigDir() {
        Path localPath = Paths.get(LOCAL_CONFIG_FOLDER);
        if (Files.exists(localPath)) {
            System.out.println("💻 Using local config directory: " + localPath.toAbsolutePath());
            return localPath;
        }

        Path cloudPath = Paths.get(CLOUD_CONFIG_FOLDER);
        try {
            if (!Files.exists(cloudPath)) {
                Files.createDirectories(cloudPath);
                System.out.println("☁️ Created fallback cloud config directory: " + cloudPath.toAbsolutePath());
            } else {
                System.out.println("☁️ Using existing fallback cloud config directory: " + cloudPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to create cloud config directory: " + e.getMessage());
        }
        return cloudPath;
    }

    // ✅ Update or create .properties file
    public synchronized static boolean updateProperties(String fileName, Map<String, String> propertiesToUpdate) {
        Properties props = new Properties();
        Path configDir = getConfigDir();
        Path filePath = configDir.resolve(fileName);

        try {
            if (!Files.exists(configDir)) Files.createDirectories(configDir);

            if (Files.exists(filePath)) {
                try (InputStream in = Files.newInputStream(filePath)) {
                    props.load(in);
                    System.out.println("📄 Loaded existing properties from: " + filePath.toAbsolutePath());
                }
            } else {
                Files.createFile(filePath);
                System.out.println("🆕 Created new properties file: " + filePath.toAbsolutePath());
                propertiesToUpdate.put("lastProcessedLeadIndex", "1");
                propertiesToUpdate.put("gmailDomain", "@yopmail.com");
                String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                propertiesToUpdate.put("leadScriptRunDate", formattedDate);
            }

            propertiesToUpdate.forEach(props::setProperty);

            try (OutputStream out = Files.newOutputStream(filePath)) {
                props.store(out, "Updated by ConfigUtils");
            }

            System.out.println("✅ Properties updated successfully: " + filePath.toAbsolutePath());
            return true;
        } catch (IOException e) {
            System.err.println("❌ Error updating properties file: " + e.getMessage());
            return false;
        }
    }

    // ✅ Enhanced customer name generator (date token + count in words)
    public static String customerNameFormatter(String name, int count, String environment,int LeadCount) {
        String prefix = environment.equalsIgnoreCase("prod") ? "Test" : "";
        if (LeadCount == 1){
           return prefix+name;
        }else {
            String datePart = formatDateToken(LocalDate.now()); // 🔥 Using date token
            String countWord = numberToWords(count); // 🔠 Convert number to words
            return String.format("%s%s%s%s",
                    prefix,
                    name.trim().replaceAll("\\s+", ""),
                    datePart,
                    countWord);
        }
    }

    // ✅ Number → Words
    private static String numberToWords(int num) {
        if (num < 0) return "Minus" + numberToWords(-num);
        if (num == 0) return "Zero";

        String[] ones = {
                "", "One", "Two", "Three", "Four", "Five", "Six", "Seven",
                "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen",
                "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
        };
        String[] tens = {
                "", "", "Twenty", "Thirty", "Forty", "Fifty",
                "Sixty", "Seventy", "Eighty", "Ninety"
        };

        StringBuilder sb = new StringBuilder();
        if (num >= 1000) {
            sb.append(ones[num / 1000]).append("Thousand");
            num %= 1000;
        }
        if (num >= 100) {
            sb.append(ones[num / 100]).append("Hundred");
            num %= 100;
        }
        if (num >= 20) {
            sb.append(tens[num / 10]);
            if (num % 10 != 0) sb.append(ones[num % 10]);
        } else if (num > 0) {
            sb.append(ones[num]);
        }
        return sb.toString();
    }


// ✅ CSV utilities
public synchronized static Map<String, Object> addToTestDataFromProperties(String fileName, Map<String, Object> testData) {
    Properties prop = new Properties();
    Path filePath = null;

    // 1️⃣ Try local folder
    Path localFile = Paths.get(LOCAL_CONFIG_FOLDER, fileName);
    if (Files.exists(localFile)) {
        filePath = localFile;
        log.info("📂 Using local properties: {}", filePath.toAbsolutePath());
    } else {
        // 2️⃣ Try cloud folder
        Path cloudFile = Paths.get(CLOUD_CONFIG_FOLDER, fileName);
        if (Files.exists(cloudFile)) {
            filePath = cloudFile;
            log.info("☁️ Using cloud properties: {}", filePath.toAbsolutePath());
        }
    }

    try (InputStream inputStream = filePath != null
            ? Files.newInputStream(filePath)
            : Utilities.class.getResourceAsStream("/" + fileName)) {

        if (inputStream == null) {
            log.error("❌ Properties file not found in local, cloud, or classpath: {}", fileName);
            return testData; // ✅ return input map even if file not found
        }

        prop.load(inputStream);

        for (String name : prop.stringPropertyNames()) {
            testData.put(name, prop.getProperty(name));
        }

        log.info("✅ Properties loaded successfully from {}",
                filePath != null ? filePath.toAbsolutePath() : "classpath:/" + fileName);

    } catch (IOException e) {
        log.error("❌ Failed to read properties {}: {}", fileName, e.getMessage(), e);
    }

    return testData; // ✅ return the updated map
}


    public void appendTestResults(String[] testResults, String filepath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath, true))) {
            writer.write(String.join(",", testResults));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createCSVReport(String[] headers, String filePath) {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if (!Files.exists(Paths.get(filePath))) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(String.join(",", headers));
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isBeforeToday(String filedDate) {
        try {
            if (filedDate == null || filedDate.isBlank()) {
                filedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }
            filedDate = filedDate.trim().replaceAll("[^0-9\\-]", "");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH);
            LocalDate fileDateParsed = LocalDate.parse(filedDate, formatter);
            LocalDate today = LocalDate.now();
            return fileDateParsed.isBefore(today);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: [" + filedDate + "] → " + e.getMessage());
            return false;
        }
    }

    public void renamingLeadReportFile(String filePath, String finalFileName) {
        File oldFile = new File(filePath);
        String parentDir = oldFile.getParent();
        File newFile = new File(parentDir, finalFileName);
        boolean renamed = oldFile.renameTo(newFile);
        if (!renamed) log.warn("⚠️ Failed to rename report file.");
    }

    public static String getTomorrowDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        return formatter.format(c.getTime());
    }

    public static String getDateTwoMonthsInTheFuture() {
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsLater = today.plusMonths(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return twoMonthsLater.format(formatter);
    }

    public static String getTodayDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        return simpleDateFormat.format(new Date());
    }

    public static String generateRandomString(int length) {
        if (length <= 0) return "";
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomAscii;
            if (ThreadLocalRandom.current().nextBoolean()) {
                randomAscii = ThreadLocalRandom.current().nextInt(65, 91);
            } else {
                randomAscii = ThreadLocalRandom.current().nextInt(97, 123);
            }
            stringBuilder.append((char) randomAscii);
        }
        return stringBuilder.toString();
    }

    public static void csvToProperties(String csvFilePath, String propertiesFile) throws IOException {
        Properties properties = new Properties();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
             FileWriter writer = new FileWriter(propertiesFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] keyValue = line.split(",", 2);

                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    properties.setProperty(key, value);
                } else {
                    log.error("Invalid line in CSV: {}", line);
                }
            }
            properties.store(writer, "Generated from CSV");
        }
    }

    public static Map<String, Map<String, String>> loadCityDataFromJson(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            Map<String, Map<String, String>> cityData = new HashMap<>();
            for (Object key : jsonObject.keySet()) {
                String type = (String) key;
                JSONObject cities = (JSONObject) jsonObject.get(type);
                Map<String, String> cityMap = new HashMap<>();
                for (Object cityKey : cities.keySet()) {
                    cityMap.put((String) cityKey, (String) cities.get(cityKey));
                }
                cityData.put(type, cityMap);
            }
            return cityData;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static String getInputPath(String resourcePath) {
        try {
            InputStream is = Utilities.class.getResourceAsStream("/" + resourcePath);
            if (is != null) {
                Path tempFile = Files.createTempFile("temp_resource_", "_" + resourcePath);
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                tempFile.toFile().deleteOnExit();
                return tempFile.toAbsolutePath().toString();
            }
            Path userDirPath = Paths.get(System.getProperty("user.dir"), "input", resourcePath);
            if (Files.exists(userDirPath)) return userDirPath.toAbsolutePath().toString();
            Path homePath = Paths.get(System.getProperty("user.home"), "SF_Lead_Creation", resourcePath);
            if (Files.exists(homePath)) return homePath.toAbsolutePath().toString();
            System.err.println("❌ Resource not found: " + resourcePath);
            return null;
        } catch (IOException e) {
            System.err.println("❌ Failed to load resource: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream getResourceAsStream(String fileName) {
        InputStream stream = Utilities.class.getResourceAsStream("/" + fileName);
        if (stream == null) {
            try {
                return new FileInputStream(System.getProperty("user.dir") + File.separator + "input" + File.separator + fileName);
            } catch (Exception e) {
                throw new RuntimeException("File not found: " + fileName);
            }
        }
        return stream;
    }

    public static Map<String, Object> addToTestDataFromYml(String fileName, Map<String, Object> testData) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = openClasspathStream(fileName)) {
            if (inputStream == null) return testData;
            Map<String, Object> yamlData = yaml.load(inputStream);
            if (yamlData != null) testData.putAll(yamlData);
        } catch (Exception e) {
            log.error("❌ Failed to read YAML {}: {}", fileName, e.getMessage(), e);
        }
        return testData;
    }

    public static Map<String, Object> loadStandardConfig(String customerType, Map<String, Object> testData) {
        Map<String, Object> updatedTestData = new HashMap<>();
        String configFile = switch (customerType) {
            case "HL", "LUXE", "HFN" -> "configuration/HL_standardConfigurations.yml";
            case "DC" -> "configuration/DC_standardConfigurations.yml";
            default -> null;
        };
        if (configFile != null) updatedTestData = addToTestDataFromYml(configFile, testData);
        return updatedTestData;
    }

    public static Map<String, Object> loadCustomerConfig(String customerType, Map<String, Object> testData) {
        Map<String, Object> updatedTestData = new HashMap<>();
        String configFile = switch (customerType) {
            case "HL", "LUXE" -> "configuration/HL_config.yml";
            case "HFN" -> "configuration/HFN_config.yml";
            case "DC" -> "configuration/DC_config.yml";
            default -> null;
        };
        if (configFile != null) updatedTestData = addToTestDataFromYml(configFile, testData);
        return updatedTestData;
    }

    public static Map<String, Object> loadCityConfig(String citycode, Map<String, Object> testData) {
        Map<String, Object> updatedTestData = new HashMap<>();
        String cityFile = switch (citycode) {
            case "1" -> "configuration/CitiesData/homelane/1-Bengaluru.properties";
            case "2" -> "configuration/CitiesData/homelane/2-Chennai.properties";
            case "3" -> "configuration/CitiesData/homelane/3-Mumbai.properties";
            default -> null;
        };
        if (cityFile != null) updatedTestData = addToTestDataFromProperties(cityFile, testData);
        return updatedTestData;
    }

    private static InputStream openClasspathStream(String fileName) {
        String cpName = fileName.startsWith("/") ? fileName.substring(1) : fileName;
        InputStream is = Utilities.class.getClassLoader().getResourceAsStream(cpName);
        if (is == null) {
            log.error("❌ Resource not found on classpath: {}", cpName);
        }
        return is;
    }
}
