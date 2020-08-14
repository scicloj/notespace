# notespace

Notebook experience in your Clojure namespace

**Note: breaking changes are coming soon. **

[![Clojars Project](https://img.shields.io/clojars/v/scicloj/notespace.svg)](https://clojars.org/scicloj/notespace)

## What is it?

This library is an attempt to answer the following question: can we have a notebook-like experience in Clojure without leaving our favourite editor?
## Status

Experimental

## Version 3
Currently in branch.

Remarks:
* At the moment, this version depends on [cljfx](https://github.com/cljfx/cljfx), merely for its [state management abstractions](https://github.com/cljfx/cljfx#subscriptions-and-contexts). This means it will run only on JDK 11.

## Version 2

See the [screencast](https://drive.google.com/file/d/1D0EBTA2Udt2vjEEetiHqjjk1blb79XcY/view?usp=sharing) for version 2.

### Usage

See this [example namespace](./test/notespace/v2/tutorial_test.clj) and its [rendered html](https://scicloj.github.io/notespace/doc/notespace/v2/tutorial-test/index.html).

## Goals

* Use any Clojure namespace, in any Clojure editor, as a notebook
  * [x] editing is done in your beloved editor
  * [x] rendering is shown at the browser
  * [x] a typical workflow has zero latency in namespace evaluation
* A nootebook is a sequence of notes
  * [x] a note represents a piece of computation and its output
  * [x] the notebook's rendering is the concatenation of its notes' renderings
  * [x] a notes' rendering can depend on its code and its output
  * [x] the precise way it depends on them varies across different kinds of nodes
  * [ ] a notes' rendering can depend on the stdout and stderr of its computation
  * [x] we remember notes' last rendering
  * [ ] we know if notes need a refresh (after code change)
* Everything can be a note
  * [x] function definitions
  * [x] tests
  * [x] code examples
  * [x] data analysis
* Support for tests
  * [x] summary of checks passed/failed
* Live reload experience
  * [x] refreshing the browser view after a note's computation has been refreshed
  * [ ] re-rendering of relevant notebook part(s)
* Different modes of computation-refresh
  * [x] compute-this-note
  * [x] compute-the-whole-namespace
  * [ ] Oz-like refresh-downwards-from-this-note
* Composing with classical Clojure documentation practices.
  * [ ] Think how that should work.
  * [ ] Can we somehow automatically generate docstrings (like `hara.test` does) ?
  * [ ] Can we automatically generate API docs ?
  * [ ] Can we integrate with cljdocs ?
  
  
## Relation to other projects

There are several magnificent existing options for literate programming in Clojure: Marginalia, Org-Babel, Gorilla REPL, Pink Gorilla, Clojuopyter, Nextjournal, Saite, Oz. Most of them are actively developed.

Creating a separate alternative would be the least desired outcome of the current project. Rather, the hope is to compose and integrate well with some of the other projects. There has been some thoughts and experiments in that direction, and it seems promising.

## Known issues
* Links to external resources (e.g., images in separate files) will appear at the rendered static html, but are currently invisible at the live-reload view.

## License

Copyright © 2019 Scicloj

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
