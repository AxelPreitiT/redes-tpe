const { Builder } = require("selenium-webdriver");

test("new next app", async () => {
    let driver;

    try {
        driver = await new Builder().forBrowser('chrome').build();
        await driver.get('http://localhost:3000');

        const title = await driver.getTitle();
        expect(title).toBe('Create Next App');
    } catch (e) {
        console.log(e);
    } finally {
        await driver.quit();
    }
}, 20000);