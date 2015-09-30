(ns daifu.diagnosis.indicator.invoke-test
  (:use midje.sweet)
  (:require [daifu.diagnosis.indicator.invoke :refer :all]
            [daifu.diagnosis.indicator :as indicator]
            [rewrite-clj.zip :as zip]))

(def +global+
  {:indicators (-> (indicator/load-defaults)
                   (select-keys [:too-many-tokens :token-count])
                   (indicator/activate-all))})

^{:refer daifu.diagnosis.indicator.invoke/stat-function :added "0.1"}
(fact "provides the function for merging statistical data"
  
  ((stat-function {}) [1 2 3 4]) => 10

  ((stat-function {:accumulate {:default :count}}) [1 2 3 4])
  => 4

  ((stat-function {:accumulate {:stat :average
                                :default :count}}) [1 2 3 4])
  => 5/2)


^{:refer daifu.diagnosis.indicator.invoke/invoke :added "0.1"}
(fact "invokes an indicator with reference to a global datastructure"
  (invoke +global+
          (-> +global+ :indicators :token-count)
          (zip/of-string "(defn add [])")
          nil)
  => (contains {:data 4, :stat 4})

  (invoke +global+
          (-> +global+ :indicators :too-many-tokens)
          (zip/of-string "(defn add [])")
          nil)
  => (contains {:data false, :stat 0})

  (invoke +global+
          (-> +global+ :indicators :too-many-tokens)
          (zip/of-string "(defn add [])")
          {:limit 2})
  => (contains {:data true, :stat 1}))

^{:refer daifu.diagnosis.indicator.invoke/invoke-arglist :added "0.1"}
(fact "invokes dependents of a particular indicator"
  (invoke-arglist +global+
                  (-> +global+ :indicators :too-many-tokens)
                  [:token-count]
                  (zip/of-string "(defn add [])"))
  => '(4))

(def +more+
  {:indicators (-> (indicator/load-defaults)
                   (select-keys [:line-count
                                 :char-per-line
                                 :line-over-char-limit
                                 :line-over-per-file])
                   (indicator/activate-all))})

^{:refer daifu.diagnosis.indicator.invoke/invoke-more :added "0.1"}
(fact "invokes an indicator with reference to a global datastructure"
  (invoke +more+
          (-> +more+ :indicators :char-per-line)
          (zip/of-string "(defn add \n [x y] \n (+ x y))")
          nil)
  => (contains {:data '(9 5 8), :stat 22/3})

  (invoke +more+
          (-> +more+ :indicators :line-over-char-limit)
          (zip/of-string "(defn add \n [x y] \n (+ x y))")
          nil)
  => (contains {:data '(), :stat 0})

  (invoke +more+
          (-> +more+ :indicators :line-over-char-limit)
          (zip/of-string "(defn add \n [x y] \n (+ x y))")
          {:limit 6})
  => (contains {:data '(9 8), :stat 2}))
