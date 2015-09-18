(ns daifu.diagnosis.indicator
  (:require [hara.namespace.eval :as ns]
            [alembic.still :as still]))

(defmulti activate-indicator :type)

(defn activate
  [indicator]
  (if-not @(:main indicator)
    (reset! (:main indicator)
            (activate-indicator indicator)))
  indicator)

(defn activated?
  [indicator]
  (not (nil? @(:main indicator))))

(defn invoke-indicator
  [indicator & inputs]
  (if-not (activated? indicator)
    (activate indicator))
  (apply @(:main indicator) inputs))

(defrecord Indicator []
  clojure.lang.IFn
  (invoke [indicator input]
          (invoke-indicator indicator input))
  (invoke [indicator x y]
          (invoke-indicator indicator x y)))

(defmethod print-method Indicator
  [v w]
  (+ 1 1)
  (.write w (str "#" (name (:type v)) [(name (:id v))] " "
                 (-> {:activated (activated? v)}
                     (assoc :* (vec (keys (dissoc v :id :type :main))))))))

(defn indicator [m]
  (-> (map->Indicator m)
      (assoc :main (atom nil))))

(defn activate-indicator-helper
  ([indicator requires]
   (activate-indicator-helper indicator requires :source))
  ([{:keys [dependencies] :as indicator} requires func]
   (if dependencies (still/distill dependencies))
   (ns/eval-temp-ns
    (conj (apply vector
                 (->> requires
                      (map #(list 'quote %))
                      (apply list 'require))
                 (:injections indicator))
          (func indicator)))))

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

(defn compiled-rules [indicator]
  (mapv (fn [rule]
          (cond (vector? rule)
                (list 'kibit.rules.util/compile-rule (list 'quote rule))

                :else
                (list 'eval rule)))
        (:rules indicator)))

(defn convert-meta [{:keys [line column] :as m}]
  (-> (dissoc m :line :column)
      (assoc :row line :col column)))

(defmethod activate-indicator
  :idiom
  [indicator]
  (activate-indicator-helper indicator '[[clojure.core.logic :as logic]
                                         [clojure.core.logic.unifier :as unifier]
                                         [kibit.rules.util :as rules]
                                         [kibit.check :as check]
                                         [daifu.diagnosis.indicator :refer [convert-meta]]]
                             (fn [indicator]
                               (list 'fn '[reader]
                                     (list '->>
                                           (list 'check/check-reader 'reader
                                                 :rules (compiled-rules indicator))
                                           (list 'map 'convert-meta))))))
