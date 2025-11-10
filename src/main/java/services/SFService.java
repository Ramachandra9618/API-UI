package services;

import configurator.UIBaseClass;
import pageObject.SFHomePage;
import pageObject.SFLoginPage;
import utils.PropertiesReader;

import static utils.PropertiesReader.*;
import static utils.Utilities.*;

public class SFService extends UIBaseClass {


    public String createLeadInSalesforce(int count) throws InterruptedException {
        PropertiesReader propertiesReader = new PropertiesReader(testData);
        String formattedCount  = String.format("%02d", count);
        String mobileNumber = propertiesReader.getMobilePrefix() +formatCurrentDate("ddMMyy")+formattedCount;
        String email = (propertiesReader.getCustomerName()).replaceAll("\\s+", "") + mobileNumber + propertiesReader.getGmailDomain();
        // Launch SF app
        initializeDriver();
        driver.get(propertiesReader.getSf_url());
        driver.manage().window().maximize();

        // Page objects
        SFLoginPage SFloginPage = new SFLoginPage(driver);
        SFHomePage homePage = new SFHomePage(driver);

        // Login
        SFloginPage.enterUsername(propertiesReader.getSf_login_email());
        SFloginPage.enterPassword(propertiesReader.getSf_login_password());
        SFloginPage.clickLoginButton();

        // Lead creation flow
        homePage.clickLeadsTab();
        homePage.clickLeadsTabDropdown();
        homePage.clickDCSalesManagerRadioButton();
        homePage.clickNextButton();

        homePage.clickMeetingTypeDropdown();
        homePage.selectDropdownOptionByText("EC");

        homePage.clickMeetingVenueDropdown();
        homePage.selectDropdownOptionByText(propertiesReader.getAppointment_venue());

        homePage.clickApproxBudgetDropdown();
        homePage.selectDropdownOptionByText("4L to 6L");

        homePage.enterMeetingDate();
        homePage.enterClientBudget("500000");
        homePage.clickDesignUserDropdown(propertiesReader.getDesign_User());

        homePage.enterCity((String) testData.get("property_city"));
        homePage.enterLastName(customerNameFormatter(propertiesReader.getCustomerName(), count,propertiesReader.getEnvironment(),propertiesReader.getLeadCount()));
        homePage.enterEmail(email);
        homePage.enterMobilePhone(mobileNumber);

        homePage.clickViewAllDependenciesButton();
        homePage.clickChannelDropdown();
        homePage.clickSource();
        homePage.clickCampaignSource();
        homePage.clickLeadSource();
        homePage.clickApplyButton();

        Thread.sleep(2000);

        homePage.clickSaveButton();
//        homePage.clickOnDetailsTab();
        Thread.sleep(20000); // Required wait for UI stabilization
//        // Get Roaster User ID from SF page link
//        String userId = homePage.clickRoasterLink();
        driver.quit();
        return mobileNumber;
    }

}
