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

(deftest ctor-tests
  (let [valid-data {:cargo-id 1 :size 20}]
    (is (make-cargo-with-map valid-data) "the valid command is not valid")
    (is (thrown? AssertionError (make-cargo-with-map (dissoc valid-data :cargo-id))) 
        "cargo-id is required")
    (is (thrown? AssertionError (make-cargo-with-map (dissoc valid-data :size)))
        "size is required")
    (is (thrown? AssertionError (make-cargo-with-map (assoc valid-data :size "hello")))
        "size should be integer")))

(deftest book-onto-cargo
  (let [unbooked-cargo (cargo/make-cargo :cargo-id 1 :size 20)
        booked-cargo (cargo/book-onto-voyage unbooked-cargo :voyage-id 12)]
    (is (nil? (:voyage-id unbooked-cargo)) "voyage-id should be nil on new cargo")
    (is (= 12 (:voyage-id booked-cargo)) "voyage-id not set onto the cargo")
    (is (thrown? AssertionError (cargo/book-onto-voyage unbooked-cargo)) "voyage-id is required")
    (is (thrown? AssertionError (cargo/book-onto-voyage unbooked-cargo :voyage-id "bon")) 
        "voyage-id should be an integer")
    (is (thrown? AssertionError (cargo/book-onto-voyage booked-cargo :voyage-id 11))
        "can't book a cargo on a voyage if it's already booked")))




    
