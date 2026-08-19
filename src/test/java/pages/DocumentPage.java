package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class DocumentPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By documentSearchInput = By.xpath(
            "//input[contains(@class, 'x-search-box__input') " +
                    "or contains(@class, 'x-page-components-search-panel__filter')]"
    );

    public DocumentPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void waitForDocumentLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                documentSearchInput
        ));
    }

    public void findTextInDocument(String text) {
        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(documentSearchInput)
        );

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(text);

        System.out.println("Текст в поле до Enter: " + input.getAttribute("value"));

        input.sendKeys(Keys.ENTER);

        try {
            Thread.sleep(2_000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Поток был прерван во время ожидания поиска",
                    exception
            );
        }
    }

    public void clickParagraph1Reference() {
        driver.switchTo().defaultContent();

        String targetUrl = findTargetLinkUrlInCurrentContext();

        if (targetUrl == null) {
            List<WebElement> frames = driver.findElements(
                    By.cssSelector("iframe, frame")
            );

            for (WebElement frame : frames) {
                try {
                    driver.switchTo().defaultContent();
                    driver.switchTo().frame(frame);

                    targetUrl = findTargetLinkUrlInCurrentContext();

                    if (targetUrl != null) {
                        break;
                    }
                } catch (RuntimeException ignored) {
                    targetUrl = null;
                } finally {
                    driver.switchTo().defaultContent();
                }
            }
        }

        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IllegalStateException(
                    "Не найдена ссылка «абзаце первом пункта 1» " +
                            "ни в основном документе, ни во вложенных iframe"
            );
        }

        driver.navigate().to(targetUrl);
    }

    private String findTargetLinkUrlInCurrentContext() {
        return (String) ((JavascriptExecutor) driver).executeScript(
                """
                        const targetText = 'абзаце первом пункта 1';
                        
                        const normalize = value => (value || '')
                            .replace(/\\u00A0/g, ' ')
                            .replace(/\\s+/g, ' ')
                            .trim()
                            .toLowerCase();
                        
                        const link = [...document.querySelectorAll('a[nb="LAW"]')]
                            .find(item => normalize(item.textContent) === targetText);
                        
                        return link ? link.href : null;
                        """
        );
    }

    public void waitForReferenceNavigation() {
        wait.until(webDriver ->
                webDriver.getCurrentUrl().contains("dst=15434")
                        || webDriver.getCurrentUrl().contains("field=134")
        );
    }

}