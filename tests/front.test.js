const { Builder } = require("selenium-webdriver");
const { Options } = require("selenium-webdriver/chrome") 

test("new next app", async () => {
    let driver;
    let options = new Options();
    options.addArguments('--headless','--no-sandbox');
    try {
        driver = await new Builder().forBrowser('chrome').setChromeOptions(options).build();
        await driver.get('http://localhost:3000');

        const title = await driver.getTitle();
        expect(title).toBe('Jenkins demo');
    } catch (e) {
        console.log(e);
    } finally {
        await driver.quit();
    }
}, 20000);