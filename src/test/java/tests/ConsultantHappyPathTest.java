package tests;

import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.DocumentPage;
import pages.HomePage;
import pages.ResultsPage;
import pages.SearchPage;
import utils.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;

@Listeners(AllureTestNg.class)
public class ConsultantHappyPathTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(25);

    private WebDriver driver;
    private WebDriverWait wait;

    private HomePage homePage;
    private SearchPage searchPage;
    private ResultsPage resultsPage;
    private DocumentPage docPage;

    // 1. Открыть браузер Chrome на странице http://www.consultant.ru/cons/
    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        // Единое явное ожидание для всех Page Object.
        wait = new WebDriverWait(driver, TIMEOUT);

        homePage = new HomePage(driver, wait);
        searchPage = new SearchPage(driver, wait);
        resultsPage = new ResultsPage(driver, wait);
        docPage = new DocumentPage(driver, wait);
    }

    @Test
    public void testConsultantHappyPath() {

        // 1. Открыть браузер Chrome на странице http://www.consultant.ru/cons/
        Allure.step(
                "Открытие главной страницы КонсультантПлюс",
                homePage::open
        );

        // 2. Перейти в Быстрый поиск
        Allure.step(
                "Переход в Быстрый поиск",
                homePage::goToQuickSearch
        );

        // 3. Ввести «Налоговый часть» и нажать кнопку «Найти»
        Allure.step("Ввод запроса и поиск", () -> {
            searchPage.enterQuery(Config.SEARCH_QUERY);
            searchPage.clickFind();
            searchPage.waitForResults();
        });

        // 4. Открыть документ и сверить название с результатами поиска
        Allure.step(
                "Ожидание ссылки на часть вторую НК РФ",
                () -> resultsPage.waitForDocumentLink(Config.TARGET_DOC_TITLE)
        );

        // Название нужно сохранить до перехода со страницы результатов.
        String titleFromResults = Allure.step(
                "Получение названия документа из результатов поиска",
                () -> resultsPage.getDocumentTitleFromResults(
                        Config.TARGET_DOC_TITLE
                )
        );

        Allure.step(
                "Открытие части второй НК РФ",
                () -> {
                    resultsPage.openDocumentByTitle(Config.TARGET_DOC_TITLE);

                    // Пауза для визуальной отрисовки динамического документа.
                    waitForVisualRendering();
                }
        );

        Allure.step(
                "Ожидание загрузки страницы документа",
                docPage::waitForDocumentLoaded
        );

        Allure.step(
                "Проверка выбранного документа и поискового запроса",
                () -> {
                    String normalizedTitleFromResults = normalizeDocumentTitle(titleFromResults);

                    Assert.assertTrue(
                            normalizedTitleFromResults.startsWith(Config.TARGET_DOC_TITLE),
                            "В результатах поиска найден неверный документ: " +
                                    titleFromResults
                    );

                    String documentSearchQuery = docPage.getDocumentSearchQuery();

                    Assert.assertEquals(
                            documentSearchQuery,
                            Config.SEARCH_QUERY,
                            "В строке поиска документа отображается неверный запрос"
                    );

                    Allure.addAttachment(
                            "Проверка выбранного документа и поискового запроса",
                            "text/plain",
                            "Полное название выбранного документа: " + titleFromResults + "\n" +
                                    "Нормализованное название: " + normalizedTitleFromResults + "\n" +
                                    "Ожидаемое начало названия: " + Config.TARGET_DOC_TITLE + "\n" +
                                    "Запрос в строке поиска: " + documentSearchQuery,
                            ".txt"
                    );
                }
        );

        // 5. В статье 145, пункте 3 нажать ссылку «абзаце первом пункта 1»
        Allure.step(
                "Поиск фразы «абзаце первом пункта 1» в документе",
                () -> docPage.findTextInDocument("абзаце первом пункта 1")
        );

        Allure.step(
                "Переход по ссылке «абзаце первом пункта 1»",
                () -> {
                    docPage.clickParagraph1Reference();

                    // Ожидаем отрисовку целевого абзаца и жёлтой стрелки.
                    waitForVisualRendering();
                }
        );

        Allure.step(
                "Проверка перехода к абзацу по ссылке",
                docPage::waitForReferenceNavigation
        );

        // Ожидаемый результат
        Allure.step("Ожидаемый результат", () -> {
            String expectedResult = """
                    Жёлтая стрелка должна указывать на первый абзац пункта 1
                    статьи 145 Налогового кодекса Российской Федерации.
                    
                    Целевой абзац начинается с текста:
                    «1. Организации и индивидуальные предприниматели...»
                    """;

            Allure.addAttachment(
                    "Ожидаемый результат",
                    "text/plain",
                    expectedResult,
                    ".txt"
            );
        });

        // 6. Выделить указанный абзац и вывести его в отчёт
        Allure.step(
                "Скриншот пункта 1 статьи 145 после перехода по ссылке",
                () -> {
                    // Отдельная пауза делает вложение Allure визуально стабильным.
                    waitForVisualRendering();

                    byte[] screenshot = ((TakesScreenshot) driver)
                            .getScreenshotAs(OutputType.BYTES);

                    Allure.addAttachment(
                            "Пункт 1 статьи 145 НК РФ",
                            "image/png",
                            new java.io.ByteArrayInputStream(screenshot),
                            ".png"
                    );
                }
        );
    }

    // 7. Закрыть браузер
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver != null && !result.isSuccess()) {
            // На ошибке прикладываем отдельный скриншот для диагностики.
            takeFailureScreenshot(result.getMethod().getMethodName());
        }

        if (driver != null) {
            driver.quit();
        }
    }

    // Сохраняем скриншот ошибки в target/screenshots и прикладывает его к Allure-отчёту.
    private void takeFailureScreenshot(String testName) {
        try {
            File source = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.FILE);

            File screenshotsDirectory = new File("target/screenshots");

            if (!screenshotsDirectory.exists()
                    && !screenshotsDirectory.mkdirs()) {
                throw new IOException(
                        "Не удалось создать папку: "
                                + screenshotsDirectory.getAbsolutePath()
                );
            }

            File screenshot = new File(
                    screenshotsDirectory,
                    testName + "_" + System.currentTimeMillis() + ".png"
            );

            FileHandler.copy(source, screenshot);

            try (FileInputStream inputStream = new FileInputStream(screenshot)) {
                Allure.addAttachment(
                        "Скриншот при ошибке",
                        "image/png",
                        inputStream,
                        ".png"
                );
            }
        } catch (IOException exception) {
            System.err.println(
                    "Не удалось создать скриншот: "
                            + exception.getMessage()
            );
        }
    }

    private String normalizeDocumentTitle(String title) {
        return title
                .replace('\u00A0', ' ')
                .replace("\"", "")
                .replace("«", "")
                .replace("»", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // Даём динамическому интерфейсу КонсультантПлюс время на визуальную отрисовку.
    private void waitForVisualRendering() {
        try {
            Thread.sleep(3_000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Поток был прерван во время ожидания отрисовки страницы",
                    exception
            );
        }
    }
}