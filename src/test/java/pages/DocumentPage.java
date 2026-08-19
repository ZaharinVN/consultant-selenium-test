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

    // Строка поиска внутри открытого нормативного документа.
    private final By documentSearchInput = By.xpath(
            "//input[contains(@class, 'x-search-box__input') " +
                    "or contains(@class, 'x-page-components-search-panel__filter')]"
    );

    // Возвращаем текст, введённый во внутридокументную строку поиска.
    public String getDocumentSearchQuery() {
        return wait.until(
                        ExpectedConditions.visibilityOfElementLocated(documentSearchInput)
                ).getAttribute("value")
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public DocumentPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    // 4. Открыть документ «Налоговый кодекс Российской Федерации (часть вторая)»
    // Ожидаем появления поля внутридокументного поиска. Его видимость подтверждает, что страница документа открылась.
    public void waitForDocumentLoaded() {
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(documentSearchInput)
        );
    }

    // 5. В статье 145, пункте 3 нажать ссылку «абзаце первом пункта 1»
    // Выполняем поиск фразы во внутреннем поиске открытого документа.
    public void findTextInDocument(String text) {
        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(documentSearchInput)
        );

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(text);
        input.sendKeys(Keys.ENTER);

        try {
            // КонсультантПлюс асинхронно обновляет документ после внутридокументного поиска.
            Thread.sleep(2_000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Поток был прерван во время ожидания поиска",
                    exception
            );
        }
    }

    // Находим ссылку «абзаце первом пункта 1» и открываем её URL. Ссылка может быть расположена в основном DOM либо во вложенном фрейме.
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

    // Ищем целевую ссылку в текущем DOM-контексте и возвращаем её абсолютный URL.
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

    // Подтверждаем, что адрес страницы содержит параметры целевого фрагмента документа.
    public void waitForReferenceNavigation() {
        wait.until(webDriver ->
                webDriver.getCurrentUrl().contains("dst=15434")
                        || webDriver.getCurrentUrl().contains("field=134")
        );
    }
}