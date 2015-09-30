(ns documentation.daifu-guide)

[[:chapter {:title "Introduction"}]]

"[daifu](https://www.github.com/helpshift/daifu) is a framework for defining, measuring, aggregating and visualising statistics for a given codebase. The basis of the library is a generic toolkit that comes with built-in defaults as well as a plugin architecture to allow customisation of statistical reporting to suit the needs of an individual project.

The origin of the name 大*(dai)* 夫*(fu)* is chinese and means 'doctor'."


[[:section {:title "Motivation"}]]

"The motivation for [daifu](https://www.github.com/helpshift/daifu) is to have a tool that integrates into the code review process. Regular code reviews have the following effect in the order of increasing importance:

1. Maintaining consistency of quality and idiomatic style
- Knowledge transfer and increased team awareness
- Finding alternative solutions and problem solving

If part of the code review process can be automated through a tool that can check for consistency of quality and idiomatic style, then the more important aspects of the process can be put into focus. Furthermore, by using a combination of statistics as well as looking at key indicators of code over time, we can determine, the healthiness of our codebase.
"

[[:chapter {:title "Quickstart"}]]

[[:section {:title "Installation"}]]

"The `lein-daifu` leiningen plugin can be installed by adding to the `[:profiles :dev :plugins]` entry of the `project.clj`:

```clojure
(defproject ...
    ...
    :profiles {:dev {:plugins [...
                               [lein-daifu \"{{PROJECT.version}}\"]
                               ...]}}
    ...)
```
"

[[:section {:title "Running"}]]

"Lets begin by typing `lein daifu` in our project:

```shell
$ lein daifu

Usage: lein daifu -i qa/indicators -j qa/jurisdictions -f json -o output.json -c qa/checkup.daifu

Options
  -p, --path PATH               /Users/chris/Development/helpshift/daifu  Path to the repository
  -c, --checkups-file PATH                                                Path to the checkups file that will be run
  -f, --format FORMAT           :edn                                      Format of output {edn|json}
  -i, --indicator-paths DIR                                               Directory to load indicators
  -j, --jurisdiction-paths DIR                                            Directory to load jurisdictions
  -o, --output FILE                                                       Path for output file
      --info                                                              Show this screen
      --use-git                                                           Load files from git
```"



[[:chapter {:title "Custom Indicators"}]]

"Lets look at writing some custom indicators for our project"
