const puppeteer = require('puppeteer');

(async () => {
  const browser = await puppeteer.launch({devtools: true});
  const page = await browser.newPage();

  page.on('console', msg => console.log('PAGE LOG:', msg.text()));

  await page.goto('http://localhost:9500');

  await Promise.all([
    page.waitForNavigation(), // The promise resolves after navigation has finished
    page.click('#sign-in'), // Clicking the link will indirectly cause a navigation
  ]);

  await page.type('[name=username]', process.env.PTESTING_USERNAME);
  await page.type('[name=password]', process.env.PTESTING_PASSWORD);

  let [resp1, resp2] = await Promise.all([
    page.waitForNavigation(),
    page.click('[name=signInSubmitButton]'), 
  ]);

  const [ returnedCookie ] = await page.cookies()
  console.log('cookie', returnedCookie);

  let content = await page.content();
  console.log(content);

  await page.waitForSelector('#account-button');

  await Promise.all([
    page.waitForSelector('#artist-profile'),
    page.click('#account-button'), 
  ]);

  await Promise.all([
    page.waitForNavigation(),
    page.click('#artist-profile'), 
  ]);

  el = await page.$('#email');
  console.log('email', el);
  
  // page.close();
  browser.close();

})();