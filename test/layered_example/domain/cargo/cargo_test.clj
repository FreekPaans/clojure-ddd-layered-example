(ns layered-example.domain.cargo.cargo-test
  (:require [layered-example.domain.cargo.cargo :as cargo]
            [clojure.test :refer :all])
  (:import [java.lang AssertionError]))


(deftest a-cargo
  (let [the-cargo (cargo/make-cargo :cargo-id 1 :size 20)]
    (is (= 1 (:cargo-id the-cargo)))
    (is (= 20 (:size the-cargo)))))

(defn make-cargo-with-map [m]
  (apply cargo/make-cargo (flatten (seq m))))

(deftest ctor-test
  (let [valid-data {:cargo-id 1 :size 20}]
    (is (make-cargo-with-map valid-data) "the valid command is not valid")
    (is (thrown? AssertionError (make-cargo-with-map (dissoc valid-data :cargo-id))) 
        "cargo-id is required")
    (is (thrown? AssertionError (make-cargo-with-map (dissoc valid-data :size)))
        "size is required")
    (is (thrown? AssertionError (make-cargo-with-map (assoc valid-data :size "hello")))
        "size should be integer")))

