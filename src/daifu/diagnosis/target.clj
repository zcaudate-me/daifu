(ns daifu.diagnosis.target
  (:require [clojure.java.io :as io]
            [gita.core :as git]
            [daifu.diagnosis.target.project :as project]
            [daifu.diagnosis.target.files :as files]))

(defrecord Target [])

(defmethod print-method Target
  [v w]
  (.write w (str "@" (name (:type v)) " "
                 (into {} (dissoc v :type)))))

(defn target?
  [x]
  (instance? Target x))

(defn target
  [m]
  (map->Target m))

(def +default-target+
  (target {:id :default
           :type :project}))

(defn read-project
  [repo target]
  (project/read-project repo target))

(defn list-files
  [repo target]
  (files/list-files repo target))

(defn pick-files
  [repo target]
  (files/pick-files repo target))

(defn retrieve-file
  [repo target]
  (files/retrieve-file repo target))
