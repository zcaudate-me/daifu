(ns leiningen.daifu
  (:require [daifu.core :as daifu]))

(defn daifu
  [project & args]
  (apply daifu/-main args))
