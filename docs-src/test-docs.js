const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const output = path.join(__dirname, '.test-dist');
const layouts = {"aj-docs.njk":"zh"};
const expected = { en: 0, zh: 0 };
function visit(directory) {
    for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
        const file = path.join(directory, entry.name);
        if (entry.isDirectory())
            visit(file);
        else if (entry.name.endsWith('.md') && !/^(temp|todo)\.md$/i.test(entry.name)) {
            const match = /^layout:\s*layouts\/([^\r\n ]+)/m.exec(fs.readFileSync(file, 'utf8'));
            if (match && layouts[match[1]])
                expected[layouts[match[1]]]++;
        }
    }
}
visit(path.join(__dirname, 'src'));
try {
    for (const language of ['en', 'zh']) {
        const index = JSON.parse(fs.readFileSync(path.join(output, 'search', language + '.json'), 'utf8'));
        assert.equal(index.version, 1);
        const pages = new Set();
        for (const entry of index.entries) {
            assert.equal(entry.project, 'aj-framework');
            assert.equal(entry.language, language);
            const [url, anchor] = entry.url.split('#');
            assert.ok(!/^\/(temp|todo)\//.test(url));
            pages.add(url);
            const html = fs.readFileSync(path.join(output, url, url.endsWith('/') ? 'index.html' : ''), 'utf8');
            if (anchor)
                assert.ok(html.includes('id="' + decodeURIComponent(anchor) + '"'), 'missing anchor ' + entry.url);
            assert.ok(html.includes('data-site-search data-language="' + language + '"'));
            const shared = 'src="https://aj-util.ajaxjs.com/asset/js/static-search.js"';
            assert.ok(html.includes(shared));
            assert.ok(html.includes('href="https://aj-util.ajaxjs.com/asset/static-search.css"'));
            assert.ok(html.indexOf(shared) < html.indexOf('src="/asset/search.js"'));
        }
        assert.equal(pages.size, expected[language], 'every document must enter its layout language index');
        console.log(language + ': ' + pages.size + ' pages, ' + index.entries.length + ' sections verified.');
    }
    assert.ok(fs.existsSync(path.join(output, 'asset/search.js')));
    assert.ok(fs.existsSync(path.join(output, 'asset/search.css')));
} finally {
    fs.rmSync(output, { recursive: true, force: true });
}
