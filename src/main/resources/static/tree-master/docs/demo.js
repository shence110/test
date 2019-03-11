jQuery(function($) {
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


  // demo 2
  var $myTree = $('#myTree');
  $myTree.tree({
    dataSource: function(options, callback) {
      setTimeout(function() {
        callback({
          data: [
            {
              title: 'Ascending and Descending',
              type: 'folder',
              attr: {
                id: 'folder1'
              }
            },
            {
              title: 'Sky and Water I (with custom icon)',
              type: 'item',
              attr: {
                id: 'item1',
                icon: 'am-icon-file'
              }
            },
            {
              title: 'Drawing Hands',
              type: 'folder',
              attr: {
                id: 'folder2'
              }
            },
            {
              title: 'Waterfall',
              type: 'item',
              attr: {
                id: 'item2'
              }
            },
            {
              title: 'Belvedere',
              type: 'folder',
              attr: {
                id: 'folder3'
              }
            },
            {
              title: 'Relativity (with custom icon)',
              type: 'item',
              attr: {
                id: 'item3',
                icon: 'am-icon-picture-o'
              }
            },
            {
              title: 'House of Stairs',
              type: 'folder',
              attr: {
                id: 'folder4'
              }
            },
            {
              title: 'Convex and Concave',
              type: 'item',
              attr: {
                id: 'item4'
              }
            }
          ]
        });
      }, 400);
    },
    multiSelect: true,
    cacheItems: true,
    folderSelect: false
  }).on('loaded.tree.amui', function(e) {
    console.log('Loaded');
  }).on('selected.tree.amui', function(e, selected) {
    console.log('Select Event: ', selected);
    console.log($myTree.tree('selectedItems'));
  }).on('updated.tree.amui', function(e, selected) {
    console.log('Updated Event: ', selected);
    console.log($myTree.tree('selectedItems'));
  }).on('opened.tree.amui', function(e, info) {
    console.log('Open Event: ', info);
  }).on('closed.tree.amui', function(e, info) {
    console.log('Close Event: ', info);
  });


  // demo 3
  $('#myTreeSelectableFolder').tree({
    dataSource: function(options, callback) {
      setTimeout(function() {
        callback({
          data: [
            {
              title: 'Ascending and Descending',
              type: 'folder',
              attr: {
                id: 'F1'
              }
            },
            {
              title: 'Drawing Hands',
              type: 'folder',
              attr: {
                id: 'F2'
              }
            },
            {
              title: 'Belvedere',
              type: 'folder',
              attr: {
                id: 'F3'
              }
            },
            {
              title: 'House of Stairs',
              type: 'folder',
              attr: {
                id: 'F4'
              }
            },
            {
              title: 'Belvedere',
              type: 'folder',
              attr: {
                id: 'F5'
              }
            }
          ]
        });
      }, 400);
    },
    cacheItems: true,
    folderSelect: true,
    multiSelect: true
  });
});
