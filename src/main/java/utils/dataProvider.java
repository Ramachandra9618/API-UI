package utils;

import org.testng.annotations.DataProvider;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class dataProvider {

    @DataProvider(name = "leadDataProvider")
    public static Object[][] provideLeadData() {
        String yamlFile = "testData/meetingDetails.yml";
        List<Map<String, String>> dataList = new ArrayList<>();
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(yamlFile)) {
            Object loaded = yaml.load(inputStream);
            if (loaded instanceof Map) {
                Map<String, String> dataMap = new HashMap<>();
                ((Map<?, ?>) loaded).forEach((k, v) -> dataMap.put(String.valueOf(k), String.valueOf(v)));
                dataList.add(dataMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object[][] result = new Object[dataList.size()][1];
        for (int i = 0; i < dataList.size(); i++) {
            result[i][0] = dataList.get(i);
        }
        return result;
    }
}
