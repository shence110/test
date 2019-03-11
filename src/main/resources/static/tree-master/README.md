# Amaze UI Tree
---

[![NPM version](https://img.shields.io/npm/v/amazeui-tree.svg?style=flat-square)](https://www.npmjs.com/package/amazeui-tree)
[![Dependency Status](https://img.shields.io/david/amazeui/tree.svg?style=flat-square)](https://david-dm.org/amazeui/tree)
[![devDependency Status](https://img.shields.io/david/dev/amazeui/tree.svg?style=flat-square)](https://david-dm.org/amazeui/tree#info=devDependencies)

Amaze UI Tree 插件。

- [使用示例](http://amazeui.github.io/tree/docs/demo.html)
- [API 文档](http://amazeui.github.io/tree/docs/api.html)

**使用说明：**

1. 获取 Amaze UI Tree

  - [直接下载](https://github.com/amazeui/tree/archive/master.zip)
  - 使用 NPM: `npm install amazeui-tree`

2. 在 Amaze UI 样式之后引入 Tree 样式（`dist` 目录下的 CSS）：

  Amaze UI Tree 依赖 Amaze UI 样式。

  ```html
  <link rel="stylesheet" href="path/to/amazeui.min.css"/>
  <link rel="stylesheet" href="path/to/amazeui.tree.min.css"/>
  ```

3. 在 jQuery 之后引入 Tree 插件（`dist` 目录下的 JS）：

  ```html
  <script src="path/to/jquery.min.js"></script>
  <script src="path/to/amazeui.tree.min.js"></script>
  ```

4. 初始化 Tree:

  ```js
  $(function() {
      $('#my-tree').tree();
  });
  ```
