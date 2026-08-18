package pages; // Укажи пакет страницы

import org.openqa.selenium.By; // Подключаем локатор
import org.openqa.selenium.WebDriver; // Подключаем управление браузером
import org.openqa.selenium.WebElement; // Подключаем веб-элемент
import org.openqa.selenium.support.ui.ExpectedConditions; // Подключаем ожидаемые условия
import org.openqa.selenium.support.ui.WebDriverWait; // Подключаем явные ожидания

import java.util.Set;

public class ResultsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ResultsPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    private By documentLink(String title) {
        return By.xpath(
                "//a[contains(normalize-space(.), " + toXPathLiteral(title) + ")]"
        );
    }

    public void waitForDocumentLink(String title) {
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(documentLink(title))
        );
    }

    public void openDocumentByTitle(String title) {
        Set<String> oldWindows = driver.getWindowHandles();
        WebElement link = wait.until(
                ExpectedConditions.elementToBeClickable(documentLink(title))
        );
        link.click();

        wait.until(webDriver ->
                webDriver.getWindowHandles().size() > oldWindows.size()
                        || !webDriver.getCurrentUrl().contains("/search/")
        );

        for (String window : driver.getWindowHandles()) {
            if (!oldWindows.contains(window)) {
                driver.switchTo().window(window);
                return;
            }
        }
    }

    private String toXPathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        return "concat('"
                + value.replace("'", "', \"'\", '")
                + "')";
    }
}