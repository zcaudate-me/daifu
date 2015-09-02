(ns daifu.diagnostic.data
  (:require [hara.item :as item :refer [defgroup defitem]]))


;;(load-items :indicator     "indicators.edn"     indicator)
;;(load-items :jurisdication "jurisdications.edn" jurisdiction)
;;(defrecord Group [])
;;(defrecord Item [])


(comment
  (defgroup indicators {:tag :indicator
                        :key :name
                        :constructor indicator})

  (defitem indicators
    {:name :project-meta
     :type :project
     :dependencies []
     :function '(fn [project]
                  )})

  (defgroup jurisdiction {:tag :realm
                          :key :name
                          :constructor jurisdiction})

  (defitem jurisdiction
    {:name :hello-example
     :type :single
     :path "src/hello/example.clj"})


  (install-items indicators "indicators.edn")

  ;;(add-item :indicator :project-meta)
  ;;(remove-item :indicator :project-meta)

  (keys indicators))
