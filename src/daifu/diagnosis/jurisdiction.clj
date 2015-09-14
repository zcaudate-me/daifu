(ns daifu.core.jurisdiction)

(defrecord Jurisdiction [])

(defmethod print-method Jurisdiction
  [v w]
  (.write w (str "@" (name (:type v)) " "
                 (into {} (dissoc v :type)))))

(defn jurisdiction [m]
  (map->Jurisdiction m))

(comment
  (jurisdiction {:type :hello}))
