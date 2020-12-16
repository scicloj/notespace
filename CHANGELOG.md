# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [3.0.0-alpha3-SNAPSHOT] - 2020-11-12
- added support for visualizing datasets as tables
- extending markdown rendering to more types
- bugfix in reporting the static rendering path
- bugfix: switching namespaces correctly
- changes in aesthetics
- handling delays, futures, atoms without needing the D,F,A special constructs
- using a version of gorilla-notes with an up-to-date core.async dep
- handling event broadcasting more carefully
- explicitly marking dereferenced values in rendering by the `(@)` sign
- Notes which are forms like `(def ...)`, `(defn ...)`, `(defmafro ...)` are assigned the kind `:void`.
- added api action: `eval-and-realize-this-notespace`
- extended api with functions such as `eval-and-realize-notes-from-line` and `eval-and-realize-notes-from-change`
- support for an Oz-like experience of listening to changes
- added a `view` api function for viewing a single note
- added a config option to avoid rendering the source code of notes
- extended the dataset-grid note kind to handle sequences of maps
- added progress logging support
- handling config changes in the event flow, preserving config on reset
- support for single-note-mode
- communicating note state more carefully
- changed default target base path from "doc" to "docs"
- made the source base path globally configurable

## [3.0.0-alpha2] - 2020-09-21
- changes in some note kinds
- static site rendering
- further progress with Clojure reference types
- experimenting with interactive inputs and reactive notes

## [3.0.0-alpha1-SNAPSHOT] - 2020-08-24
- a complete rewrite
- new evaluation semantics
- new syntax
- using gorilla-notes as a rendering engine
- using cljfx context and subscriptions for JVM state management

## [2.0.0-alpha5] - 2020-04-30
- bugfix: using static files on live-reload mode
- basic support for vega static rendering (through svg)
- making sure the default target path exists

## [2.0.0-alpha4] - 2020-03-29
- master rendered location is now configuralble, and with a different default: doc instead of resources/public
- wider zprint render (40->72)
- more informative exceptions when note computations fail

## [2.0.0-alpha3] - 2020-02-24
- added css processing + basic.css file for rendered notebooks

## [2.0.0-alpha2] - 2020-02-21
- fixed the re-read mechanism, to update existing notes with reader metadata (e.g., source lines)
- added a `reset-state!` API function

## [2.0.0-alpha1] - 2020-02-21
- a major change with a new mechanism, breaks the previous workflow
  - live-reload
  - usage of tools.reader for more accurate code-rendering
  - now code can be represented as text (rather than the forms read from the text)
  - code is indexed by lines, rather than forms
  - fixing various bugs and limitations that were related related to representing code as forms
  - written to be integrated with some basic editor support

## [1.0.3] - 2020-01-07
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
