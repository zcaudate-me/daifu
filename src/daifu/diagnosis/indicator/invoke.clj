(ns daifu.diagnosis.indicator.invoke
  (:require [daifu.diagnosis.indicator.activate :as activate]
            [daifu.diagnosis.indicator.result :as result]
            [hara.function.args :as args]))

(declare invoke)

(defn stat-function
  "provides the function for merging statistical data
   
   ((stat-function {}) [1 2 3 4]) => 10
 
   ((stat-function {:accumulate {:default :count}}) [1 2 3 4])
   => 4
 
   ((stat-function {:accumulate {:stat :average
                                 :default :count}}) [1 2 3 4])
   => 5/2"
  {:added "0.1"}
  [{:keys [accumulate] :as indicator}]
  (let [type (or (:stat accumulate)
                 (:default accumulate)
                 :auto)]
    (case type
      :auto    result/->stat
      :average result/average
      :total   result/total
      :count   count)))

(defn invoke-arglist
  "invokes dependents of a particular indicator
   (invoke-arglist +global+
                   (-> +global+ :indicators :too-many-tokens)
                   [:token-count]
                   (zip/of-string \"(defn add [])\"))
   => '(4)"
  {:added "0.1"}
  ([global indicator arglist input]
   (invoke-arglist global indicator arglist input nil))
  ([global indicator arglist input options]
   (->> arglist
        (reduce (fn [out k]
                  (let [sub (-> global :indicators k)]
                    (cond (= (:type sub) (:type indicator))
                          (conj out (invoke global sub input options))

                          :else
                          (throw (Exception. "NOT YET IMPLEMENTED")))))
                [])
        (map :data))))

(defn invoke
  "invokes an indicator with reference to a global datastructure
   (invoke +global+
           (-> +global+ :indicators :token-count)
           (zip/of-string \"(defn add [])\")
           nil)
   => (contains {:data 4, :stat 4})
 
   (invoke +global+
           (-> +global+ :indicators :too-many-tokens)
           (zip/of-string \"(defn add [])\")
           nil)
   => (contains {:data false, :stat 0})
 
   (invoke +global+
           (-> +global+ :indicators :too-many-tokens)
           (zip/of-string \"(defn add [])\")
           {:limit 2})
   => (contains {:data true, :stat 1})"
  {:added "0.1"}
  ([global indicator input]
   (invoke global indicator input nil))
  ([global indicator input options]
   (let [main   (-> indicator :main deref)
         fargs  (apply max (args/arg-count main))
         options (merge (:default indicator)
                        (-> global :current :pair second)
                        options)
         args   (invoke-arglist global indicator (:arglist indicator) input options)
         slots  (- fargs (count args))
         inputs (case slots
                  0 []
                  1 [input]
                  2 [input options])]
     (result/result (apply main (concat args inputs))
                    (stat-function indicator)))))
