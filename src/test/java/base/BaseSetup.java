package base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

public class BaseSetup {

    protected static ExtentReports reportManager;
    protected static ExtentTest report;
    protected static final Logger log = LogManager.getLogger(BaseSetup.class);
    protected static RequestSpecification baseRequestSpec;

    private static final String BASE_URL = "https://fakerestapi.azurewebsites.net";
    private static final String REPORT_OUTPUT = "reports/TestExecutionReport.html";

    @BeforeSuite
    public void initializeReporting() {
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(REPORT_OUTPUT);
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Public API - Test Execution Report");
        sparkReporter.config().setReportName("Automated API Test Results");

        reportManager = new ExtentReports();
        reportManager.attachReporter(sparkReporter);
        reportManager.setSystemInfo("Tester", "Ifiok-obong Robertson Akpan");
        reportManager.setSystemInfo("Project", "Public API Test Automation");
        reportManager.setSystemInfo("Environment", "Production");
        reportManager.setSystemInfo("Java Version", System.getProperty("java.version"));

        log.info("ExtentReports initialized successfully");
    }

    @BeforeClass
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URL;

        baseRequestSpec = new RequestSpecBuilder()
                .setContentType("application/json")
                .setAccept("application/json")
                .build();

        log.info("REST Assured configured with base URI: {}", BASE_URL);
    }

    @AfterSuite
    public void finalizeReporting() {
        if (reportManager != null) {
            reportManager.flush();
            log.info("Test report generated at: {}", REPORT_OUTPUT);
        }
    }
}
