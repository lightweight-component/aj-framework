Powered by [Eleventy](https://www.11ty.dev/)

This is a Node.js project, type `npm install` to install dependencies, and type `npm run dev` to start the development server.

Easy to write HTML content in this environment, just pure HTML/CSS/JS is enough, plus Markdown to HTML is supported.

## Less.js support
Install https://www.npmjs.com/package/eleventy-plugin-less

## Static documentation search

The document layouts load the shared browser resources directly:

- `https://aj-util.ajaxjs.com/asset/js/static-search.js`
- `https://aj-util.ajaxjs.com/asset/static-search.css`

No reusable component source is copied here. Local `src/asset/search.js` configures the site's language/index and expands mobile navigation; `src/asset/search.css` integrates the existing layout and theme.

`lib/search-index.js` and `eleventy.config.js` extract sections only from Markdown pages rendered by layouts with an explicit `data-search-document` article marker. Navigation/footer text, unmarked website/marketing pages, and `temp.md` / `todo.md` drafts are excluded. This site currently has Chinese documentation only; the English index is intentionally empty. Existing heading IDs are preserved; missing IDs are generated together with their result links.

Run from `docs-src`:

```sh
npm test
npm run build
```

The tests verify document boundaries, language classification, resource load order and every generated result anchor. Normal builds output `dist/search/en.json` and `dist/search/zh.json` alongside the pages and local adapter/styles; test output uses `.test-dist` and is removed afterward. Each site fetches its own indexes, not the shared component host's indexes. Edit source documents, not generated JSON.

For optional real-browser verification after building, run `node test-search-browser.js`. It needs network access to the shared resources, Playwright and a browser. `AJ_DOCS_PLAYWRIGHT` and `AJ_DOCS_BROWSER` can override the module/browser locations. The test checks real shared JS/CSS, language UI, local index requests, highlighting, keyboard and section navigation, mobile/dark display and failed-index retry.

Publish pages, local adapter/styles and `search/` together when deploying. A local build does not publish the site. Use HTTP/HTTPS, not direct `file://` viewing. If the shared component is unavailable, the adapter shows a localized message instead of breaking the page.
