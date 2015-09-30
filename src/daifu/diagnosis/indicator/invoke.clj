(ns daifu.diagnosis.indicator.invoke
  (:require [daifu.diagnosis.indicator.activate :as activate]
            [daifu.diagnosis.indicator.result :as result]
            [hara.function.args :as args]))

(declare invoke)

(defn invoke-inputs [global inputs args]
  (flatten (reduce (fn [out k]
                     (conj out (invoke global (-> global :indicators k) args)))
                   []
                   inputs)))

(defn invoke [global indicator args]
  (let [main   (-> indicator :main deref)
        margs  (apply max (args/arg-count main))
        inputs (invoke-inputs global (:inputs indicator) args)
        opts?  (if (-> (dec margs) (- (count inputs)) zero?)
                 []
                 [(merge (:default indicator) (-> global :current :pair second))])]
    (apply main (concat args inputs opts?))))


(comment

  (require '[daifu.diagnosis.indicator :as indi])

  (def global {:indicators (-> (indi/load-defaults)
                               (indi/activate-all))})

  (invoke global (-> global :indicators :token-count) [(zip/of-string "(defn add [])")])
  
  (invoke global (-> global :indicators :too-many-tokens) [(zip/of-string "(defn add [])")])
  
  
  (invoke (-> global :indicators :too-many-tokens) [(zip/of-string "(defn add [])")]
          (assoc-in global
                    [:current :pair] [:too-many-tokens {:limit 40}]))

 
  
  (require '[rewrite-clj.zip :as zip]
           '[clojure.string :as string])
  
  
  (def indi (activate/create-function
             {:id :char-count
              :type :function
              :injections '[(require '[clojure.string :as string])]
              :source '(fn [zloc]
                         (->> (zip/->string zloc)
                              (string/split-lines)
                              (map string/trim)
                              (map count)))}))
  (args/arg-count indi)
  
  (def secd (activate/create-function
             {:id    :char-over
              :type  :function
              :level :warn
              :defaults {:max 30}
              :inputs [:char-count]
              :source '(fn [zloc counts opts]
                         (filter #(< (:max opts) %) counts))}))

  (def third (activate/create-function
              {:id    :lines-over
               :type  :function
               :level :warn
               :inputs [:char-over]
               :source '(fn [zloc counts]
                          (count counts))}))

  {:indicators {:char-count indi
                :char-over secd
                :line-over third}}

  (invoke-indicator )

  [[:char-over {:id :default :max 40}]]

  (indi (zip/of-string "(defn add []\n\n(+ 1 2))"))
  )
