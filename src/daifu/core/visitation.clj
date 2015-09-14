(ns daifu.core.visitation
  (:require [daifu.diagnosis :as diagnosis]))

(defrecord Visitation [])

(defmethod print-method Visitation
  [v w]
  (.write w (str "#visit " (into {} (-> v
                                        (update-in [:indicators] (comp vec keys))
                                        (update-in [:jurisdictions] (comp vec keys)))))))

(defn visitation [opts]
  (map->Visitation opts))
