'use strict';

var gulp = require('gulp');
var docUtil = require('amazeui-doc-util');
var runSequence = require('run-sequence');
var tasks = require('amazeui-gulp-tasks');

var config = {
  pkg: require('./package.json'),

  // release task
  ghPages: {
    src: 'dist/**/*'
  },

  // remote branch
  git: {
    branch: 'master'
  },

  browserSync: {
    notify: false,
    server: 'dist',
    logPrefix: 'AMP'
  },

  // watch files and reload browserSync
  bsWatches: 'dist/**/*',

  less: {
    src: './less/amazeui.tree.less',
    autoPrefixer: docUtil.autoprefixerBrowsers,
    dist: './dist',
    watches: './less/**/*.less'
  },

  // docs:md
  md: {
    src: ['README.md', 'docs/*.md'],
    data: {
      pluginTitle: 'Amaze UI Tree',
      pluginDesc: 'jQuery Tree 插件 Amaze UI 集成',
      buttons: 'amazeui/tree',
      head: '<link rel="stylesheet" href="../amazeui.tree.css"/>'
    },
    rename: function(file) {
      file.basename = file.basename.toLowerCase();
      if (file.basename === 'readme') {
        file.basename = 'index';
      }
      file.extname = '.html';
    },
    dist: function(file) {
      if (file.relative === 'index.html') {
        return 'dist'
      }
      return 'dist/docs';
    }
  },

  // browserify
  browserify: {
    bundleOptions: {
      entries: './lib/amazeui.tree.js',
      standalone: 'AMUITree',
      cache: {},
      packageCache: {}
    },
    filename: 'amazeui.tree.js',
    transforms: [['browserify-shim', {global: true}]],
    plugins: [],
    dist: 'dist'
  },

  // clean path
  clean: 'dist'
};

// init tasks
tasks(gulp, config);

gulp.task('copy:js', function() {
  return gulp.src('docs/*.js')
    .pipe(gulp.dest('dist/docs'));
});

gulp.task('copy:watch', function() {
  return gulp.watch('docs/*.js', ['copy:js']);
});

gulp.task('copy', ['copy:js', 'copy:watch']);

gulp.task('build', function(cb) {
  runSequence('clean', ['copy', 'browserify', 'less', 'markdown'], cb);
});

gulp.task('dev', ['build', 'server']);
