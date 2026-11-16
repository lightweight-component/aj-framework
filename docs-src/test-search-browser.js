// Optional browser test: build first; requires network access to the real shared component.
const profile = {"name":"aj-framework","layouts":{"aj-docs.njk":"zh"},"zh":"/","zhQuery":"框架"};

const assert = require('node:assert/strict');
const fs = require('node:fs/promises');
const http = require('node:http');
const path = require('node:path');
const os = require('node:os');
const { chromium } = require(process.env.AJ_DOCS_PLAYWRIGHT || 'playwright');

async function main() {
    const output = path.resolve(__dirname, 'dist');
    const types = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript', '.json': 'application/json', '.css': 'text/css' };
    const server = http.createServer(async (request, response) => {
        try {
            const url = new URL(request.url, 'http://localhost');
            const file = path.resolve(output, '.' + decodeURIComponent(url.pathname), url.pathname.endsWith('/') ? 'index.html' : '');
            if (!file.startsWith(output + path.sep)) {
                response.writeHead(403).end();
                return;
            }
            response.setHeader('Content-Type', types[path.extname(file)] || 'application/octet-stream');
            response.end(await fs.readFile(file));
        } catch (_) {
            response.writeHead(404).end();
        }
    });
    await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
    let browser;
    try {
        browser = await chromium.launch(process.env.AJ_DOCS_BROWSER ? { executablePath: process.env.AJ_DOCS_BROWSER } : {});
        const page = await browser.newPage({ viewport: { width: 1280, height: 900 }, locale: 'en-US', ignoreHTTPSErrors: true });
        const base = 'http://127.0.0.1:' + server.address().port;
        const shared = 'https://aj-util.ajaxjs.com';
        const loaded = new Set();
        const indexes = [];
        const errors = [];
        page.on('pageerror', error => errors.push(error.message));
        page.on('dialog', dialog => dialog.dismiss());
        page.on('response', response => { if (response.ok()) loaded.add(response.url()); });
        page.on('request', request => {
            if (/\/search\/(zh|en)\.json/.test(request.url()))
                indexes.push(request.url());
        });
        await page.route('**/*', route => {
            const origin = new URL(route.request().url()).origin;
            return [base, shared, 'https://framework.ajaxjs.com', 'https://iam.ajaxjs.com'].includes(origin)
                ? route.continue() : route.abort();
        });
        await page.goto(base + profile.zh, { waitUntil: 'domcontentloaded', timeout: 60000 });
        const input = page.locator('.ss-input');
        const links = page.locator('.ss-results a');
        await input.fill(profile.zhQuery);
        await links.first().waitFor();
        assert.ok(await page.locator('.ss-mark').count());
        assert.ok((await links.first().getAttribute('href')).startsWith(base + '/'));
        await input.press('Enter');
        await page.waitForFunction(() => document.activeElement.matches('.ss-link'));
        await page.keyboard.press('Enter');
        await page.waitForURL(url => Boolean(url.hash));
        assert.ok(await page.evaluate(() => Boolean(document.getElementById(decodeURIComponent(location.hash.slice(1))))));
        await input.press('Escape');
        assert.equal(await input.inputValue(), '');
        assert.ok(await page.locator('.ss-results').isHidden());
        if (profile.en) {
            await page.goto(base + profile.en, { waitUntil: 'domcontentloaded', timeout: 60000 });
            await input.fill(profile.enQuery);
            await links.first().waitFor();
            assert.ok((await page.locator('.ss-status').textContent()).includes('results'));
        }

        await page.setViewportSize({ width: 390, height: 844 });
        await page.goto(base + profile.zh, { waitUntil: 'domcontentloaded', timeout: 60000 });
        await input.fill(profile.zhQuery);
        await links.first().waitFor();
        await page.evaluate(() => {
            if (window.AJUtils)
                AJUtils.applyTheme('dark', false);
            else
                document.documentElement.dataset.theme = 'dark';
        });
        assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), 'mobile page overflows horizontally');
        assert.notEqual(await page.locator('.ss-search').evaluate(element => getComputedStyle(element).backgroundColor),
            'rgba(0, 0, 0, 0)', 'dark search text needs an opaque dark background even on legacy light-only sites');
        assert.ok(await page.evaluate(() => {
            const menu = document.querySelector('.docs-navigation');
            return menu.clientHeight >= menu.scrollHeight;
        }), 'mobile results are clipped');
        assert.ok(await page.evaluate(() => document.querySelector('nav h1').getBoundingClientRect().bottom
            <= document.querySelector('.docs-navigation').getBoundingClientRect().top), 'mobile heading overlaps search');
        const screenshots = await fs.mkdtemp(path.join(os.tmpdir(), profile.name + '-search-'));
        await page.screenshot({ path: path.join(screenshots, 'mobile.png'), fullPage: true });

        await page.goto(base + profile.zh, { waitUntil: 'domcontentloaded', timeout: 60000 });
        await page.route('**/search/zh.json', route => route.abort());
        await input.fill(profile.zhQuery);
        await page.waitForFunction(() => document.querySelector('.ss-status').textContent.includes('失败'));
        await page.unroute('**/search/zh.json');
        await input.press('Enter');
        await links.first().waitFor();
        assert.ok(loaded.has(shared + '/asset/js/static-search.js'));
        assert.ok(loaded.has(shared + '/asset/static-search.css'));
        assert.ok(indexes.includes(base + '/search/zh.json'));
        if (profile.en)
            assert.ok(indexes.includes(base + '/search/en.json'));
        assert.ok(indexes.every(url => url.startsWith(base + '/search/')), 'index must belong to this site');
        assert.deepEqual(errors, []);
        console.log(profile.name + ': real shared resources, local indexes, language UI, highlights, keyboard/anchor navigation, mobile/dark display and failure retry verified.');
        console.log('Screenshot: ' + path.join(screenshots, 'mobile.png'));
    } finally {
        if (browser)
            await browser.close();
        await new Promise(resolve => server.close(resolve));
    }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
