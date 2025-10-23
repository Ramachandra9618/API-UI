package models;

import java.util.Map;

public class ModuleInputData {

    private final String serialNo;
    private final String designerName;
    private final String designerEmail;
    private final String designerId;
    private final String officeCity;
    private final String customerId;

    public ModuleInputData(Map<String, String> roomDetails) {
        this.serialNo = getValue(roomDetails, "s.no");
        this.designerName = getValue(roomDetails, "designerName");
        this.designerEmail = getValue(roomDetails, "designerEmail");
        this.designerId = getValue(roomDetails, "designerId");
        this.officeCity = getValue(roomDetails, "officeCity");
        this.customerId = getValue(roomDetails, "customerId");

    }

    // Utility method to safely retrieve values
    private static String getValue(Map<String, String> map, String key) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                String value = entry.getValue();
                return (value != null && !value.isEmpty()) ? value : null;
            }
        }
        return null;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public String getDesignerName() {
        return designerName;
    }

    public String getDesignerEmail() {
        return designerEmail;
    }

    public String getDesignerId() {
        return designerId;
    }

    public String getOfficeCity() {
        return officeCity;
    }

    public String getCustomerId() {
        return customerId;
    }
}

