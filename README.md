# notespace

Notebook experience in your Clojure namespace

[![Clojars Project](https://img.shields.io/clojars/v/scicloj/notespace.svg)](https://clojars.org/scicloj/notespace)

## Status

Update, 2022-04-24: The Notespace project is currently on hold.

### The broader picture

The field of Clojure visual tools is growing rapidly, and it seems that there are very basic questions to be figured out about the future directions: compatibility and conventions across tools, and a lightweight workflow to answer basic needs of sharing and reusing code examples. 

Some of these questions are addressed by a separate project -- [Clay](https://github.com/scicloj/clay), which is the current focus of daslu, the Notespace maintainer.

A few of us meet regularly at the [visual-tools group](https://scicloj.github.io/docs/community/groups/visual-tools/) to collaborate on those issues and more exciting ones.

### Clay

Clay is similar to Notespace in several aspects, such as relying on [Kindly](https://github.com/scicloj/kindly) for specifying the kinds of visualizations. It is rather easy to convert Notespace projects to Clay projects.

Clay offers a more basic, and hopefully simple and robust, approach. Notespace addresses more delicate questions of user interaction, that seem less pressing to solve. They could still be useful, and hopefully, we will come back to them.

If you are writing a new project, it is recommended to try Clay. If you are maintaining existing projects with Notespace, please reach out, and we could discuss how to support your needs.

### Notespace versions

Version 4 is the most recent version and is in alpha stage.

Version 3 and Version 2 have been used in some projects. We are not planning to develop them further, but please reach out if you need any support.

## What is it?

This tool is an attempt to answer the following question: can we have a notebook-like experience in Clojure without leaving one's favourite editor?

See this recorded [Overview](https://www.youtube.com/watch?v=uICA2SDa-ws).

## Versions

Notespace has been evolving gradually, slowly realizing some lessons of usage in the Scicloj study groups, in individual research projects, and in documenting some of the Scicloj libraries.

* [Version 4](doc/v4.md) -- please go here if you are new to Notespace

* [Version 3](doc/v3.md)

* [Version 2](doc/v2.md)

## Setup and Usage

See details in the dedicated version pages linked above.

## Discussion

Hearing your comments, opinions and wishes will help!

[#notespace-dev at the Clojurians Zulip](https://clojurians.zulipchat.com/#narrow/stream/224153-notespace-dev).

## Relation to other projects

There are several magnificent existing options for literate programming in Clojure: Marginalia, Org-Babel, Gorilla REPL, Oz, Saite, Clojupyter, Nextjournal, Pink-Gorilla/Goldly, Clerk. Most of them are actively developed.

Creating a separate alternative would be the least desired outcome of the current project. Rather, the hope is to compose and integrate well with some of the other projects. 

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
