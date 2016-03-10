(ns layered-example.data.mysql-cargo-repository-test
  (:require [layered-example.data.mysql-cargo-repository :refer [new-cargo-repository]]
            [layered-example.domain.cargo.cargo :refer [map->Cargo]]
            [layered-example.domain.cargo.cargo-repository :refer [-find -update! -add!]]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as db]))

(def db-spec {:subprotocol "mysql"
              :subname "//127.0.0.1:3306/layered_example"
              :user "layered-example"
              :password "layered-example"})

(defn clean-tables! []
  (db/execute! db-spec ["delete from cargoes"]))

(defn insert-cargo! [row]
  (let [row (merge {:version 0
                    :size 0
                    :voyage_id nil} 
                   row)]
    (-> (db/insert! db-spec :cargoes row)
        first
        :generated_key)))

(use-fixtures :each (fn [f]
                      (clean-tables!)
                      (f)))

(def repo (new-cargo-repository db-spec))

(deftest find-cargo
  (let [cargo-id (insert-cargo! {:size 20
                                 :voyage_id 12
                                 :version 13}) ]
    (is cargo-id "cargo-id niet gezet")
    (let [{:keys [version cargo]} (-find repo cargo-id)
          expected-cargo (map->Cargo {:cargo-id cargo-id
                                      :size 20
                                      :voyage-id 12})]
      (is (= expected-cargo cargo) "cargo niet gevonden")
      (is (= 13 version)))))
