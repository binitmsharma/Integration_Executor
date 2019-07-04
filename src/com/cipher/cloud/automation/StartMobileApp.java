package com.cipher.cloud.automation;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.touch.ScrollAction;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class StartMobileApp {

	public static void main(String[] args) throws MalformedURLException, InterruptedException {
		

		System.setProperty("webdriver.chrome.driver", "C://chromedriver.exe");

		DesiredCapabilities capabilities = new DesiredCapabilities();
		File app= new File("C:\\Selenium\\com.salesforce.chatter.apk");
		//capabilities.setCapability("deviceName", "080b7e41006991b3");
		
		//note 3
		capabilities.setCapability("deviceName", "34044a1651d22123");
		capabilities.setCapability(CapabilityType.BROWSER_NAME, ""); //Name of mobile web browser to automate. Should be an empty string if automating an app instead.
		capabilities.setCapability(CapabilityType.VERSION, "5.0");
		capabilities.setCapability("platformName", "Android");
		capabilities.setCapability("newCommandTimeout","240");
		capabilities.setCapability("app",app.getAbsolutePath());
		capabilities.setCapability("app-package", "com.salesforce.chatter"); //Replace with your app's package
		capabilities.setCapability("app-activity", "salesfore.chatter.main"); //Replace with app's Activity
		RemoteWebDriver driver = new RemoteWebDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);
		// driver.wait(60);

		//driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		Thread.sleep(5000);
		//driver.findElement(By.id("com.salesforce.chatter:id/mi_accept")).click();
		Thread.sleep(5000);
		driver.findElement(By.id("com.salesforce.chatter:id/log_in")).click();
		Thread.sleep(20000);
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);


		driver.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);

		driver.findElementByXPath("//android.view.View[@content-desc='Username']").sendKeys("binitmsharma@accenture.com");
		driver.findElementByXPath("//android.view.View[@content-desc='Password']").sendKeys("binmoh1266");
		driver.findElement(By.xpath("//android.widget.Button[@content-desc='Log in to Salesforce']")).click();
		Thread.sleep(5000);
		//----- driver.findElement(By.xpath("//android.widget.Button[@content-desc='Email me a verification code']")).click();

		driver.findElement(By.xpath("//android.widget.Button[@content-desc='Allow']")).click();
		Thread.sleep(5000);
		//driver.findElement(By.xpath("//android.view.View[@content-desc='Skip']")).click();
		driver.findElement(By.id("android:id/home")).click();
		//driver.findElement(By.xpath("//android.widget.RelativeLayout[@index='8']")).click();
		//------driver.findElement(By.id("com.salesforce.chatter:id/standard_title")).click();
		Thread.sleep(5000);
		//driver.findElement(By.xpath("//android.widget.TextView[@text='Accounts']")).click();
		
		// ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		
		
		driver.findElement(By.xpath("//android.widget.TextView[@text='Log Out']")).click();
		driver.findElement(By.xpath("//android.widget.TextView[@text='Yes']")).click();
		
		//UiSelector us =new UiSelector().text();
		// Thread.sleep(5000);
		//driver.findElement(By.xpath("//android.widget.RelativeLayout[8]/android.widget.TextView[@text='Accounts']")).click();
		Thread.sleep(5000);
		//driver.findElement(By.id("com.salesforce.chatter:id/new_button")).click();
		//Thread.sleep(65000);
	//	driver.findElement(By.xpath("//android.widget.EditText[@content-desc='Account Name*'] ")).sendKeys("Automation01");
		//driver.findElement(By.xpath("//android.view.View[@content-desc='Account Name*']/android.widget.EditText[@index='1']")).sendKeys("Automation01");

	//	driver.findElement(By.xpath("//android.widget.Button[@content-desc='Save']")).click();






		//driver.findElement(By.id("com.salesforce.chatter:id/sign_up")).click();

		//RemoteWebDriver driver = new RemoteWebDriver(new URL("http://localhost:4723/wd/hub"),caps);
	}

}
