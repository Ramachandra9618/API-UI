package utils;

import java.util.HashMap;
import java.util.Map;

public class PropertiesReader {

    private Map<String, Object> testData;
    private Map<String, Object> environments;
    private Map<String, Object> login_Credential;

    // Config fields
    private String environment;
    private String customerType;
    private String baseURl;
    private String ScProUrl;
    private String synapseUrl;
    private String roasterBaseUrl;
    private String logged_in_user_id;
    private String appointment_venue;
    private String rosterCookies;
    private String dp_email;
    private String dp_password;
    private String finance_dp;
    private String finance_Password;
    private String sf_url;
    private String sf_login_email;
    private String sf_login_password;
    private String design_User;

    // Test Data
    private String gmailDomain;
    private int lastProcessedLeadIndex;
    private String mobileNoStarting2digitPrefix;
    private int leadCount;
    private String customerId;
    private String projectID;
    private String customerName;
    private String filedDate;


    public PropertiesReader(Map<String, Object> testData) {
        this.testData = testData;
        load();
    }
    public PropertiesReader(){

    }

    private void load() {
        this.environment = (String) testData.getOrDefault("environment", "");
        this.customerType = (String) testData.getOrDefault("customerType", "");

        this.gmailDomain = (String) testData.getOrDefault("gmailDomain", "");
        this.lastProcessedLeadIndex = Integer.parseInt((String) testData.getOrDefault("lastProcessedLeadIndex", "0"));
        this.mobileNoStarting2digitPrefix = (String) testData.getOrDefault("mobileNoStarting2digitPrefix", "99");
        this.leadCount = Integer.parseInt((String) testData.getOrDefault("totalLeadsToCreate", "1"));
        this.customerId = (String) testData.getOrDefault("customerId", "");
        this.projectID = (String) testData.getOrDefault("projectID", "");
        this.customerName = (String) testData.getOrDefault("customerName", "");
        this.filedDate = (String) testData.getOrDefault("leadScriptRunDate", "");

        this.environments = (Map<String, Object>) testData.get("projectEnvironments");
        this.login_Credential = (Map<String, Object>) testData.get("login_Credential");

        initializeEnvironment(this.environment);
    }

    private void initializeEnvironment(String env) {
        Map<String, String> envData = getSubMap(environments, env);
        Map<String, String> loginData = getSubMap(login_Credential, env);

        if (envData == null || loginData == null) {
            throw new IllegalStateException("Environment or login credentials not found for: " + env);
        }

        ScProUrl = envData.getOrDefault("ScProUrl", "");
        baseURl = envData.getOrDefault("BaseURL", "");
        synapseUrl = envData.getOrDefault("SynapseUrl", "");
        roasterBaseUrl = envData.getOrDefault("RoasterBaseUrl", "");
        rosterCookies = envData.getOrDefault("cookies", "");

        finance_dp = loginData.getOrDefault("finance_email", "");
        finance_Password = loginData.getOrDefault("finance_password", "");
        logged_in_user_id = loginData.getOrDefault("logged_in_user_id", "");
        dp_email = loginData.getOrDefault("dp_email", "");
        dp_password = loginData.getOrDefault("dp_password", "");
        appointment_venue = String.valueOf(testData.getOrDefault(env + "Appointment_venue", ""));

        if (customerType.equalsIgnoreCase("DC")) {
            sf_url = loginData.getOrDefault("SFurl", "");
            sf_login_email = loginData.getOrDefault("SFUserName", "");
            sf_login_password = loginData.getOrDefault("SFPassword", "");
            design_User = loginData.getOrDefault("design_User", "");
            appointment_venue = loginData.getOrDefault("DCAppointment_venue", "");
        }
    }

    private Map<String, String> getSubMap(Map<String, Object> parentMap, String key) {
        if (parentMap == null || key == null) return new HashMap<>();
        for (String mapKey : parentMap.keySet()) {
            if (mapKey.equalsIgnoreCase(key)) {
                Object value = parentMap.get(mapKey);
                if (value instanceof Map) {
                    return (Map<String, String>) value;
                }
            }
        }
        return new HashMap<>();
    }

    // ✅ Getters only — no statics
    public String getEnvironment() { return environment; }
    public String getBaseURl() { return baseURl; }
    public String getScProUrl() { return ScProUrl; }
    public String getRoasterBaseUrl() { return roasterBaseUrl; }
    public String getDp_email() { return dp_email; }
    public String getDp_password() { return dp_password; }
    public String getMobilePrefix() {
        return mobileNoStarting2digitPrefix; }
    public int getLeadCount() { return leadCount; }

    public String getCustomerName(){
        return customerName;
    }
    public String getGmailDomain(){
        return gmailDomain;
    }

    public String getFinance_dp() {
        return finance_dp;
    }
    public String getFinance_Password(){
        return finance_Password;
    }
    public String getSf_url(){
        return sf_url;
    }
    public String getSf_login_email(){
        return sf_login_email;
    }

    public String getSf_login_password(){
        return sf_login_password;
    }
    public String getAppointment_venue() {
        return appointment_venue;
    }

    public String getDesign_User(){
        return design_User;
    }

    public String getSynapseUrl(){
        return synapseUrl;
    }

    public String getFiledDate(){
        return filedDate;
    }

    public int getLastProcessedIndex(){
        return lastProcessedLeadIndex;
    }
    public String getLoggedInUserId(){
        return logged_in_user_id;
    }
}
