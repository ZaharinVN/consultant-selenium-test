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
import utils.AllureReporter;
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

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, TIMEOUT);

        homePage = new HomePage(driver, wait);
        searchPage = new SearchPage(driver, wait);
        resultsPage = new ResultsPage(driver, wait);
        docPage = new DocumentPage(driver, wait);
    }

    @Test
    public void testConsultantHappyPath() {
        Allure.step(
                "Открытие главной страницы КонсультантПлюс",
                homePage::open
        );

        Allure.step(
                "Переход в Быстрый поиск",
                homePage::goToQuickSearch
        );

        Allure.step("Ввод запроса и поиск", () -> {
            searchPage.enterQuery(Config.SEARCH_QUERY);
            searchPage.clickFind();
            searchPage.waitForResults();
        });

        Allure.step(
                "Ожидание ссылки на часть вторую НК РФ",
                () -> resultsPage.waitForDocumentLink(Config.TARGET_DOC_TITLE)
        );

        Allure.step(
                "Открытие части второй НК РФ",
                () -> resultsPage.openDocumentByTitle(Config.TARGET_DOC_TITLE)
        );

        Allure.step(
                "Ожидание загрузки страницы документа",
                docPage::waitForDocumentLoaded
        );

        Allure.step(
                "Поиск фразы «абзаце первом пункта 1» в документе",
                () -> docPage.findTextInDocument("абзаце первом пункта 1")
        );

        Allure.step(
                "Переход по ссылке «абзаце первом пункта 1»",
                docPage::clickParagraph1Reference
        );

        Allure.step(
                "Проверка перехода к абзацу по ссылке",
                docPage::waitForReferenceNavigation
        );

        Allure.step("Добавление выделенного абзаца в отчёт", () -> {
            String paragraphText = docPage.getReferencedParagraphText();

            Assert.assertFalse(
                    paragraphText.isBlank(),
                    "Не удалось получить текст абзаца после перехода по ссылке"
            );

            AllureReporter.attachText(
                    "Выделенный абзац",
                    paragraphText
            );
        });

    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver != null && !result.isSuccess()) {
            takeFailureScreenshot(result.getMethod().getMethodName());
        }

        if (driver != null) {
            driver.quit();
        }
    }

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
}