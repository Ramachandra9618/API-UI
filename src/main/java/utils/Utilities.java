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

public class Utilities {
    private static final Logger log = LogManager.getLogger(Utilities.class);
    private static final Random random = new Random();


    public static String formatCurrentDate(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date());
    }

     // ✅ Preferred base path (auto-adjusts for local vs. cloud)
    private static final String LOCAL_CONFIG_FOLDER = System.getProperty("user.home") + "/OneDrive/Documents/API-UI";
    private static final String CLOUD_CONFIG_FOLDER = System.getProperty("user.home") + "/API-UI";

    /**
     * Dynamically determines config folder depending on environment.
     */
    private static Path getConfigDir() {
        Path localPath = Paths.get(LOCAL_CONFIG_FOLDER);
        if (Files.exists(localPath)) {
            System.out.println("📂 Using local config directory: " + localPath.toAbsolutePath());
            return localPath;
        }

        Path cloudPath = Paths.get(CLOUD_CONFIG_FOLDER);
        try {
            if (!Files.exists(cloudPath)) {
                Files.createDirectories(cloudPath);
                System.out.println("📁 Created cloud config directory: " + cloudPath.toAbsolutePath());
            } else {
                System.out.println("📂 Using existing cloud config directory: " + cloudPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to create cloud config directory: " + e.getMessage());
        }
        return cloudPath;
    }

    /**
     * Updates (or creates) a .properties file with given key-value pairs.
     */
    public synchronized static boolean updateProperties(String fileName, Map<String, String> propertiesToUpdate) {
        Properties props = new Properties();
        Path configDir = getConfigDir();
        Path filePath = configDir.resolve(fileName);

        try {
            // ✅ Load existing properties if file already exists
            if (Files.exists(filePath)) {
                try (InputStream in = Files.newInputStream(filePath)) {
                    props.load(in);
                    System.out.println("📄 Loaded existing properties from: " + filePath.toAbsolutePath());
                }
            } else {
                Files.createFile(filePath);
                System.out.println("🆕 Created new properties file: " + filePath.toAbsolutePath());
            }

            // ✅ Update values
            propertiesToUpdate.forEach(props::setProperty);

            // ✅ Save file
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

    public static String customerNameFormatter(String name, int count, String envirnoment, String brand) {
        String formattedName;
        if (envirnoment.equalsIgnoreCase("prod") && brand.equalsIgnoreCase("DC")) {
            formattedName = "Test" + name + convertDateToStringFormat() + convert(count);
        } else {
            formattedName = name + convertDateToStringFormat() + convert(count);
        }
        return formattedName;
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
}



