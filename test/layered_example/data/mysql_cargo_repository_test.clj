(ns layered-example.data.mysql-cargo-repository-test
  (:require [layered-example.data.mysql-cargo-repository :refer [new-cargo-repository]]
            [layered-example.domain.cargo.cargo :refer [map->Cargo create-new-cargo book-onto-voyage]]
            [layered-example.domain.cargo.cargo-repository :refer [-find -update! -add!]]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as db]
            [layered-example.data.test-helpers :as test-helpers]))

(def db-spec (test-helpers/default-db-spec))


(defn find-cargo-row [cargo-id]
  (-> (db/query db-spec ["select * from cargoes where id=?" cargo-id])
      first))

(defn insert-cargo! [row]
  (let [row (merge {:version 0
                    :size 0
                    :voyage_id nil} 
                   row)]
    (-> (db/insert! db-spec :cargoes row)
        first
        :generated_key)))

(defn update-cargo-row! [cargo-id data]
  (db/update! db-spec :cargoes data ["id=?" cargo-id]))

(use-fixtures :each (fn [f]
                      (test-helpers/set-db-spec! db-spec)
                      (test-helpers/clean-tables!)
                      (f)))

(def repo (new-cargo-repository db-spec))

(deftest find-cargo
  (testing "happy flow"
    (let [cargo-id (insert-cargo! {:size 20
                                   :voyage_id 12
                                   :version 13})]
      (is cargo-id "cargo-id niet gezet")
      (let [{:keys [version cargo]} (-find repo cargo-id)
            expected-cargo (map->Cargo {:cargo-id cargo-id
                                        :size 20
                                        :voyage-id 12})]
        (is (= expected-cargo cargo) "cargo not found")
        (is (= 13 version)))))
  (testing "cargo doesn't exist"
    (test-helpers/clean-tables!)
    (is (nil? (-find repo 1)) "cargo should not be found")))

(deftest add-cargo
  (testing "a new cargo"
    (let [cargo (create-new-cargo :size 44)
          {:keys [cargo-id] :as added-cargo} (-add! repo cargo)]
      (is cargo-id "cargo-id is not set")
      (is added-cargo "cargo should be returned")))
  (testing "an existing cargo"
    (let [cargo-id (insert-cargo! {})
          {:keys [cargo]} (-find repo cargo-id)]
      (is (thrown? AssertionError (-add! repo cargo)) "can't add an existing cargo"))))
      
(deftest update-cargo
  (testing "book on voyage"
    (let [cargo-id (insert-cargo! {})
          {:keys [version cargo]} (-find repo cargo-id)
          booked-cargo (book-onto-voyage cargo :voyage-id 12)]
      (-update! repo version booked-cargo)
      (is (= 12 (:voyage_id (find-cargo-row cargo-id))) "voyage_id should be updated in database")))
  (testing "a new cargo"
    (let [cargo (create-new-cargo :size 44)]
      (is (thrown? AssertionError (-update! repo 0 cargo)) "a new cargo cannot be updated")))
  (testing "update logic"
    (let [cargo-1-id (insert-cargo! {})
          cargo-2-id (insert-cargo! {})
          {:keys [version cargo]} (-find repo cargo-1-id)
          booked-1 (book-onto-voyage cargo :voyage-id 13)]
      (do
        (-update! repo version booked-1)
        (let [cargo-2-row (find-cargo-row cargo-2-id)]
          (is (nil? (:voyage_id cargo-2-row)) "only the cargo-1 row should be updated")))))
  (testing "concurrency check"
    (let [cargo-id (insert-cargo! {})
          {:keys [version cargo]} (-find repo cargo-id)
          booked-cargo (book-onto-voyage cargo :voyage-id 13)]
      (do
        (update-cargo-row! cargo-id {:version (inc version)})
        (try 
          (-update! repo version booked-cargo)
          (is nil "update should fail")
          (catch RuntimeException ex
            (is (= :optimistic-concurrency (:exception-type (ex-data ex))))))))))
          
