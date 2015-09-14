(ns daifu.core
  (:require [clojure.tools.cli :as cli]
            [clojure.java.io :as io]))

(def cli-options
  [["-b" "--branch BRANCH" "Branch name"]
   ["-c" "--commit COMMIT" "Commit sha, time or relative tag (HEAD^1)"]
   ["-d" "--diagnostic PATH" "Path to the diagnostic that will be run"]
   ["-f" "--format FORMAT" "Format of output {edn|json|html}"]
   ["-i" "--indicators DIR" "Directory to load indicators"]
   ["-j" "--jurisdictions DIR" "Directory to load jurisdictions"]
   ["-o" "--output PATH" "Path for output file"]
   [nil  "--no-defaults" "Do not load default indicators"]])


(-> (cli/parse-opts ["-b" "gh-pages" "--no-defaults"] cli-options)
    :options)


(defrecord Visitation [])

(defn visitation [path options])

(comment

  (slurp (io/resource "daifu/defaults/indicators/arithmatic.indi"))
  (./pull-project)
  )
