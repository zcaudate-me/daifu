(ns daifu.diagnosis
  (:require [daifu.diagnosis.indicator :as indicator]
            [daifu.diagnosis.target :as target]))

(defn accumulate [results indicator])

(defn diagnose [{:keys [repo] :as global} indicator target])

(comment

  {:repo    {}
   :outputs {:var   {}
             :file  {}
             :all   {}}}
  

  )
