# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [2.0.0-alpha1] - 2019-02-21
- a major change with a new mechanism, breaks the previous workflow
  - live-reload
  - usage of tools.reader for more accurate code-rendering
  - now code can be represented as text (rather than the forms read from the text)
  - code is indexed by lines, rather than forms
  - fixing various bugs and limitations that were related related to representing code as forms
  - written to be integrated with some basic editor support

## [1.0.3] - 2019-01-07
- changed in page rendering
- recognizing source path carefully
- switched to cambrium logging
- added a new kind of tests ("checks") infrastructure
- changed notes equality semantics

## [1.0.1] - 2019-12-31
- changed colors
- avoiding empty table of contents
- handling quote signs in code (see [this zprint issue](https://github.com/kkinnear/zprint/issues/121))

## [1.0.0] - 2019-12-25
Initial v0 version.
