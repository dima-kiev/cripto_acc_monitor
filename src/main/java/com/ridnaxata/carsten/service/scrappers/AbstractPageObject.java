package com.ridnaxata.carsten.service.scrappers;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// todo !!!! new instance for each scrapping request
// to avoid multiThreading problems
@Component
public abstract class AbstractPageObject implements PageObject {

    private WebDriver drv;
    private Wait<WebDriver> wait;

    public AbstractPageObject() {
        ChromeOptions options = new ChromeOptions();
        //options.setHeadless(true); //todo -> props
        options.addArguments("--disable-gpu");
        drv = new ChromeDriver(options);

        wait = new WebDriverWait(drv,5);
    }

    protected void openPage(String url) {
        drv.get(url);
    }

    protected void checkPage(String xpath, String expected) {
        wait.until(ExpectedConditions.textToBePresentInElement(drv.findElement(By.xpath(xpath)), expected));
    }

    protected boolean tryToClick(String xPath) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOf(drv.findElement(By.xpath(xPath))));
            if (!el.getAttribute("class").contains("disabled")) {
                el.click();
                return true;
            }
        } catch (Exception e) {
            // suppress. ok here.
        }
        return false;
    }

    protected void sleep(long millies) {
        try {
            Thread.sleep(millies);
        } catch (InterruptedException e){
            // suppress. ok here.
        }
    }

    protected void executeScript(String script) {
        JavascriptExecutor executor = (JavascriptExecutor) drv;
        executor.executeScript(script);
    }

    protected void clickElement(String xPath) {
        drv.findElement(By.xpath(xPath)).click();
    }

    protected List<WebElement> scrapTransactionContainers(String xpath) {
        return drv.findElements(By.xpath(xpath));
    }

    protected Map<String, String> readBlock(String blockUrl, Map<String, String> fieldSelectors) {
        drv.get(blockUrl);
        Map<String, String> blocks = new HashMap<>(fieldSelectors.size());
        fieldSelectors
            .keySet()
            .forEach(field -> {
                    WebElement el = wait.until(ExpectedConditions.visibilityOf(drv.findElement(By.xpath(fieldSelectors.get(field)))));
                    blocks.put(field, el.getText());
            });
        return blocks;
    }


}
