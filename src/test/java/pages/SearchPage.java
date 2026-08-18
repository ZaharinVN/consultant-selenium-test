package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public SearchPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void enterQuery(String query) {
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                org.openqa.selenium.By.cssSelector("input.x-search-box__input")
        ));
        searchInput.clear();
        searchInput.sendKeys(query);
    }

    public void clickFind() {
        WebElement findButton = wait.until(ExpectedConditions.elementToBeClickable(
                org.openqa.selenium.By.xpath("//button[normalize-space(.)='Найти']")
        ));
        findButton.click();
    }

    public void waitForResults() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                org.openqa.selenium.By.xpath("//a[contains(., 'Налоговый')]")
        ));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}