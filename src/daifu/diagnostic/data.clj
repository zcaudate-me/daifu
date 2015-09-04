(ns daifu.diagnostic.data
  (:require [hara.group :as group :refer [defgroup defitem]]
            [hara.namespace.eval :as ns]
            [alembic.still :as still]))

(defrecord Indicator []
  clojure.lang.IFn
  (invoke [obj input]
          (if-not (activated? obj)
            (activate obj))
          (@(:main obj) input)))

(defn indicator [m]
  (-> (map->Indicator m)
      (assoc :main (atom nil))))

(defgroup indicators {:tag :indicator
                      :constructor indicator})

(defmulti activate-indicator :type)

(defmethod activate-indicator
  :function
  [indicator]
  (still/distill (:dependencies indicator))
  (ns/eval-with-temp-ns
   (conj (apply vector
                '(require '[rewrite-clj.zip :as zip]
                          '[rewrite-clj.node :as node])
                (:injections indicator))
         (:source indicator))))

(defmethod activate-indicator
  :file
  [indicator]
  (still/distill (:dependencies indicator))
  (ns/eval-with-temp-ns
   (conj (apply vector
                '(require '[clojure.java.io :as io])
                (:injections indicator))
         (:source indicator))))

(defmethod activate-indicator
  :project
  [indicator]
  (still/distill (:dependencies indicator))
  (ns/eval-with-temp-ns
   (conj (apply vector
                '(require '[leiningen.core.project :as project])
                (:injections indicator))
         (:source indicator))))

(defn activate
  [indicator]
  (if-not @(:main indicator)
    (reset! (:main indicator) (activate-indicator indicator)))
  indicator)

(defn activated?
  [indicator]
  (not (nil? @(:main indicator))))


(def ind1 
  (indicator {:type :function
              :dependencies '[[im.chit/hara.common.checks "2.2.7"]]
              :injections '[(require '[hara.common.checks :as checks])]
              :source '(fn [x] (checks/hash-map? x))}))

(ind1 1)
(ind1 {})

(require [])
(comment
  (def hmap? (activate-indicator id))
  (hmap? 1)
  (hmap? )

  (ns/eval-with-temp-ns
   '[(require '[hara.class.checks :as checks])
     (fn [x] (checks/long? x))])

  (create-ns 'example)
  (ns/eval-with-ns 'example
   '[(require '[hara.class.checks :as checks])
     (fn [x] (checks/long? x))])
  (ns-resolve (namespace "example"))
  )

(ns-resolve (create-ns 'example) 'checks/long?)
(ns-aliases (create-ns 'example))
{map #object[clojure.lang.Namespace 0x483e77e8 "hara.data.map"], checks #object[clojure.lang.Namespace 0x7d324565 "hara.class.checks"]}

(binding [*ns* (the-ns 'example)]
  (eval '(checks/long? 3)))

(binding [*ns* (the-ns 'hara.common.checks)]
  (eval '(long? 3)))

(./doc namespace)

(ns-publics (the-ns 'example))


(ns/with-temp-ns
  (require '[hara.class.checks :as checks])
  (fn [x] (checks/hash-map? x)))

(ns/with-temp-ns
  (require '[hara.class.checks :refer :all])
  (fn [x] (hash-map? x)))


(ns/with-ns 'example
  (clojure.core/refer-clojure)
  (require '[hara.common.checks :as checks]))

(filter (fn [[k v]]
          (not= 'clojure.core (symbol (namespace (symbol (apply str (drop 2 (str v)))))))) (ns-refers 'example))
([abstract? #'hara.class.checks/abstract?] [multimethod? #'hara.class.checks/multimethod?] [protocol? #'hara.class.checks/protocol?] [interface? #'hara.class.checks/interface?])



(create-ns 'example)
(remove-ns 'example)

(ns-publics 'example)
(ns-aliases 'example)
(ns-resolve (the-ns 'example) 'hash-map?)
(get (ns-map (the-ns 'example)) 'hash-map?)

(ns-resolve (the-ns 'example) 'hash-map?)
(ns-resolve (the-ns 'example) 'checks/hash-map?)

(comment
  (./pull '[jonase/kibit "0.1.2"])
  
  (defgroup indicators {:tag :indicator
                        :key :name
                        :constructor indicator})

  (defitem indicators 
    {:name :project-meta
     :type :project
     :dependencies []
     :function '(fn [project]
                  )})

  (defgroup jurisdiction {:tag :jurisdiction
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
