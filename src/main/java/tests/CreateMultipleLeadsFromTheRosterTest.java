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
import java.util.HashMap;
import java.util.Map;

import static utils.Utilities.*;

public class CreateMultipleLeadsFromTheRosterTest extends BaseClass {
    private static final Logger logger = LogManager.getLogger(CreateMultipleLeadsFromTheRosterTest.class);
    RoasterService roasterService ;
    PropertiesReader propertiesReader;
    SFService sfService = new SFService();
    Map<String, String> projectDetails = new HashMap<>();
    Utilities utilities = new Utilities();
    String filepath;
    int currentIndex;

    @BeforeMethod
    public void setUp(){
        refreshTestData();
         propertiesReader = new PropertiesReader(testData);
        roasterService = new RoasterService();
        roasterService.initialize();
        if (!leadInputValidation()) {
            throw new RuntimeException("Lead input validation failed. Aborting test execution.");
        }
        System.out.println("lead input validation passed");
        filepath = generateLeadCreationCSV(propertiesReader.getEnvironment(), customerType);
    }

    @Test(enabled = true)
    public void testCreateBulkLeads(ITestContext context) {
        runLeadCreation(propertiesReader.getLeadCount(), customerType, propertiesReader.getEnvironment(), currentIndex,context);
    }

    private void runLeadCreation(int leadCount, String customerType, String environment, int currentIndex, ITestContext context) {
        String dpId = "745564";
        String dpEmail = "testorgstructuredp1@homelane.com";
        String dpName = "Test DP User";
        runLeadCreationBulk(leadCount, customerType, dpId, dpEmail, dpName, currentIndex, context);
    }

    private void runLeadCreationBulk(int leadCount, String customerType,
                                     String dpId, String dpEmail, String dpName,
                                     int workingIndex, ITestContext context) {

        int successfulLeads = 0;

        // Initial log
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

                        // Capture success message
                        String successMsg = String.format(
                                "✅ Lead #%d created successfully. CustomerId=%s, ProjectID=%s, DP Email=%s",
                                workingIndex,
                                projectDetails.get("customerId"),
                                projectDetails.get("projectID"),
                                projectDetails.get("dpEmail")
                        );
                        logger.info(successMsg);
                        captureOutput(context,"projectFullURL", projectDetails.get("fullProjectURL"));
                        captureOutput(context,"customerId",  projectDetails.get("customerId"));
                        captureOutput(context, "testCreateBulkLeadsOutput", successMsg);

                        // Store in ITestContext
                        context.setAttribute("LastSuccessfulLeadIndex", workingIndex);
                        // Append to CSV
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
                        String warnMsg = String.format(
                                "⚠️ Lead #%d creation failed on attempt %d: Incomplete project details.",
                                workingIndex, attempt
                        );
                        logger.warn(warnMsg);
                        captureFailure(context, "testCreateBulkLeadsFailures", warnMsg);


                    }

                } catch (Throwable t) {
                    String errorMsg = String.format(
                            "🛑 Lead #%d failed on attempt %d: %s",
                            workingIndex, attempt, t.getMessage()
                    );
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


    public String generateLeadCreationCSV(String environment, String customerType) {
        String[] headers = {"S.no", "customer_Id", "projectID", "designerEmail", "designerId"};
        String subFolder = "reports/LeadCreationReports/" + environment + "/" + formatCurrentDate("📅dd-MM-yyyy↘️");
        String fileName = customerType + "-Leads_LastAt_" +propertiesReader.getLastProcessedIndex()  + ".csv";
        String filePath = subFolder + "/" + fileName;
        utilities.createCSVReport(headers, filePath);
        return filePath;
    }


    private boolean leadInputValidation() {
        boolean isValid = true;
        int lastProcessedLeadIndex = propertiesReader.getLastProcessedIndex();
        System.out.println(propertiesReader.getFiledDate());
        if ( isBeforeToday(propertiesReader.getFiledDate())) {
            lastProcessedLeadIndex = 1;
        }
        System.out.println(lastProcessedLeadIndex);
        System.out.println(propertiesReader.getLeadCount());
        System.out.println(propertiesReader.getLeadCount() + propertiesReader.getLastProcessedIndex());
        System.out.println("jai sri ram");
        if ((propertiesReader.getLeadCount() + propertiesReader.getLastProcessedIndex()) >= 100) {
            logger.error("❌ TotalLeadsToCreate should be less than 100. Please provide different mobileStarting prefix and lastProcessedLeadIndex reset to 0 Found: {}", propertiesReader.getLeadCount() + lastProcessedLeadIndex);
            isValid = false;
        }
 System.out.println("jai sri ram-1");
        try {
            int prefix = Integer.parseInt(propertiesReader.getMobilePrefix());
            if (prefix < 59 || prefix > 99) {
                logger.error("❌ Invalid mobile number prefix. It must between 60-99. Found: {}", propertiesReader.getMobilePrefix());
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
        
        String finalFileName = propertiesReader.getLeadCount() + "_" + customerType + "-Leads_LastAt_" + currentIndex + formatCurrentDate(" ⏰ hh.mm.a") + ".csv";
        utilities.renamingLeadReportFile(filepath, finalFileName);
    }
}
