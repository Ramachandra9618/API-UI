package tests;

import configurator.BaseClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import services.RoasterService;
import services.SFService;
import utils.PropertiesReader;
import utils.Utilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static utils.Utilities.*;

public class CreateMultipleLeadsFromTheRosterTest extends BaseClass {

    private static final Logger logger = LogManager.getLogger(CreateMultipleLeadsFromTheRosterTest.class);
    private RoasterService roasterService;
    private PropertiesReader propertiesReader;
    private final SFService sfService = new SFService();
    private final Utilities utilities = new Utilities();

    private Map<String, String> projectDetails = new HashMap<>();
    private String filepath;
    private int currentIndex;

    /**
     * Dynamically resolves base path for reports (works for both local + cloud)
     */
    private static String getReportsBaseDir() {
        String envReportDir = System.getenv("REPORT_PATH");
        if (envReportDir != null && !envReportDir.isBlank()) {
            return envReportDir;
        }
        // ✅ Default: ~/API-UI/reports (works on both local & Railway)
        return System.getProperty("user.home") + "/API-UI/reports";
    }

    @BeforeMethod
    public void setUp() {
        refreshTestData();
        propertiesReader = new PropertiesReader(testData);
        roasterService = new RoasterService();
        roasterService.initialize();

        if (!leadInputValidation()) {
            throw new RuntimeException("Lead input validation failed. Aborting test execution.");
        }

        System.out.println("lead input validation passed ✅");

        // Always generate CSV in valid base path
        filepath = generateLeadCreationCSV(propertiesReader.getEnvironment(), customerType);
        System.out.println("📁 Report CSV created at: " + filepath);
    }

    @Test(enabled = true)
    public void testCreateBulkLeads(ITestContext context) {
        System.out.println("testCreateBulkLeads started");
        runLeadCreation(propertiesReader.getLeadCount(), customerType, propertiesReader.getEnvironment(), currentIndex, context);
    }

    private void runLeadCreation(int leadCount, String customerType, String environment, int currentIndex, ITestContext context) {
        String dpId = "745564";
        String dpEmail = "testorgstructuredp1@homelane.com";
        String dpName = "Test DP User";
        System.out.println("runLeadCreationBulk started");
        runLeadCreationBulk(leadCount, customerType, dpId, dpEmail, dpName, currentIndex, context);
    }

    private void runLeadCreationBulk(int leadCount, String customerType,
                                     String dpId, String dpEmail, String dpName,
                                     int workingIndex, ITestContext context) {

        int successfulLeads = 0;
        String startMessage = String.format("🚀 Starting to create %d lead(s) from index %d onward.", leadCount, workingIndex);
        logger.info(startMessage);
        captureOutput(context, "testCreateBulkLeadsOutput", startMessage);

        while (successfulLeads < leadCount) {
            boolean success = false;

            for (int attempt = 1; attempt <= 3 && !success; attempt++) {
                try {
                    String creatingMsg = String.format("Creating lead at index %d (attempt %d).", workingIndex, attempt);
                    logger.info(creatingMsg);
                    captureOutput(context, "testCreateBulkLeadsOutput", creatingMsg);

                    projectDetails = switch (customerType) {
                        case "HL", "HFN", "LUXE" -> roasterService.createProject(dpEmail, workingIndex);
                        case "DC" -> {
                            String userId = sfService.createLeadInSalesforce(workingIndex);
                            yield roasterService.completeProLeadSetup(userId);
                        }
                        default -> null;
                    };

                    if (projectDetails != null &&
                            projectDetails.containsKey("customerId") &&
                            projectDetails.containsKey("projectID")) {

                        String successMsg = String.format(
                                "✅ Lead #%d created successfully. CustomerId=%s, ProjectID=%s, DP Email=%s",
                                workingIndex,
                                projectDetails.get("customerId"),
                                projectDetails.get("projectID"),
                                projectDetails.get("dpEmail")
                        );
                        logger.info(successMsg);
                        captureOutput(context, "projectFullURL", projectDetails.get("fullProjectURL"));
                        captureOutput(context, "customerId", projectDetails.get("customerId"));
                        captureOutput(context, "testCreateBulkLeadsOutput", successMsg);

                        String[] resultData = {
                                String.valueOf(workingIndex),
                                customerType,
                                projectDetails.get("customerId"),
                                projectDetails.get("projectID"),
                                projectDetails.get("dpName"),
                                projectDetails.get("dpEmail"),
                                projectDetails.get("dpMobile")
                        };
                        utilities.appendTestResults(resultData, filepath);

                        workingIndex++;
                        successfulLeads++;
                        success = true;

                    } else {
                        String warnMsg = String.format("⚠️ Lead #%d creation failed on attempt %d: Incomplete project details.", workingIndex, attempt);
                        logger.warn(warnMsg);
                        captureFailure(context, "testCreateBulkLeadsFailures", warnMsg);
                    }

                } catch (Throwable t) {
                    String errorMsg = String.format("🛑 Lead #%d failed on attempt %d: %s", workingIndex, attempt, t.getMessage());
                    logger.error(errorMsg);
                    captureFailure(context, "testCreateBulkLeadsOutput", errorMsg);
                }

                if (!success && attempt < 3) {
                    String retryMsg = String.format("🔁 Retrying lead #%d (attempt %d/3)", workingIndex + 1, attempt + 1);
                    logger.info(retryMsg);
                    captureFailure(context, "testCreateBulkLeadsOutput", retryMsg);
                }
            }

            if (!success) {
                String failMsg = String.format("❌ Lead #%d failed after 3 attempts. Skipping to next index.", workingIndex);
                logger.error(failMsg);
                captureFailure(context, "testCreateBulkLeadsOutput", failMsg);
                throw new SkipException(failMsg);
            }

            currentIndex = workingIndex;
        }
    }

    /**
     * ✅ Creates CSV report safely under environment-independent folder.
     */
    public String generateLeadCreationCSV(String environment, String customerType) {
        try {
            String baseDir = getReportsBaseDir();
            String subFolder = baseDir + "/LeadCreationReports/" + environment + "/" + formatCurrentDate("📅dd-MM-yyyy↘️");
            Files.createDirectories(Paths.get(subFolder));

            String fileName = customerType + "-Leads_LastAt_" + propertiesReader.getLastProcessedIndex() + ".csv";
            String filePath = subFolder + "/" + fileName;

            String[] headers = {"S.no", "customer_Id", "projectID", "designerEmail", "designerId"};
            utilities.createCSVReport(headers, filePath);

            return filePath;
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to create CSV report: " + e.getMessage(), e);
        }
    }

    private boolean leadInputValidation() {
        boolean isValid = true;
        int lastProcessedLeadIndex = propertiesReader.getLastProcessedIndex();
        System.out.println(propertiesReader.getFiledDate());

        if (isBeforeToday(propertiesReader.getFiledDate())) {
            lastProcessedLeadIndex = 1;
        }

        if ((propertiesReader.getLeadCount() + propertiesReader.getLastProcessedIndex()) >= 100) {
            logger.error("❌ TotalLeadsToCreate should be less than 100. Please provide different mobileStarting prefix.");
            isValid = false;
        }

        try {
            int prefix = Integer.parseInt(propertiesReader.getMobilePrefix());
            if (prefix < 59 || prefix > 99) {
                logger.error("❌ Invalid mobile number prefix. It must be between 60-99. Found: {}", propertiesReader.getMobilePrefix());
                isValid = false;
            }
        } catch (NumberFormatException e) {
            logger.error("❌ Mobile number prefix is not a valid number: {}", propertiesReader.getMobilePrefix());
            isValid = false;
        }

        currentIndex = lastProcessedLeadIndex + 1;
        return isValid;
    }

    @AfterClass
    public void afterClassTasks() {
        Map<String, String> updateUserProperty = new HashMap<>();
        updateUserProperty.put("lastProcessedLeadIndex", String.valueOf(--currentIndex));
        updateUserProperty.put("leadScriptRunDate", formatCurrentDate("dd-MM-yyyy"));
        updateProperties("userConfigurations.properties", updateUserProperty);

        if (filepath != null && !filepath.isBlank()) {
            String finalFileName = propertiesReader.getLeadCount() + "_" + customerType + "-Leads_LastAt_" + currentIndex + formatCurrentDate(" ⏰ hh.mm.a") + ".csv";
            utilities.renamingLeadReportFile(filepath, finalFileName);
        } else {
            System.err.println("⚠️ No report file generated — skipping rename.");
        }
    }
}
