package pageObject;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public class RoasterCustomerDetailsPage {
    WebDriver driver;
    WebDriverWait wait;
    public RoasterCustomerDetailsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(300));
    }

    @FindBy(xpath = "//button[contains(text(),'Launch Spacecraft')]")
    WebElement launchButtonInCustomerPage;
    @FindBy(id = "select0")WebElement bhkDropdown;
    @FindBy(xpath = "//input[@type='number' and @placeholder='Floor Area']")
    WebElement floorAreaInput;
    @FindBy(xpath = "//input[@placeholder='Move in Date']")
    WebElement moveInDateInput;
    @FindBy(xpath = "//input[@type='number' and @placeholder='Client Budget']")
    WebElement clientBudgetInput;
    @FindBy(xpath = "//input[@type='radio' and @value='Yes']")
    WebElement possessionOfTheSiteYesRadioBtn;
    @FindBy(xpath = "//input[@type='radio' and @value='Floor Plan Not Available']")
    WebElement floorPlanNotAvailableRadioBtn;
    @FindBy(xpath = "//button[@type='submit' and @class='false LaunchSpaceCraft_nextButton__f3Vfr']")
    WebElement nextButton;
    @FindBy(id = "googleInputField") WebElement googleInputField;
    @FindBy(xpath = "//input[@placeholder='Flat/House no' and @type='text']")
    WebElement flatHouseNoInput;
    @FindBy(xpath = "//input[@placeholder='Pincode' and @type='number']")
    WebElement pinCodeInput;
    @FindBy(xpath = "//div[contains(@class,'VerifyScCustomer_sendOtp')]//button[normalize-space()='SEND OTP']")
    WebElement sendOtpButton;
    @FindBy(xpath = "//label[span[normalize-space()='OTP']]/input")
    WebElement otpInputField;
    @FindBy(xpath = "//div[contains(@class,'VerifyScCustomer_popupFooter')]//button[normalize-space()='LAUNCH SPACECRAFT']")
    WebElement launchSpaceCraftButton;

    public void clickLauchButton() {
             pageLoad();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement launchButton = wait.until(ExpectedConditions.elementToBeClickable(
                launchButtonInCustomerPage));
        launchButton.click();
    }

    public void selectBHKOption() {
        pageLoad();
        Select select = new Select(bhkDropdown);
        select.selectByVisibleText("2 BHK");
        System.out.println( " selected from dropdown.");
    }

    public void enterFloorArea(String floorArea) {
        pageLoad();
        floorAreaInput.sendKeys(floorArea);
        System.out.println(floorArea + " is entered in the floor area field.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void selectDate(){
        moveInDateInput.click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("react-datepicker")));
        LocalDate date = LocalDate.now(); // selectedDay = 12 or 14
        int day = date.getDayOfMonth();
        WebElement targetDate = driver.findElement(By.xpath(
                "//div[contains(@class,'react-datepicker__day') and text()='" + day + "']"
        ));

        targetDate.click();
    }

    public void enterClientBudget(String budget) {
        pageLoad();
        clientBudgetInput.sendKeys(budget);
        System.out.println(budget + " is entered in the client budget field.");
    }

    public void selectPossessionOfTheSite(){
        if (!possessionOfTheSiteYesRadioBtn.isSelected()) {
            possessionOfTheSiteYesRadioBtn.click();
        }
    }

    public void selectFloorPlan(){
        if (!floorPlanNotAvailableRadioBtn.isSelected()) {
            floorPlanNotAvailableRadioBtn.click();
        }

    }
    public void clickNextButton() {
        nextButton.click();
        System.out.println("Next button clicked.");
    }

    public void enterPropertyDetails(String input) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            Actions actions = new Actions(driver);

            // Clear and type character-by-character
            googleInputField.clear();
            for (char ch : input.toCharArray()) {
                googleInputField.sendKeys(Character.toString(ch));
                Thread.sleep(100);
            }
pageLoad();
            // Wait for autocomplete container to appear
            List<WebElement> suggestions = wait.until(
                    ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".pac-item"))
            );

            System.out.println("Suggestions found: " + suggestions.size());

            // Loop through suggestions to find exact match
            for (WebElement suggestion : suggestions) {
                WebElement querySpan = suggestion.findElement(By.cssSelector(".pac-item-query"));
                String mainText = querySpan.getText().trim();
                String fullText = suggestion.getText().trim();

                System.out.println("Main query: " + mainText);
                System.out.println("Full suggestion text: " + fullText);

                if (fullText.contains("Test yantra") && fullText.contains("Jayanagar")) {
                    actions.moveToElement(suggestion).click().perform();
                    System.out.println("Clicked on matched suggestion: " + fullText);
                    break;
                }
            }

            pageLoad(); // Wait for result to load

        } catch (Exception e) {
            System.out.println("Error while entering property details: " + e.getMessage());
        }
    }

    public void enterFlatHouseNo(String flatHouseNo) {
        flatHouseNoInput.sendKeys(flatHouseNo);
        System.out.println(flatHouseNo + " is entered in the flat/house no field.");
    }
    public void enterPinCode(String pincode) {

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", pinCodeInput);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value="+pincode+";", pinCodeInput
        );

        System.out.println(pincode + " is entered in the pincode field.");
    }

    public void selectExperienceCentre(String centreName) {
        pageLoad();
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement inputFields = driver.findElement(By.xpath("(//input[@id='search_input'])[2]"));
                System.out.println();
                if (inputFields.isDisplayed()) {
                    js.executeScript("arguments[0].removeAttribute('disabled');", inputFields);
                    js.executeScript("arguments[0].focus();", inputFields);
                    js.executeScript("arguments[0].click();", inputFields);
            }
            Thread.sleep(10000);

                WebElement center = driver.findElement(By.xpath("//ul[@class='optionContainer']//li[normalize-space(text())='" + centreName + "']"));
              js.executeScript("arguments[0].scrollIntoView(true);", center);
                js.executeScript("arguments[0].click();", center);
                System.out.println(centreName + " is selected from the Experience Centre dropdown.");
        } catch (Exception e) {
            System.err.println("Error selecting Experience Centre: " + e.getMessage());
        }
    }

    public void clickSendOtpButton() {
        pageLoad();
            sendOtpButton.click();
            System.out.println("Send OTP button clicked.");
    }

    public void enterOtp(String otp) {
        pageLoad();
        otpInputField.sendKeys(otp);
        System.out.println(otp + " is entered in the OTP field.");
    }
    public void clickLaunchSpaceCraftButton() {
        pageLoad();
        launchSpaceCraftButton.click();
        System.out.println("Launch Spacecraft button clicked.");
    }






    public void pageLoad() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
