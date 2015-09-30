(ns leiningen.daifu
  (:require [daifu.core.cli :as cli]))

(defn daifu
  [project & args]
  (cli/cli-main args))
