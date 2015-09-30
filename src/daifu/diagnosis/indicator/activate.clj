(ns daifu.diagnosis.indicator.activate
  (:require [hara.namespace.eval :as ns]
            [alembic.still :as still]))

(defmulti create-function (fn [indicator] (:type indicator)))

(defn activation [indicator]
  (let [state (-> indicator :main deref)]
    (cond (nil? state) :unactivated
          (fn? state)  :activated
          :else        :errored)))

(defn create-function-helper
  ([indicator requires]
   (create-function-helper indicator requires :source))
  ([{:keys [dependencies] :as indicator} requires func]
   (if dependencies (still/distill dependencies))
   (ns/eval-temp-ns
    (conj (apply vector
                 (->> requires
                      (map #(list 'quote %))
                      (apply list 'require))
                 (:injections indicator))
          (func indicator)))))

(defmethod create-function
  :form
  [indicator]
  (create-function-helper indicator '[[rewrite-clj.zip :as zip]
                                      [rewrite-clj.node :as node]]))

(defmethod create-function
  :function
  [indicator]
  (create-function-helper indicator '[[rewrite-clj.zip :as zip]
                                         [rewrite-clj.node :as node]]))

(defmethod create-function
  :file
  [indicator]
  (create-function-helper indicator '[[clojure.java.io :as io]]))

(defmethod create-function
  :project
  [indicator]
  (create-function-helper indicator '[[leiningen.core.project :as project]]))

(defn compiled-rules
  [indicator]
  (mapv (fn [rule]
          (cond (vector? rule)
                (list 'kibit.rules.util/compile-rule (list 'quote rule))

                :else
                (list 'eval rule)))
        (:rules indicator)))

(defn convert-meta
  [{:keys [line column] :as m}]
  (-> (dissoc m :line :column)
      (assoc :row line :col column)))

(defmethod create-function
  :idiom
  [indicator]
  (create-function-helper indicator '[[clojure.core.logic :as logic]
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

(defn activate
  [indicator]
  (if (= (activation indicator)
         :unactivated)
    (try (let [main (create-function indicator)]
           (reset! (:main indicator) main))
         (catch Throwable t
           (reset! (:main indicator) {:error t}))))
  indicator)
