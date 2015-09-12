(ns daifu.core
  (:require [clojure.tools.cli :as cli]
            [clojure.java.io :as io]))

(def cli-options
  [["-i" "--indicators DIR" "Directory to load indicators"]
   ["-j" "--jurisdictions DIR" "Directory to load jurisdictions"]
   [nil  "--no-defaults" "Do not load default indicators"]
   ["-b" "--branch BRANCH" "Branch name"]
   ["-c" "--commit COMMIT" "Commit sha, time or relative tag (HEAD^1)"]])



(defrecord Visitation [])

(defn visitation [path options])

(comment

  (slurp (io/resource "daifu/defaults/indicators/arithmatic.indi"))
  (./pull-project)
  )
