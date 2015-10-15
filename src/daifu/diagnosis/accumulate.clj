(ns daifu.diagnosis.accumulate
  (:require [daifu.diagnosis.result :as result])
  (:import daifu.diagnosis.result.Result
           java.util.List
           clojure.lang.APersistentMap))

(defmulti ->stat
  "creates a stat (of type Number) from various data types
   
   (->stat nil) => 0
   (->stat false) => 0
   (->stat true) => 1
   (->stat 10) => 10
   (->stat [1 2 3]) => 6
   (->stat (result [1 2 3])) => 6"
  {:added "0.1"}
  type)

(defn sum
  "calculates the sum of all data points in a list
 
   (total [true false true]) => 2
   (total [1 2 3 4]) => 10"
  {:added "0.1"}
  [xs]
  (apply + (map ->stat xs)))

(defn average
  "calculates the average of data points in a list
 
   (average [true false true false]) => 1/2
   (average [1 2 3 4 5]) => 3"
  {:added "0.1"}
  [xs]
  (if (empty? xs)
    0
    (/ (sum xs) (count xs))))

(defmethod ->stat nil
  [obj]
  0)

(defmethod ->stat Number
  [obj]
  obj)

(defmethod ->stat Boolean
  [obj]
  (if (true? obj) 1 0))

(defmethod ->stat Result
  [{:keys [value raw] :as result}]
  (cond value
        (->stat value)

        raw (->stat raw)

        :else 1))

(defmethod ->stat List
  [obj]
  (sum obj))

(defmethod ->stat APersistentMap
  [obj]
  (->stat (result/result obj)))

(defn group-distribute [results {:keys [keys] :as opts}]
  (reduce (fn [out result]
            (let [path (mapv #(get result %) keys)]
              (update-in out path (fnil (fn [res] (update-in res [:raw] conj (result/result result)))
                                        (result/result {:raw []})))))
          {}
          results))

(defn group-merge [groups {:keys [merge] :as opts}]
  (reduce-kv (fn [out k v]
               (if (result/result? v)
                 (assoc out k (assoc v :value ((or merge sum) (map ->stat (:raw v)))))
                 (assoc out k (group-merge v opts))))
             {}
             groups))

(defn group
  [results opts]
  (-> results
      (group-distribute opts)
      (group-merge opts)))

(defn accumulate
  [groups [func & more]]
  (if (empty? more)
    (let [raw   (vec (vals groups))
          value (func (map ->stat raw))]
      (result/result {:value value :raw raw}))
    (accumulate (reduce-kv (fn [out k v]
                             (assoc out k (accumulate v more)))
                           {}
                           groups)
                [func])))

(comment
  
  (-> (group [{:ns 'hello :value 1}
              {:ns 'hello :value 2}
              {:ns 'world :value 3}
              {:ns 'world :value 4}]
             {:keys  [:ns]
                      :merge sum
              :order [:value]})
      
      (accumulate [average]))
  => {:raw [{:raw [1 2], :value 3}
            {:raw [3 4], :value 7}],
      :value 5}

  (-> (group [{:ns 'hello :value 1}
              {:ns 'hello :value 2}
              {:ns 'world :value 3}
              {:ns 'world :value 4}]
             {:keys  [:ns]
                      :merge sum
              :order [:value]})
      
      (accumulate [sum]))
  => {:raw [{:raw [1 2], :value 3}
            {:raw [3 4], :value 7}],
      :value 10}
  
  (-> (group [{:ns 'hello :name 'a :value 1}
              {:ns 'hello :name 'b :value 2}
              {:ns 'world :name 'a :value 3}
              {:ns 'world :name 'a :value 4}]
             {:keys  [:ns :name]
              :merge sum})
      (accumulate [average average]))
  => {:raw [{:raw [{:raw [1], :value 1}
                   {:raw [2], :value 2}],
             :value 3/2}
            {:raw [{:raw [3 4],
                    :value 7}],
             :value 7}],
      :value 17/4}
  
  

  
  
  )


(comment
  
  
  (defn evaluate [groups])
  

  
  
  (group [{:ns 'hello :value 1}
          {:ns 'hello :value 2}
          {:ns 'world :value 3}
          {:ns 'world :value 4}]
         {:group {:keys  [:ns]
                  :merge sum
                  :order [:value]}
          :evaluate {:group {:default {:keys  [:file]
                                       :merge sum
                                       :order [:value]}
                             
                             :lines-per-file {:keys []}}
                     
                     :fn '(/ [:arithmatic]
                             (+ [:arithmatic]
                                [:collection]))} 
          
          :accumulate {}})

  
  {hello {:value 3, :raw [{:value 1} {:value 2}]}, world {:value 7, :raw [{:value 3} {:value 4}]}}
  
  
  (group (mapv result/result [{:ns 'hello :value 1}
                              {:ns 'hello :value 2}
                              {:ns 'world :value 3}
                              {:ns 'world :value 4}])
         {:group-by [:ns]
          :merge-by sum
          :order-by [:value]})

  => {'hello {:value 3
              :raw [{:ns 'hello :value 1}
                    {:ns 'hello :value 2}]}
      'world {:value 7
              :raw [{:ns 'world :value 3}
                    {:ns 'world :value 4}]}}


  (accumulate (mapv result/result [{:ns 'hello :name 'a :value 1}
                                   {:ns 'hello :name 'b :value 2}
                                   {:ns 'world :name 'a :value 3}
                                   {:ns 'world :name 'a :value 4}])
              {:group-by [:ns :name]
               :merge-by average})
  
  => {'hello {'a {:value 1
                  :raw [{:ns 'hello :name 'a :value 1}]}
              'b {:value 2
                  :raw [{:ns 'hello :name 'b :value 2}]}}
      'world {'a {:value 3.5
                  :raw [{:ns 'world :name 'a :value 3}
                        {:ns 'world :name 'a :value 4}]}}}
  

  (accumulate [{:ns 'hello :name 'a :value 1}
               {:ns 'hello :name 'b :value 2}
               {:ns 'world :name 'a :value 3}
               {:ns 'world :name 'a :value 4}]
              {:group-by [[:name :average] [:ns :sum]]})
  => {'a {'hello {:value 1
                  :raw [{:ns 'hello :name 'a :value 1}]}
          'world {:value 7
                  :raw [{:ns 'world :name 'a :value 3}
                        {:ns 'world :name 'a :value 4}]}}
      'b {'hello {:value 2
                  :raw [{:ns 'hello :name 'b :value 2}]}}}
  )
