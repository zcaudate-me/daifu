(ns daifu.diagnosis.indicator.activate
  (:require [hara.namespace.eval :as ns]
            [alembic.still :as still]))

(defmulti create-function
  "creates a function from the format for defining indicators
 
   ((create-function {:id :count-example
                      :type :function
                      :source '(fn [zloc]
                                 (count (zip/sexpr zloc)))})
    (zip/of-string \"(defn add [])\"))
   => 3"
  {:added "0.1"}
  (fn [indicator] (:type indicator)))

(defn activation
  "returns the activation state for an indicator
 
   (-> (indicator/indicator {:id :count-example
                             :type :function
                             :source '(fn [zloc]
                                        (count (zip/sexpr zloc)))})
       (activation))
   => :unactivated"
  {:added "0.1"}
  [indicator]
  (let [state (-> indicator :main deref)]
    (cond (nil? state) :unactivated
          (fn? state)  :activated
          :else        :errored)))

(defn create-function-helper
  "the workhorse for creating the indicator main function"
  {:added "0.1"}
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
                                      [rewrite-clj.node :as node]
                                      [daifu.diagnosis.indicator.result :as result]]))

(defmethod create-function
  :function
  [indicator]
  (create-function-helper indicator '[[rewrite-clj.zip :as zip]
                                      [rewrite-clj.node :as node]
                                      [daifu.diagnosis.indicator.result :as result]]))

(defmethod create-function
  :file
  [indicator]
  (create-function-helper indicator '[[clojure.java.io :as io]
                                      [daifu.diagnosis.indicator.result :as result]]))

(defmethod create-function
  :project
  [indicator]
  (create-function-helper indicator '[[leiningen.core.project :as project]
                                      [daifu.diagnosis.indicator.result :as result]]))

(defn compiled-rules
  "creates the kibit form for compiled-rules
 
   (-> (indicator/indicator {:id    :plus-one
                   :type  :idiom
                   :rules '[[(+ ?x 1) (inc ?x)]]})
       (compiled-rules))
   => '[(kibit.rules.util/compile-rule (quote [(+ ?x 1) (inc ?x)]))]"
  {:added "0.1"}
  [indicator]
  (mapv (fn [rule]
          (cond (vector? rule)
                (list 'kibit.rules.util/compile-rule (list 'quote rule))

                :else
                (list 'eval rule)))
        (:rules indicator)))

(defn convert-meta
  "converts the kibit meta to be daifu compatible
 
   (convert-meta {:line 3 :column 4})
   => {:row 3, :col 4}"
  {:added "0.1"}
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
                                      [daifu.diagnosis.indicator :refer [convert-meta]]
                                      [daifu.diagnosis.indicator.result :as result]]
                             (fn [indicator]
                               (list 'fn '[reader]
                                     (list '->>
                                           (list 'check/check-reader 'reader
                                                 :rules (compiled-rules indicator))
                                           (list 'map 'convert-meta))))))

(defn activate
  "activates an indicator for use for invocation
 
   (-> (indicator/indicator {:id :count-example
                             :type :function
                             :source '(fn [zloc]
                                        (count (zip/sexpr zloc)))})
       (activate)
       (activation))
  => :activated"
  {:added "0.1"}
  [indicator]
  (if (= (activation indicator)
         :unactivated)
    (try (let [main (create-function indicator)]
           (reset! (:main indicator) main))
         (catch Throwable t
           (reset! (:main indicator) {:error t}))))
  indicator)
