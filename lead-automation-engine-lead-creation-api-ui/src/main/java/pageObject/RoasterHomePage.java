package pageObject;

import configurator.UIBaseClass;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RoasterHomePage extends UIBaseClass {
    WebDriver driver;
    WebDriverWait wait;
    public RoasterHomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(300));
    }

    @FindBy(xpath = "//div[@class='LoginPage_inputBoxEmail__lsFdc']/input")
    WebElement emailField;
    @FindBy(xpath = "//div[@class='LoginPage_inputBoxPass__NpUen']/input")
    WebElement passwordField;
    @FindBy(xpath = "//div[@class='LoginPage_signIn__Z-tdz']")
    WebElement signInButton;


    public void enterEmail(String email) {
        pageLoad();
        emailField.sendKeys(email);
        System.out.println(email + " is entered in the email field.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void enterPassword(String password) {
        passwordField.sendKeys(password);
        System.out.println(password + " is entered in the password field.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void clickSignInButton() {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", signInButton);

    }

    public void pageLoad() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
