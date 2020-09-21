# notespace

Notebook experience in your Clojure namespace

[![Clojars Project](https://img.shields.io/clojars/v/scicloj/notespace.svg)](https://clojars.org/scicloj/notespace)

## What is it?

This library is an attempt to answer the following question: can we have a notebook-like experience in Clojure without leaving one's favourite editor?

## Status

Everything here is considered experimental.

## Version 3
Version 3, under active development, is an attempt to rethink the user experience and internals of Version 2.

It follows some ideas from our discussions of [alternative notation](https://clojurians.zulipchat.com/#narrow/stream/224153-notespace-dev/topic/alternative.20notation) and of [evaluation semantics](https://clojurians.zulipchat.com/#narrow/stream/224153-notespace-dev/topic/evaluation.20semantics.20--.20suggested.20breaking.20change).

Several people's ideas and comments have affected this version.
`@awb99` `@behrica` `@daslu` `@ezmiller` `@genmeblog` `@jsa-aerial` `@metasoarous` `@nickstares` `@vlaaad` 

### Usage

See [this screencast](https://youtu.be/_GcTnkhn9g0) for a first experimental concept. It is a bit outdated in terms of the note kinds.

See [this namespace](./test/notespace/v3_experiment1_test.clj) for some more examples, or read it directly [as the corresponding rendered html](https://scicloj.github.io/notespace/doc/notespace/v3-experiment1-test/index.html).

The concepts and idioms here may change. They are presented just as a basis for further discussion.

See [emacs-config.el](https://github.com/scicloj/notespace/blob/v3/emacs-config.el) as a recommended way to connect editor keybindings to the main api functions.

### Main changes
Here are the main thingss that are changing here, comparing to Version 2:
* Version 3 offers a different notion of what notespaces and ntoes are. Arguably, this notion is less strange to the usual Clojure concepts and idioms.
* Very little extra notation is necessary, and it is just some tiny amount of metadata that determines how things should be rendered.
* A small set of API functions allows to evaluate code in a way that informs the browser notebook about changes.
* Time-related Clojure semantics such as futures and delays can be handled in a way that respects and informs the notebook too when values are realized.
* The new implementation makes things more flexible and easier to extend and experiment with.
* Data visualizations based on [gorilla-ui](https://github.com/pink-gorilla/gorilla-ui) Hiccup extensions are supported.

### Implementation
For state management and event handling at Clojure JVM, we use [cljfx](https://github.com/cljfx/cljfx)'s [state management logic](https://github.com/cljfx/cljfx#subscriptions-and-contexts).

The current rendering engine is based on [gorilla-notes](https://github.com/scicloj/gorilla-notes), which is a thin wrapper of [gorilla-ui](https://github.com/pink-gorilla/gorilla-ui).

The client side stack is based on [shadow-cljs](https://github.com/thheller/shadow-cljs) and [reagent](https://reagent-project.github.io).

As with Version 2, we use [tools.reader](https://github.com/clojure/tools.reader) to read the code of the namespace.

#### Renderers

Rendering is now an abstract notion, and thus Notespace can be extended with different ways of rendering. At least one of them will create static-htmls, based on gorilla-notes. Other potential renderers may rely on [Oz](https://github.com/metasoarous/oz) or [rmarkdown-clojure](https://github.com/genmeblog/rmarkdown-clojure), or generate and update [Pink Gorilla](https://pink-gorilla.github.io) notebooks on the fly.

### Known issues
* Rendering as static html is not supported yet.
* Many of the notions, ideas and behaviours of Version 2 are not supported by Version 3 at the moment. Most of them are enabled in a different way. We need to discuss whether to create some backwards compatibility layer.
* At the moment, this version brings [cljfx](https://github.com/cljfx/cljfx) as a dependency, merely for its state management logic. This means it will run only on JDK 11.
* Currently things tend to break when moving across several namespaces. For now, calling `(notespace.api/init)` is a good habit for such cases.

It follows some ideas from our discussions of [alternative notation](https://clojurians.zulipchat.com/#narrow/stream/224153-notespace-dev/topic/alternative.20notation) and of [evaluation semantics](https://clojurians.zulipchat.com/#narrow/stream/224153-notespace-dev/topic/evaluation.20semantics.20--.20suggested.20breaking.20change).

Several people's ideas and comments have affected this version.
`@awb99` `@behrica` `@daslu` `@ezmiller` `@genmeblog` `@jsa-aerial` `@metasoarous` `@nickstares` `@vlaaad` 

### Usage

See [this screencast](https://youtu.be/_GcTnkhn9g0) for a first experimental concept. See [this namespace](./test/notespace/v3_experiment1_test.clj) for some more examples.

The concepts and idioms here may change. They are presented just as a basis for further discussion.

See [emacs-config.el](https://github.com/scicloj/notespace/blob/v3/emacs-config.el) as a recommended way to connect editor keybindings to the main api functions.

### Main changes
Here are the main thingss that are changing here, comparing to Version 2:
* Version 3 offers a different notion of what notespaces and ntoes are. Arguably, this notion is less strange to the usual Clojure concepts and idioms.
* Very little extra notation is necessary, and it is just some tiny amount of metadata that determines how things should be rendered.
* A small set of API functions allows to evaluate code in a way that informs the browser notebook about changes.
* Time-related Clojure semantics such as futures and delays can be handled in a way that respects and informs the notebook too when values are realized.
* The new implementation makes things more flexible and easier to extend and experiment with.
* Data visualizations based on [gorilla-ui](https://github.com/pink-gorilla/gorilla-ui) Hiccup extensions are supported.

### Implementation
For state management and event handling at Clojure JVM, we use [cljfx](https://github.com/cljfx/cljfx)'s [state management logic](https://github.com/cljfx/cljfx#subscriptions-and-contexts).

The current rendering engine is based on [gorilla-notes](https://github.com/scicloj/gorilla-notes), which is a thin wrapper of [gorilla-ui](https://github.com/pink-gorilla/gorilla-ui).

The client side stack is based on [shadow-cljs](https://github.com/thheller/shadow-cljs) and [reagent](https://reagent-project.github.io).

As with Version 2, we use [tools.reader](https://github.com/clojure/tools.reader) to read the code of the namespace.

#### Renderers

Rendering is now an abstract notion, and thus Notespace can be extended with different ways of rendering. At least one of them will create static-htmls, based on gorilla-notes. Other potential renderers may rely on [Oz](https://github.com/metasoarous/oz) or [rmarkdown-clojure](https://github.com/genmeblog/rmarkdown-clojure), or generate and update [Pink Gorilla](https://pink-gorilla.github.io) notebooks on the fly.

### Known issues
* Rendering as static html is not supported yet.
* Many of the notions, ideas and behaviours of Version 2 are not supported by Version 3 at the moment. Most of them are enabled in a different way. We need to discuss whether to create some backwards compatibility layer.
* At the moment, this version brings [cljfx](https://github.com/cljfx/cljfx) as a dependency, merely for its state management logic. This means it will run only on JDK 11.
* Currently things tend to break when moving across several namespaces. For now, calling `(notespace.api/init)` is a good habit for such cases.

## Version 2

At the moment, Version 2 is used for documentation and testing at [ClojisR](https://github.com/scicloj/clojisr) and [ClojisR-examples](https://github.com/scicloj/clojisr-examples).

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
