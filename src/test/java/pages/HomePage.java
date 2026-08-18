package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static utils.Config.BASE_URL;

public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By quickSearchButton = By.xpath(
            "//*[self::button or self::div or self::span]" +
                    "[contains(normalize-space(.), 'Быстрый поиск')]"
    );

    public HomePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void open() {
        driver.get(BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.tagName("body")
        ));
    }

    public void goToQuickSearch() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(quickSearchButton)
        );

        btn.click();
    }
}