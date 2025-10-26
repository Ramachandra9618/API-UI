package utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class UpdateJsons {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void updatePayloadFromMap(Map<String, String> map, String filename) {
        JSONObject jsonObject = readJsonFile(filename);
        jsonObject.putAll(map);
        writeJsonFile(filename, jsonObject);
    }

    public  JSONObject readJsonFile(String filepath) {
        try (FileReader reader = new FileReader(filepath)) {
            JSONParser jsonParser = new JSONParser();
            return (JSONObject) jsonParser.parse(reader);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + filepath, e);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error reading or parsing file: " + filepath, e);
        }
    }

    public void writeJsonFile(String filepath, JSONObject jsonObject) {
        try (FileOutputStream outputStream = new FileOutputStream(filepath)) {
            byte[] strToBytes = jsonObject.toJSONString().getBytes();
            outputStream.write(strToBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error writing JSON to file: " + filepath, e);
        }
    }

    public String updatePayloadFromMap(String relativePath, Map<String, Object> updatedData) {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(relativePath)) {

        if (inputStream == null) {
            throw new FileNotFoundException("❌ Payload not found in resources: " + relativePath);
        }

        // Read JSON from classpath
        JsonNode rootNode = objectMapper.readTree(inputStream);

        // Apply updates
        if (rootNode.isObject()) {
            updateNode((ObjectNode) rootNode, updatedData);
        } else if (rootNode.isArray()) {
            for (JsonNode item : rootNode) {
                if (item.isObject()) {
                    updateNode((ObjectNode) item, updatedData);
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported root node type in JSON: " + relativePath);
        }

        // ✅ No file writing here — we just return the updated JSON string
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

    } catch (IOException e) {
        throw new RuntimeException("❌ Failed to update JSON payload: " + e.getMessage(), e);
    }
}


    // public String updatePayloadFromMap(String relativePath, Map<String, Object> updatedData) {
    //     try {
    //         // 🔍 Resolve the file dynamically for IDE / JAR / jpackage
    //         Path resolvedPath = resolveAppFile(relativePath);
    //         if (resolvedPath == null) {
    //             throw new FileNotFoundException("❌ Payload file not found: " + relativePath);
    //         }

    //        // System.out.println("📄 Loading JSON from: " + resolvedPath.toAbsolutePath());

    //         // 🔹 Read the JSON
    //         JsonNode rootNode = objectMapper.readTree(resolvedPath.toFile());

    //         // 🔹 Apply updates
    //         if (rootNode.isObject()) {
    //             updateNode((ObjectNode) rootNode, updatedData);
    //         } else if (rootNode.isArray()) {
    //             for (JsonNode item : rootNode) {
    //                 if (item.isObject()) {
    //                     updateNode((ObjectNode) item, updatedData);
    //                 }
    //             }
    //         } else {
    //             throw new IllegalArgumentException("Unsupported root node type in JSON: " + relativePath);
    //         }

    //         // 🔹 Write back to same file
    //         objectMapper.writerWithDefaultPrettyPrinter().writeValue(resolvedPath.toFile(), rootNode);

    //        // System.out.println("💾 Updated payload saved to: " + resolvedPath.toAbsolutePath());

    //         return objectMapper.writeValueAsString(rootNode);

    //     } catch (IOException e) {
    //         throw new RuntimeException("❌ Failed to update JSON payload: " + e.getMessage(), e);
    //     }
    // }


    // public static Path resolveAppFile(String relativePath) {
    //     Path workingDir = Paths.get(System.getProperty("user.dir")).resolve(relativePath);
    //     if (Files.exists(workingDir)) return workingDir;
    //     Path jpackagePath = Paths.get(System.getProperty("java.home"))
    //             .getParent()
    //             .resolve("app")
    //             .resolve(relativePath);
    //     if (Files.exists(jpackagePath)) return jpackagePath;
    //     Path homePath = Paths.get(System.getProperty("user.home"), "SF_Lead_Creation", relativePath);
    //     if (Files.exists(homePath)) return homePath;

    //     // ❌ Not found anywhere
    //     System.err.println("❌ File not found in any known location: " + relativePath);
    //     return null;
    // }



    private static void updateNode(ObjectNode node, Map<String, Object> updates) {
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                if (node.has(key) && node.get(key).isObject()) {
                    updateNode((ObjectNode) node.get(key), (Map<String, Object>) value);
                } else {
                    node.set(key, objectMapper.valueToTree(value));
                }
            } else if (value instanceof Iterable) {
                ArrayNode arrayNode = objectMapper.createArrayNode();
                for (Object item : (Iterable<?>) value) {
                    if (item instanceof Map) {
                        ObjectNode childNode = objectMapper.createObjectNode();
                        updateNode(childNode, (Map<String, Object>) item);
                        arrayNode.add(childNode);
                    } else {
                        arrayNode.addPOJO(item);
                    }
                }
                node.set(key, arrayNode);
            } else {
                node.putPOJO(key, value);
            }
        }
    }

}
