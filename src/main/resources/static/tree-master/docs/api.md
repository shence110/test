---
title: Amaze UI Tree API 列表
next: api.html
---

# Amaze UI Tree API
---

## 方法

### .tree('destroy')

销毁 Tree 产生的数据。

### .tree('selectedItems')

返回包含选中条目的数组。

### .tree('selectItem', $('#itemId'))

选中 `$('#itemId') 条目（不包含子级的条目）。

### .tree('openFolder', $('#folderId'))

打开指定文件夹。

### .tree('closeFolder', $('#folderId'))

关闭指定文件夹。

### .tree('toggleFolder', $('#folderId'))

交替文件夹状态。

### .tree('closeAll')

关闭所有文件夹。

### .tree('discloseAll')

展开所有文件夹。

## 选项

<table class="am-table am-table-bordered am-table-striped">
  <thead>
    <tr>
      <th style="width: 100px;">名称</th>
      <th style="width: 80px;">类型</th>
      <th style="width: 130px;">默认</th>
      <th>描述</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>dataSource</td>
      <td>function</td>
      <td></td>
      <td>数据处理函数</td>
    </tr>
    <tr>
      <td>multiSelect</td>
      <td>boolean</td>
      <td>false</td>
      <td>是否开启多选</td>
    </tr>
    <tr>
      <td>cacheItems</td>
      <td>boolean</td>
      <td>true</td>
      <td>是否缓存已经渲染的选项</td>
    </tr>
    <tr>
      <td>folderSelect</td>
      <td>boolean</td>
      <td>true</td>
      <td>是否允许选择文件夹</td>
    </tr>
    <tr>
      <td>folderIcon</td>
      <td>string</td>
      <td>plus-square-o</td>
      <td>文件夹图标</td>
    </tr>
    <tr>
      <td>folderOpenIcon</td>
      <td>string</td>
      <td>minus-square-o</td>
      <td>文件夹打开图标</td>
    </tr>
    <tr>
      <td>itemIcon</td>
      <td>string</td>
      <td>angle-right</td>
      <td>条目图标</td>
    </tr>
    <tr>
      <td>itemSelectedIcon</td>
      <td>string</td>
      <td>check</td>
      <td>条目选中图标</td>
    </tr>
  </tbody>
</table>

### 数据格式

插件需要以对象数组的形式提供数据，数据格式如下：

<div class="am-scrollable-horizontal">
  <table class="am-table am-table-bordered am-table-striped am-text-nowrap">
    <thead>
    <tr>
      <th style="width: 125px;">键</th>
      <th style="width: 100px;">类型</th>
      <th>描述</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>title</td>
      <td>string</td>
      <td><i>必需</i> 节点标题</td>
    </tr>
    <tr>
      <td>type</td>
      <td>string</td>
      <td><i>必需</i> 节点类型 <code>folder</code> 或 <code>item</code></td>
    </tr>
    <tr>
      <td>attr</td>
      <td>object</td>
      <td>附加数据</td>
    </tr>
    <tr>
      <td>attr.classNames</td>
      <td>string</td>
      <td>添加到节点的类名</td>
    </tr>
    <tr>
      <td>attr.icon</td>
      <td>string</td>
      <td>自定义图标的 class 名称</td>
    </tr>
    <tr>
      <td>attr.id</td>
      <td>string</td>
      <td></td>
    </tr>
    </tbody>
  </table>
</div>

## 自定义事件

<table class="am-table am-table-bordered am-table-striped">
  <thead>
  <tr>
    <th style="width: 150px;">Event Type</th>
    <th>Description</th>
  </tr>
  </thead>
  <tbody>
    <tr>
    <td>selected.tree.amui</td>
    <td>Fires when a user selects an item or folder. Returns an object containing an array of the selected items' jQuery data and the jQuery data of the triggering item. <code>{ selected: [array], target: [object] }</code></td>
  </tr>
  <tr>
    <td>deselected.tree.amui</td>
    <td>Fires when a user deselects an item or folder. Returns an object containing an array of the selected items' jQuery data and the jQuery data of the triggering item. <code>{ selected: [array], target: [object] }</code></td>
  </tr>
  <tr>
    <td>loaded.tree.amui</td>
    <td>Fires when sub-content has been is loaded. Returns the jQuery element of the folder loaded.</td>
  </tr>
<tr>
    <td>updated.tree.amui</td>
    <td>Fires after <code>selected.tree.amui</code> and <code>deselected.tree.amui</code> events. Returns an object containing an array of selected items' jQuery data, the triggering jQuery element and the event type. <code>{ selected: [array], item: [object], eventType: [string] }</code></td>
  </tr>
  <tr>
    <td>disclosedFolder.tree.amui</td>
    <td>Fires when a user opens a folder. Returns an object containing the jQuery data of the opened folder.</td>
  </tr>
  <tr>
    <td>closed.tree.amui</td>
    <td>Fires when a user closes a folder. Returns an object containing the jQuery data of the closed folder.</td>
  </tr>
  <tr>
    <td>closedAll.tree.amui</td>
    <td>Fires when all folders have finished closing. Returns an object containing an array of closed folders' jQuery data and the tree's jQuery element. The <code>length</code> of <code>reportedClosed</code> will provide the number of folders closed. <code>{ reportedClosed: [array], tree: [$element] }</code></td>
  </tr>
  <tr>
    <td>disclosedVisible.tree.amui</td>
    <td>Fires when all visible folders have disclosed/opened. Returns an object containing an array of disclosed folders' jQuery data and the tree's jQuery element. The <code>length</code> of <code>reportedOpened</code> will provide the number of folders opened. <code>{ reportedOpened: [array], tree: [$element] }</code></td>
  </tr>
    <td>exceededDisclosuresLimit.tree.amui</td>
    <td>Fires when tree halts disclosing due to hitting discloserLimit cap. Returns an object containing <code>{ disclosures: [number], tree: [$element] }</code></td>
  </tr>
  <tr>
    <td>disclosedAll.tree.amui</td>
    <td>Fires when all folders have disclosed. <em>It will not fire if tree stops disclosing due to hitting discloserLimit cap.</em> Returns an object containing <code>{ disclosures: [number], tree: [$element] }</code></td>
    </tr>
  </tbody>
</table>

所有事件在 <code>.am-tree</code> 上触发。

```js
$('#myTree').on('selected.tree.amui', function (event, data) {
  // do something with data: { selected: [array], target: [object] }
});
```
