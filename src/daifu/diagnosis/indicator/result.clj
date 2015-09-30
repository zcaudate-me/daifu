(ns daifu.diagnosis.indicator.result
  (:import java.util.List))

(defrecord Result [])

(defmethod print-method Result
  [v w]
  (.write w (str "#result " (into {} v))))

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

(defn total
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
    (/ (total xs) (count xs))))

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
  [{:keys [stat data] :as result}]
  (if stat
    stat
    (->stat data)))

(defmethod ->stat List
  [obj]
  (total obj))

(defn result?
  "check to see if data if of type Result
   
   (result? {:data 1})
   => false"
  {:added "0.1"}
  [x]
  (instance? Result x))

(defn result
  "creates a result object from various types
   
   (result true)
   => (contains {:data true, :stat 1})
 
   (result 1)
   => (contains {:data 1, :stat 1})
   
   (result {:data 1})
   => (contains {:data 1, :stat 1})
 
   (result [1 2 3 4 5] average)
   => (contains {:data [1 2 3 4 5], :stat 3})
 
   (result [(result [1 2 3]) (result [4 5 6])] average)
   => (contains {:stat 21/2})"
  {:added "0.1"}
  ([x] (result x ->stat))
  ([x stat-fn]
   (let [res (cond (result? x)
                   x

                   (map? x)
                   (map->Result x)

                   :else 
                   (result {:data x}))
         data (:data res)]
     (assoc res :stat (stat-fn data)))))
