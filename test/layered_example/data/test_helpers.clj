(ns layered-example.data.test-helpers
  (:require [clojure.java.jdbc :as db]))

(def db-spec nil)

(defn default-db-spec []
  {:subprotocol "mysql"
   :subname "//127.0.0.1:3306/layered_example"
   :user "layered-example"
   :password "layered-example"})

(defn set-db-spec! [spec]
  (alter-var-root (var db-spec) (constantly spec)))

(defn clean-tables! []
  (db/execute! db-spec ["delete from cargoes"]))
