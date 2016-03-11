(ns layered-example.application-service-test
  (:require [layered-example.data.test-helpers :as test-helpers]
            [clojure.test :refer :all]
            [layered-example.application-service :as app-svc]
            [layered-example.domain.cargo.cargo-repository :as cargo-repository]
            [layered-example.data.mysql-cargo-repository :as mysql-cargo-repository]))

(def db-spec (test-helpers/default-db-spec))

(def the-cargo-repo (mysql-cargo-repository/new-cargo-repository db-spec))

(use-fixtures :each (fn [f]
                      (test-helpers/set-db-spec! db-spec)
                      (test-helpers/clean-tables!)
                      (cargo-repository/set-implementation! the-cargo-repo)
                      (f)))


(deftest create-a-cargo
  (let [{:keys [cargo-id]} (app-svc/create-new-cargo! :size 44)]
    (is cargo-id "cargo should be returned")
    (let [cargo (cargo-repository/find cargo-id)]
      (is cargo "cargo should be found in repository"))))

(deftest book-cargo-onto-voyage
  (let [cargo (app-svc/create-new-cargo! :size 44)
        cargo-id (:cargo-id cargo)]
    (do (app-svc/book-onto-voyage! cargo-id :voyage-id 13)
        (let [{updated-cargo :cargo} (cargo-repository/find cargo-id)]
          (is (= 13 (:voyage-id updated-cargo)) "voyage-id should be set")))))
