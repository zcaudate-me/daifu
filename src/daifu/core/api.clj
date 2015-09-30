(ns daifu.core.api
  (:require [clojure.java.io :as io]
            ;;[gita.core :as git]
            ;;[cheshire.core :as json]
            ;;[clojure.pprint :as pprint]
            ;;[daifu.diagnosis :as diagnosis]
            ;;[daifu.diagnosis.indicator :as indicator]
            ;;[daifu.diagnosis.jurisdiction :as jurisdiction]
            ))

(defn run [opts]
  (println "Running with" opts)
  #_(let [opts  (dissoc opts :diagnosis)
          visit (visitation opts)]
      (diagnosis visit (:checkups visit))))
