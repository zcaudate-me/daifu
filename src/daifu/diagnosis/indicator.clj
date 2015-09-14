(ns daifu.core.indicator
  (:require [hara.namespace.eval :as ns]
            [alembic.still :as still]))

(defmulti activate-indicator :type)

(defn activate
  [indicator]
  (if-not @(:main indicator)
    (reset! (:main indicator) (activate-indicator indicator)))
  indicator)

(defn activated?
  [indicator]
  (not (nil? @(:main indicator))))

(defn invoke-indicator
  [indicator input]
  (if-not (activated? obj)
    (activate obj))
  (@(:main obj) input))

(defrecord Indicator []
  clojure.lang.IFn
  (invoke [indicator input]
          (invoke-indicator indicator input)))

(defmethod print-method Indicator
  [v w]
  (.write w (str "#" (name (:type v)) " "
                 (into {} (-> (dissoc v :type :main :source :dependencies :injections)
                              (assoc :activated (activated? v))
                              (assoc :* (vec (keys (dissoc v :id :type :main)))))))))

(defn indicator [m]
  (-> (map->Indicator m)
      (assoc :main (atom nil))))

(defn activate-indicator-helper
  [indicator requires]
  (still/distill (:dependencies indicator))
  (ns/eval-temp-ns
   (conj (apply vector
                (->> requires
                     (map #(list 'quote %))
                     (apply list 'require))
                (:injections indicator))
         (:source indicator))))

(defmethod activate-indicator
  :form
  [indicator]
  (activate-indicator-helper indicator '[[rewrite-clj.zip :as zip]
                                         [rewrite-clj.node :as node]]))

(defmethod activate-indicator
  :function
  [indicator]
  (activate-indicator-helper indicator '[[rewrite-clj.zip :as zip]
                                         [rewrite-clj.node :as node]]))

(defmethod activate-indicator
  :file
  [indicator]
  (activate-indicator-helper indicator '[[clojure.java.io :as io]]))

(defmethod activate-indicator
  :project
  [indicator]
  (activate-indicator-helper indicator '[[leiningen.core.project :as project]]))

(comment
  (require '[rewrite-clj.zip :as zip])

  ((indicator {:id :hello-world
               :type :function
               :source '(fn [zloc] (zip/sexpr zloc))})
   (zip/of-string "(+ 1 1)"))



  )
