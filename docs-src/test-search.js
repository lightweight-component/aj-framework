const assert = require('node:assert/strict');
const { indexPage } = require('./lib/search-index');
const fixture = '<html lang="zh-CN"><nav>noise</nav><article data-search-document><p>前言</p>'
    + '<h1>文档</h1><p>说明</p><h2>配置</h2><p>Configuration</p><h2>配置</h2><p>更多</p>'
    + '<h3 id="custom">已有锚点</h3></article><footer>noise</footer></html>';
const indexed = indexPage(fixture, '/', './src/index.md');
assert.equal(indexed.entries.length, 5);
assert.ok(indexed.entries.every(entry => entry.project === 'aj-framework' && entry.language === 'zh'));
assert.ok(indexed.html.includes('id="search-配置-2"'));
assert.ok(indexed.entries.some(entry => entry.url.endsWith('#custom')));
assert.ok(!indexed.entries.some(entry => entry.text.includes('noise')));
assert.equal(indexPage(fixture.replace('zh-CN', 'en'), '/guide/', 'src\\guide.md').entries[0].language, 'en');
for (const source of ['src/temp.md', 'src/todo.md', 'src/home.html', 'other/guide.md'])
    assert.deepEqual(indexPage(fixture, '/', source).entries, []);
assert.deepEqual(indexPage(fixture.replace('data-search-document', 'class="card"'), '/', 'src/index.md').entries, []);
console.log('aj-framework: document marker/source boundary, language and heading anchors verified.');
