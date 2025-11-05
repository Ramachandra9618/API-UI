package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.yaml.snakeyaml.Yaml;

public class Utilities {
    private static final Logger log = LogManager.getLogger(Utilities.class);
    private static final Random random = new Random();


    public static String formatCurrentDate(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }

     // ✅ Preferred base path (auto-adjusts for local vs. cloud)
    private static final String LOCAL_CONFIG_FOLDER = System.getProperty("user.home") + "/OneDrive/Documents/API-UI/input"; // ✅ include /input
    private static final String CLOUD_CONFIG_FOLDER = System.getProperty("user.home") + "/API-UI/input"; // ✅ include /input

    /**
     * Dynamically determines config folder depending on environment.
     */
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


    /**
     * Updates (or creates once) a .properties file with given key-value pairs.
     * ✅ Never recreates file — only updates or adds keys.
     */
    public synchronized static boolean updateProperties(String fileName, Map<String, String> propertiesToUpdate) {
        Properties props = new Properties();
        Path configDir = getConfigDir();
        Path filePath = configDir.resolve(fileName);

        try {
            // ✅ Ensure directory exists
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            // ✅ Load existing properties if file exists
            if (Files.exists(filePath)) {
                try (InputStream in = Files.newInputStream(filePath)) {
                    props.load(in);
                    System.out.println("📄 Loaded existing properties from: " + filePath.toAbsolutePath());
                }
            } else {
                // Create empty file only once
                Files.createFile(filePath);
                System.out.println("🆕 Created new properties file: " + filePath.toAbsolutePath());
                propertiesToUpdate.put("lastProcessedLeadIndex", "1");
                propertiesToUpdate.put("gmailDomain", "@yopmail.com");
                String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                propertiesToUpdate.put("leadScriptRunDate", formattedDate);   
            }

            // Print current values before update
            if (!props.isEmpty()) {
                System.out.println("🔹 Current properties before update:");
                props.forEach((k, v) -> System.out.println(k + " = " + v));
            } else {
                System.out.println("⚪ No existing properties, starting fresh.");
            }

            // Apply updates (overwrites same keys, keeps old ones)
            propertiesToUpdate.forEach(props::setProperty);

            // Print values after update
            System.out.println("🔹 Properties after update:");
            props.forEach((k, v) -> System.out.println(k + " = " + v));


            // ✅ Apply updates (overwrites same keys, keeps old ones)
            propertiesToUpdate.forEach(props::setProperty);

            // ✅ Save without deleting existing data
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

    /**
     * Reads properties file into a map.
     */
    public synchronized static Map<String, String> readProperties(String fileName) {
        Map<String, String> result = new HashMap<>();
        Path configDir = getConfigDir();
        Path filePath = configDir.resolve(fileName);

        if (!Files.exists(filePath)) {
            System.err.println("⚠️ Properties file not found: " + filePath.toAbsolutePath());
            return result;
        }

        try (InputStream in = Files.newInputStream(filePath)) {
            Properties props = new Properties();
            props.load(in);
            for (String key : props.stringPropertyNames()) {
                result.put(key, props.getProperty(key));
            }
            System.out.println("📄 Loaded properties from: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Error reading properties: " + e.getMessage());
        }

        return result;
    }

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
                //log.info("Test report created with headers.");
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
      
        // Only before today (your logic seems incorrect earlier)
        return fileDateParsed.isBefore(today);

    } catch (DateTimeParseException e) {
        System.err.println("Invalid date format: [" + filedDate + "] → " + e.getMessage());
        System.out.println("time check failed");
        return false;
    }
}


    public void renamingLeadReportFile(String filePath, String finalFileName) {
        File oldFile = new File(filePath);
        String parentDir = oldFile.getParent();

        File newFile = new File(parentDir, finalFileName);
        boolean renamed = oldFile.renameTo(newFile);
        if (renamed) {
            //log.info("📝 Renamed report to: {}", newFile.getAbsolutePath());
        } else {
            log.warn("⚠️ Failed to rename report file.");
        }
    }

    public static String getTomorrowDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        date = c.getTime();
        return formatter.format(date);
    }


    public static String getDateTwoMonthsInTheFuture() {
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsLater = today.plusMonths(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return twoMonthsLater.format(formatter);
    }


    public static String getTodayDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        Date date = new Date();
        return simpleDateFormat.format(date);
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


    public static String convertDateToStringFormat() {
        Date today = new Date();

        SimpleDateFormat dayFormat = new SimpleDateFormat("d");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yy");

        int day = Integer.parseInt(dayFormat.format(today));
        int month = Integer.parseInt(monthFormat.format(today));
        int year = Integer.parseInt(yearFormat.format(today));

        // Mapping for months
        String[] months = {
                "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"
        };

        // Mapping for day numbers
        String[] days = {
                "", "first", "second", "third", "fourth", "fifth", "sixth", "seventh",
                "eighth", "ninth", "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth",
                "fifteenth", "sixteenth", "seventeenth", "eighteenth", "nineteenth", "twentieth",
                "twentyfirst", "twentysecond", "twentythird", "twentyfourth", "twentyfifth",
                "twentysixth", "twentyseventh", "twentyeighth", "twentyninth", "thirtieth",
                "thirtyfirst"
        };

        // Convert year to words
        Map<Character, String> numberWords = new HashMap<>();
        numberWords.put('0', "zero");
        numberWords.put('1', "one");
        numberWords.put('2', "two");
        numberWords.put('3', "three");
        numberWords.put('4', "four");
        numberWords.put('5', "five");
        numberWords.put('6', "six");
        numberWords.put('7', "seven");
        numberWords.put('8', "eight");
        numberWords.put('9', "nine");

        StringBuilder yearInWords = new StringBuilder();
        for (char digit : String.valueOf(year).toCharArray()) {
            yearInWords.append(numberWords.get(digit));
        }
        return days[day] + months[month] + yearInWords;
    }

    public static String convert(int number) {
        String[] ones = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};

        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

        if (number == 0) {
            return "Zero";
        }
        String result = "";

        if (number >= 1000) {
            result += ones[number / 1000] + "Thousand";
            number %= 1000;
        }
        if (number >= 100) {
            result += ones[number / 100] + "Hundred";
            number %= 100;
        }
        if (number >= 20) {
            result += tens[number / 10];
            number %= 10;
        }
        if (number > 0) {
            result += ones[number];
        }
        return result.trim();
    }

    public static String customerNameFormatter(String name, int count, String environment, String brand) {
    String prefix = environment.equalsIgnoreCase("prod") ? "Test" : "";
    String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    return String.format("%s%s",
            prefix,
            name.trim().replaceAll("\\s+", ""));
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
            //log.info("Properties file created successfully: {}", propertiesFile);
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
                System.out.println("📄 Resource loaded from classpath: " + resourcePath);
                return tempFile.toAbsolutePath().toString();
            }
            Path userDirPath = Paths.get(System.getProperty("user.dir"), "input", resourcePath);
            if (Files.exists(userDirPath)) {
                System.out.println("📄 Resource loaded from working directory: " + userDirPath.toAbsolutePath());
                return userDirPath.toAbsolutePath().toString();
            }

            Path appDir = Paths.get(System.getProperty("java.home"))
                    .getParent()
                    .resolve("app")
                    .resolve(resourcePath);
            if (Files.exists(appDir)) {
                System.out.println("📄 Resource loaded from app directory: " + appDir.toAbsolutePath());
                return appDir.toAbsolutePath().toString();
            }

            Path homePath = Paths.get(System.getProperty("user.home"), "SF_Lead_Creation", resourcePath);
            if (Files.exists(homePath)) {
                System.out.println("📄 Resource loaded from user home folder: " + homePath.toAbsolutePath());
                return homePath.toAbsolutePath().toString();
            }

            // 5️⃣ Resource not found
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
                return new java.io.FileInputStream(System.getProperty("user.dir") + File.separator + "input" + File.separator + fileName);
            } catch (Exception e) {
                throw new RuntimeException("File not found: " + fileName);
            }
        }
        return stream;
    }

    public static Map<String, Object> addToTestDataFromYml(String fileName, Map<String, Object> testData) {
    Yaml yaml = new Yaml();
    try (InputStream inputStream = openClasspathStream(fileName)) { // try-with-resources
        if (inputStream == null) {
            log.warn("⚠️ YAML file {} not found in classpath", fileName);
            return testData;
        }

        Map<String, Object> yamlData = yaml.load(inputStream);
        if (yamlData != null) {
            testData.putAll(yamlData);
            log.info("✅ Loaded YAML test data from {}", fileName);
        } else {
            log.warn("⚠️ YAML file {} was empty or invalid", fileName);
        }
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
        if (configFile != null) {
         updatedTestData =   addToTestDataFromYml(configFile, testData);
        } else {
            log.warn("Unknown customer type: {}", customerType);
        }
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
        if (configFile != null) {
            updatedTestData = addToTestDataFromYml(configFile, testData);
        } else {
            log.warn("Unknown customer type: {}", customerType);
        }
        return updatedTestData;
    }

    public static Map<String, Object> loadCityConfig(String citycode, Map<String, Object> testData) {
        Map<String, Object> updatedTestData = new HashMap<>();
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
            updatedTestData = addToTestDataFromProperties(cityFile, testData);
        } else {
            log.warn("Unknown city code: {}", citycode);
        }
        return updatedTestData; 
    }


       private static InputStream openClasspathStream(String fileName) {
        String cpName = fileName.startsWith("/") ? fileName.substring(1) : fileName;
        InputStream is = Utilities.class.getClassLoader().getResourceAsStream(cpName);
        if (is == null) {
            log.error("❌ Resource not found on classpath: {}", cpName);
        } else {
            System.out.println("📄 Loading from classpath: " + cpName);
        }
        return is;
    }
}



