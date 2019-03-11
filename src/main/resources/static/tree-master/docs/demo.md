---
title: Amaze UI Tree 使用演示
next: api.html
prev: ../
---

## 使用演示
---

使用 Tree 插件时，需要设置一个 HTML 模板，然后使用 JS 初始化。

下面是必须的模板结构：

```html
<ul class="am-tree">
  <!-- 包含子级的条目模板 -->
  <li class="am-tree-branch am-hide" data-template="treebranch">
    <div class="am-tree-branch-header">
      <button class="am-tree-branch-name">
        <span class="am-tree-icon am-tree-icon-folder"></span>
        <span class="am-tree-label"></span>
      </button>
    </div>
    <ul class="am-tree-branch-children"></ul>
    <div class="am-tree-loader">Loading...</div>
  </li>

  <!-- 不包含子级的条目模板 -->
  <li class="am-tree-item am-hide" data-template="treeitem">
    <button class="am-tree-item-name">
      <span class="am-tree-icon am-tree-icon-item"></span>
      <span class="am-tree-label"></span>
    </button>
  </li>
</ul>
```

```js
var dataSource = function(parentData, callback){
  //...
};

$('#myTree').tree({dataSource: dataSource});
```

### 演示 1

`````html
<ul class="am-tree" id="firstTree">
  <li class="am-tree-branch am-hide" data-template="treebranch">
    <div class="am-tree-branch-header">
      <button class="am-tree-branch-name">
        <span class="am-tree-icon am-tree-icon-folder"></span>
        <span class="am-tree-label"></span>
      </button>
    </div>
    <ul class="am-tree-branch-children"></ul>
    <div class="am-tree-loader"><span class="am-icon-spin am-icon-spinner"></span></div>
  </li>
  <li class="am-tree-item am-hide" data-template="treeitem">
    <button class="am-tree-item-name">
      <span class="am-tree-icon am-tree-icon-item"></span>
      <span class="am-tree-label"></span>
    </button>
  </li>
</ul>
`````
```html
<ul class="am-tree" id="firstTree">
  <li class="am-tree-branch am-hide" data-template="treebranch">
    <div class="am-tree-branch-header">
      <button class="am-tree-branch-name">
        <span class="am-tree-icon am-tree-icon-folder"></span>
        <span class="am-tree-label"></span>
      </button>
    </div>
    <ul class="am-tree-branch-children"></ul>
    <div class="am-tree-loader"><span class="am-icon-spin am-icon-spinner"></span></div>
  </li>
  <li class="am-tree-item am-hide" data-template="treeitem">
    <button class="am-tree-item-name">
      <span class="am-tree-icon am-tree-icon-item"></span>
      <span class="am-tree-label"></span>
    </button>
  </li>
</ul>
```
```js
  // demo 1
  var data = [
    {
      title: '苹果公司',
      type: 'folder',
      products: [
        {
          title: 'iPhone',
          type: 'item'
        },
        {
          title: 'iMac',
          type: 'item'
        },
        {
          title: 'MacBook Pro',
          type: 'item'
        }
      ]
    },
    {
      title: '微软公司',
      type: 'item'
    },
    {
      title: 'GitHub',
      type: 'item',
      attr: {
        icon: 'am-icon-github'
      }
    }
  ];

  $('#firstTree').tree({
    dataSource: function(options, callback) {
      // 模拟异步加载
      setTimeout(function() {
        callback({data: options.products || data});
      }, 400);
    },
    multiSelect: false,
    cacheItems: true,
    folderSelect: false
  });
```

### 演示 2

`````html
<ul class="am-tree" role="tree" id="myTree">
  <li class="am-tree-branch am-hide" data-template="treebranch" role="treeitem" aria-expanded="false">
    <div class="am-tree-branch-header">
      <button class="am-tree-branch-name">
        <span class="am-tree-icon am-tree-icon-folder"></span>
        <span class="am-tree-label"></span>
      </button>
    </div>
    <ul class="am-tree-branch-children" role="group"></ul>
    <div class="am-tree-loader" role="alert">Loading...</div>
  </li>
  <li class="am-tree-item am-hide" data-template="treeitem" role="treeitem">
    <button class="am-tree-item-name">
      <span class="am-tree-icon am-tree-icon-item"></span>
      <span class="am-tree-label"></span>
    </button>
  </li>
</ul>
`````
```html
<ul class="am-tree" role="tree" id="myTree">
  <li class="am-tree-branch am-hide" data-template="treebranch" role="treeitem" aria-expanded="false">
    <div class="am-tree-branch-header">
      <button class="am-tree-branch-name">
        <span class="am-tree-icon am-tree-icon-folder"></span>
        <span class="am-tree-label"></span>
      </button>
    </div>
    <ul class="am-tree-branch-children" role="group"></ul>
    <div class="am-tree-loader" role="alert">Loading...</div>
  </li>
  <li class="am-tree-item am-hide" data-template="treeitem" role="treeitem">
    <button class="am-tree-item-name">
      <span class="am-tree-icon am-tree-icon-item"></span>
      <span class="am-tree-label"></span>
    </button>
  </li>
</ul>
```

### 文件夹可选择

`````html
<ul class="am-tree am-tree-folder-select" role="tree" id="myTreeSelectableFolder">
  <li class="am-tree-branch am-hide" data-template="treebranch" role="treeitem" aria-expanded="false">
    <div class="am-tree-branch-header">
      <button class="am-tree-icon am-tree-icon-caret am-icon-caret-right"><span class="am-sr-only">Open</span></button>
      <button class="am-tree-branch-name">
        <span class="am-tree-icon am-tree-icon-folder"></span>
        <span class="am-tree-label"></span>
      </button>
    </div>
    <ul class="am-tree-branch-children" role="group"></ul>
    <div class="am-tree-loader" role="alert">Loading...</div>
  </li>
  <li class="am-tree-item am-hide" data-template="treeitem" role="treeitem">
    <button class="am-tree-item-name">
      <span class="am-tree-icon am-tree-icon-item"></span>
      <span class="am-tree-label"></span>
    </button>
  </li>
</ul>
`````
```html
<ul class="am-tree am-tree-folder-select" role="tree" id="myTreeSelectableFolder">
  <li class="am-tree-branch am-hide" data-template="treebranch" role="treeitem" aria-expanded="false">
    <div class="am-tree-branch-header">
      <button class="am-tree-icon am-tree-icon-caret am-icon-caret-right"><span class="am-sr-only">Open</span></button>
      <button class="am-tree-branch-name">
        <span class="am-tree-icon am-tree-icon-folder"></span>
        <span class="am-tree-label"></span>
      </button>
    </div>
    <ul class="am-tree-branch-children" role="group"></ul>
    <div class="am-tree-loader" role="alert">Loading...</div>
  </li>
  <li class="am-tree-item am-hide" data-template="treeitem" role="treeitem">
    <button class="am-tree-item-name">
      <span class="am-tree-icon am-tree-icon-item"></span>
      <span class="am-tree-label"></span>
    </button>
  </li>
</ul>
```

<script src="../amazeui.tree.js"></script>
<script src="demo.js"></script>
