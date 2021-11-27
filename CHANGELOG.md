# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [4-alpha-18]
- more robust handling of tab titles
- bugfix: handling nrepl events with no path

## [4-alpha-17]
- handling event edge cases
- added merge-config! api fn
- aesthetics change
- bugfix: making sure paths are real paths

## [4-alpha-16] - 2021-11-24
- minor reordering of tabs
- tabs logic bugfixes
- changes in aesthetics

## [4-alpha-15] - 2021-11-23
- more tab-aware dynamics

## [4-alpha-14]
- tab rename
- tab-aware static rendering

## [4-alpha-12] [4-alpha-13] - 2021-11-17
- disabled some problematic event handling
- changes in tabs
- broken channel bugfix
- view change

## [4-alpha-11] - 2021-11-16
- added set-config! to api
- changed channels params
- shifting to tab-based frontend
- handling defmulti, defmethod
- more careful handling of eval events (WIP)

## [4-alpha-10] - 2021-11-12
- basic config support

## [4-alpha-9] - 2021-11-12
- frontend refactoring (still keeping gorilla-notes for now)
- updated deps
- fixed concurrency bug with events counter

## [4-alpha-8] - 2021-10-26
- bugfix: unnecessary frontend updates

## [4-alpha-7] - 2021-10-26
- support for delays

## [4-alpha-6] - 2021-10-25
- more sensible frontend updates

## [4-alpha-5] - 2021-10-24
- event debounce
- bugfix in note editing on eval failure

## [4-alpha-4] - 2021-10-15
- minor api extension
- more careful kind resolution
- more detailed information tracking from nREPL
- made the information flow a little more informative
- image support
- deriving some default note kinds by the notes' form

## [4-alpha-3] - 2021-10-10
- cleanup of old namespaces from the generated jar

## [4-alpha-2] - 2021-10-10
- API change, separating into two systems: frontend & events
- cleaning up v3 code

## [4-alpha-1] - 2021-10-09
- An initial release of Notespace v4, a rewrite of Notespace for more seamless setup and integration with user workflows.

## [3-beta9] - 2021-08-16
- allow to specify options for cli render [#61](https://github.com/scicloj/notespace/pull/61)

## [3-beta8] - 2021-08-16
- excluding com.fzakaria/slf4j-timbre in project definition
- removing the delay in listen mode

## [3-beta7] - 2021-06-14
- updated gorilla-notes version, where static rendering works differently: the client side bundle is a file living alongside the html document, rather than downloaded from a cdn

## [3-beta6] - 2021-05-08
- updated gorilla-notes version, solving some concurrency and performance problems on the frontend

## [3-beta5] - 2021-03-16
- updated to gorilla-notes 0.5.11: css changes (borught margins back), prepartation for added sci component and external files, support for math inside markdown, experimental quil support, improved browser sync
- sci notes
- quil notes (experimental)
- support for external files (experimental)
- added function for rendering a namespace to file, given a ns symbol (PR #57)
- clojute.test support (experimental)
- more careful note rendering (handling various situations more gracefully)

## [3-beta4] - 2021-02-25
- removed the cljfx dependency (copied the relevant parts under Notespace)

## [3-beta3] - 2021-02-20
- (temporarily?) removing the Oz-compatibility sugar, that was not implemented well
- considering more things as ::void kind
- inferring note behaviors from types

## [3-beta2] - 2021-02-19
- rendering notes with status descriptions properly (fixing a recent mistake in logic)

## [3-beta1] (previously called: [3-alpha3-SNAPSHOT]) - 2021-02-17
- added support for visualizing datasets as tables
- extending markdown rendering to more types
- bugfix in reporting the static rendering path
- bugfix: switching namespaces correctly
- changes in aesthetics
- handling delays, futures, atoms without needing the D,F,A special constructs
- using a version of gorilla-notes with an up-to-date core.async dep
- handling event broadcasting more carefully
- explicitly marking dereferenced values in rendering by the `(@)` sign
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
- cleanup of old dependencies and obsolete code
- (require ...) forms get the void kind by default
- recognizing midje facts as a special kind of note, with a dedicated rendering function
- gorilla-notes update (0.5.0): state cleanup on server and client
- plain html support
- customizable server port
- support for stopping the server
- Notes which are forms like `(def ...)`, `(defn ...)`, and a few more of these, are assigned the kind `:void`.
- added :code and :math note kinds
- support for overriding note kinds on runtime

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
