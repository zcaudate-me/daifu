(ns documentation.daifu-guide)

[[:chapter {:title "Introduction"}]]

"[daifu](https://www.github.com/helpshift/daifu) is a tool for checking code quality that integrates into the code review process. Regular code reviews have the following effect in the order of increasing importance:

1. Maintaining consistency of quality and idiomatic style
- Knowledge transfer and increased team awareness
- Finding alternative solutions and problem solving

If part of the code review process can be automated through a tool that can check for consistency of quality and idiomatic style, then the more important aspects of the process can be put into focus. Furthermore, by using a combination of statistics as well as looking at key indicators of code over time, we can determine, the healthiness of our codebase.

If we can provide metrics and views on how a system is performing over time, then it will be in our best interest to make it better and better. The origin of the name 大*(dai)* 夫*(fu)* is chinese and means 'doctor'.
"

[[:section {:title "Installation"}]]

"In your `project.clj`, add [daifu](https://www.github.com/helpshift/daifu) to the `[:profiles :dev :dependencies]` entry:

```clojure
(defproject ...
    ...
    :profiles {:dev {:dependencies [...
                                    [helpshift/daifu \"{{PROJECT.version}}\"]
                                    ...]}}
    ...)
```
"

"All functionality is the `daifu.core` namespace:"

(comment
  (use 'daifu.core))

[[:chapter {:title "Usage"}]]

[[:section {:title "In Repl"}]]

"The following shows the usage of daifu for a particular project"

(comment
  (use 'daifu.core))

(comment
    
  (-> (visitation "path/to/project.clj"
                  {:indicators    ["qa/indicators"] ; name of a folder
                   :jurisdictions ["qa/jurisdictions"] ; name of a folder"
                   ;;:run :everything
                   :defaults {:multi   :multi-example
                              :single  :hello-example}})

      (diagnosis [:project-meta]
                 [:project-dependencies]
                 [:project-newest]
                 [:arithmetic-forms :hello-example]
                 [:arithmetic-forms :world-example]
                 [:equality-forms   :hello-example]
                 [:equality-forms   :multi-example])
      
      (presentation {:format :html})))

[[:section {:title "Leiningen"}]]

"[daifu](https://www.github.com/helpshift/daifu) will also be avaliable as a leiningen plugin."

[[:code {:lang "shell"}
"
% lein daifu -i qa/indicators -j qa/jurisdictions 
             -d qa/diagnostic.edn -f json -o out.json"]]

"Will output in `out.json` something similar to:"

[[:code {:lang "javascript"}
"
{\"created\": \"Tue Sep 01 2015 15:15:30 GMT+0530 (IST)\"
 \"diagnosis\": [{\"name\": \"project-meta\"
                \"realm\": \"default\"
                \"output\": { ... }}]}"]]



[[:chapter {:title "Indicators"}]]

"Indicators are created via the following calls:"

(comment
  {:name :<INDICATOR>
   :type #{:idiom :file :project}

   ... indicator type dependent characteristics ...})


[[:section {:title "idiom indicator"}]]

"`:idiom` indicators are defined as follows:"

(comment
  (add-item +indicators+
            {:name  :arithmatic-forms
             :type  :idiom
             :rules [[(+ ?x 1) (inc ?x)]
                     [(+ 1 ?x) (inc ?x)]
                     [(- ?x 1) (dec ?x)]
                     
                     [(* ?x (* . ?xs)) (* ?x . ?xs)]
                     [(+ ?x (+ . ?xs)) (+ ?x . ?xs)]

                     ;;trivial identites
                     [(+ ?x 0) ?x]
                     [(- ?x 0) ?x]
                     [(* ?x 1) ?x]
                     [(/ ?x 1) ?x]
                     [(* ?x 0) 0]
                     
                     ;;Math/hypot
                     [(Math/sqrt (+ (Math/pow ?x 2)
                                    (Math/pow ?y 2)))
                      (Math/hypot ?x ?y)]
                     
                     ;;Math/expm1
                     [(dec (Math/exp ?x))
                      (Math/expm1 ?x)]

                     ;;ugly rounding tricks
                     [(long (+ ?x 0.5))
                      (Math/round ?x)]]}))

[[:section {:title "project indicator"}]]

"`:project` indicators are defined as follows:"

(comment
  (add-item +indicators+
            {:name  :wrong-arity
             :level 3
             :group :syntax
             :type  :project
             :dependencies [[jonase/eastwood "0.2.1"]]
             :injections [(require [eastwood.lint :as e])]
             :main (fn [project]
                     (e/lint {:source-paths ["src"] :test-paths ["test"]}))}))


[[:section {:title "file indicator"}]]

"`:file` indicators are defined as follows:"

(comment
  (add-item +indicators+
            {:name  :word-count
             :type  :file
             :dependencies []
             :injections []
             :main (fn [file]
                     ....)}))


[[:section {:title "function indicator"}]]

"`:function` indicators are defined as follows:"

(comment
  (add-item +indicators+
            {:name  :has-docstring-check
             :type  :function
             :dependencies []
             :injections []
             :main (fn [zloc]
                     ....)}))


[[:chapter {:title "Jurisdictions"}]]

"Jurisdictions are domains which indicators are active. Sometimes, we want to get information about a single file, whilst other times we want information about all the files within the project, or some aspect of the project itself."

[[:section {:title "file"}]]

"`:file` jurisdictions allow the specification of a single path to a file"

(comment
  (add-item +jurisdictions+
            {:name  :hello-example
             :type  :file
             :path  "src/hello/example.clj"}))


[[:section {:title "multi"}]]

"`:multi` jurisdictions allow the specification of a set of files as well as a set of patterns"

(comment
  (add-item +jurisdictions+
            {:name  :multi-example
             :type  :multi
             :files ["src/hello/example.clj"
                     "src/hello/world.clj"]}))

(comment
  (add-item +jurisdictions+
            {:name  :pattern-example
             :type  :multi
             :patterns [#"src/hello"]}))


[[:section {:title "project"}]]

(comment
  (add-item +jurisdictions+
            {:name  :hello-project
             :type  :project
             :exclude {:src-files  []
                       :test-files []}}))

[[:chapter {:title "Diagnosis"}]]

"The `diagnosis` function allows for a set of indicator/jurisdiction pairs to be specified."

[[:chapter {:title "Presentation"}]]

"The `presentation` function allows for statistics to be generated in json/html for consumption by other devices."


[[:chapter {:title "Specification"}]]

"Lets first define a `project.clj` template:"

(comment
  (defproject temp "0.1.0-SNAPSHOT"
    :description "FIXME: write description"
    :url "http://example.com/FIXME"
    :license {:name "Eclipse Public License"
              :url "http://www.eclipse.org/legal/epl-v10.html"}
    :dependencies [[org.clojure/clojure "1.6.0"]
                   [org.clojure/test.check "0.5.8"]
                   [criterium "0.4.2"]]
    :profiles {:dev {:dependencies [[midje "1.6.0"]]}}))

[[:section {:title "project-meta indicator"}]]

"The project-meta indicator will look at related project metadata"

(comment
  (add-item +indicators+
            {:name  :project-meta
             :type  :project
             :dependencies []
             :injections []
             :main (fn [project]
                     ....)})

  (-> (visitation "path/to/project.clj")      
      (diagnosis [:project-meta :default-project]))

  => {:diagnosis [{:indicator :project-meta
                   :jurisdiction :default-project
                   :stat 2
                   :results [{:file "project.clj"
                              :line 2
                              :msg "please add proper description"}
                             {:file "project.clj"
                              :line 3
                              :msg "please add proper url"}]}]})

[[:section {:title "project-dependencies indicator"}]]

"The project-meta indicator will look at resolving project dependencies:"

(comment
  (add-item +indicators+
            {:name  :project-dependencies
             :type  :project
             :dependencies []
             :injections []
             :main (fn [project]
                     ....)})

  (-> (visitation "path/to/project.clj")      
      (diagnosis [:project-dependencies :default-project]))

  => {:diagnosis [{:indicator :project-dependencies
                   :jurisdiction :default-project
                   :stat 1
                   :results [{:file "project.clj"
                              :line 7
                              :msg "org.clojure/test.check is only used inside test directory. Move it to dev dependencies instead"}]}]})

[[:section {:title "project-newest indicator"}]]

"The project-newest indicator will look at getting the newest project."


(comment
  (add-item +indicators+
            {:name  :project-newest
             :type  :project
             :dependencies []
             :injections []
             :main (fn [project]
                     ....)})

  (-> (visitation "path/to/project.clj")      
      (diagnosis [:project-newest :default-project]))

  => {:diagnosis [{:indicator :project-newest
                   :jurisdiction :default-project
                   :stat 1
                   :results [{:file "project.clj"
                              :line 8
                              :msg "criterium can be upgraded to version 0.4.3"}]}]})

[[:section {:title "ns indicator"}]]

"This indicator looks at the namespace form for each file and catches:

- bad usages of `:refer`, i.e `:refer :all` is not allowed.
- removal of direct references
- removal of unsued libraries
- warning for instances of `:use`"

(comment
  (add-item +indicators+
            {:name  :ns-indicator
             :type  :file
             :dependencies [[slamhound "1.5.5"]]
             :injections [(use 'slam.hound)]
             :main (fn [file]
                     ....)})

  (-> (visitation "path/to/project.clj")      
      (diagnosis [:ns-indicator :default-project]))

  => {:diagnosis [{:indicator :ns-indicator
                   :jurisdiction :default-project
                   :results [{:file "src/hello/example.clj"
                             :stat 2
                             :results {}}
                            {:file "src/world/example.clj"
                             :stat 0
                             :results {}}
                            {:file "src/again/example.clj"
                             :stat 0
                             :results {}}]}]})


[[:section {:title "docstring indicator"}]]

"This indicator looks for a docstring"

(comment
  (add-item +indicators+
            {:name  :docstring-indicator
             :type  :function
             :dependencies []
             :injections []
             :main (fn [zloc]
                     ...)})

  (-> (visitation "path/to/project.clj")      
      (diagnosis [:docstring-indicator :hello-example]))

  => {:extra {:timestamp <>}
      :diagnosis [{:indicator :docstring-indicator
                   :jurisdiction :hello-example
                   :results [{:file "src/hello/example.clj"
                              :namespace hello.example
                              :results [{:function add-n
                                         :stat true
                                         :summary {:line 3}}
                                        {:function minus-n
                                         :stat false
                                         :summary {:line 5}}]}]}]})
