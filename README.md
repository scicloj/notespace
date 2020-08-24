# notespace

Notebook experience in your Clojure namespace

## What is it?

This library is an attempt to answer the following question: can we have a notebook-like experience in Clojure without leaving one's favourite editor?

## Status

Everything here is considered experimental.

## Version 3
Version 3, under active development, is an attempt to rethink the user experience and internals of Version 2.

You can see the evolving draft under the [v3 branch](https://github.com/scicloj/notespace/tree/v3).

It follows some ideas from our discussions of [alternative notation](https://clojurians.zulipchat.com/#narrow/stream/224153-notespace-dev/topic/alternative.20notation) and of [evaluation semantics](https://clojurians.zulipchat.com/#narrow/stream/224153-notespace-dev/topic/evaluation.20semantics.20--.20suggested.20breaking.20change).

Several people's ideas and comments have affected this version.
`@awb99` `@behrica` `@daslu` `@genmeblog` `@jsa-aerial` `@metasoarous` `@nickstares` `@vlaaad` 

### Usage

See [this screencast](https://tinyurl.com/y5vg5qfe) for a first experimental concept. See [this namespace](./test/notespace/v3_experiment1_test.clj) for some more examples.

The concepts and idioms here may change. They are presented just as a basis for further discussion.

### Emacs config

See [emacs-config.el](./emacs-config.el) as a recommended way to connect the 

### Implementation
For state management and event handling at Clojure JVM, we use [cljfx](https://github.com/cljfx/cljfx)'s [state management logic](https://github.com/cljfx/cljfx#subscriptions-and-contexts).

The rendering engine is based on [gorilla-notes](https://github.com/scicloj/gorilla-notes), which is a thin wrappre of [gorilla-ui](https://github.com/pink-gorilla/gorilla-ui).

The client side stack is based on [shadow-cljs](https://github.com/thheller/shadow-cljs) and [reagent](https://reagent-project.github.io).

As with Version 2, we use [tools.reader](https://github.com/clojure/tools.reader) to read the code of the namespace.

### Known issues
* Rendering as static html is not supported yet.
* Many of the notions, ideas and behaviours of Version 2 are not supported by Version 3 at the moment. Most of them are enabled in a different way. We need to discuss whether to create some backwards compatibility layer.
* At the moment, this version brings [cljfx](https://github.com/cljfx/cljfx) as a dependency, merely for its state management logic. This means it will run only on JDK 11.

## Version 2
The current version at the master branch and at Clojars is Version 2.
[![Clojars Project](https://img.shields.io/clojars/v/scicloj/notespace.svg)](https://clojars.org/scicloj/notespace)

At the moment, it is used for documentation and testing at [ClojisR](https://github.com/scicloj/clojisr) and [ClojisR-examples](https://github.com/scicloj/clojisr-examples).

### Usage

See the [screencast](https://drive.google.com/file/d/1D0EBTA2Udt2vjEEetiHqjjk1blb79XcY/view?usp=sharing) to have an idea about it.

See this [example namespace](./test/notespace/v2/tutorial_test.clj) and its [rendered html](https://scicloj.github.io/notespace/doc/notespace/v2/tutorial-test/index.html).

### Known issues
* Links to external resources (e.g., images in separate files) will appear at the rendered static html, but are currently invisible at the live-reload view.

## Discussion

Hearing your comments, opinions and wishes will help!

[#notespace-dev at the Clojurians Zulip](https://clojurians.zulipchat.com/#narrow/stream/224153-notespace-dev).

## Relation to other projects

There are several magnificent existing options for literate programming in Clojure: Marginalia, Org-Babel, Gorilla REPL, Pink Gorilla, Clojuopyter, Nextjournal, Saite, Oz. Most of them are actively developed.

Creating a separate alternative would be the least desired outcome of the current project. Rather, the hope is to compose and integrate well with some of the other projects. There has been some thoughts and experiments in that direction, and it seems promising.

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
