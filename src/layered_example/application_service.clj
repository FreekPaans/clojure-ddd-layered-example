(ns layered-example.application-service
  (:require [layered-example.domain.cargo.cargo :as cargo]
            [layered-example.domain.cargo.cargo-repository :as cargo-repository]))

(defn create-new-cargo! [& args ]
  (let [new-cargo (apply cargo/create-new-cargo args)]
    (cargo-repository/add! new-cargo)))

(defn book-onto-voyage! [cargo-id & args]
  (let [{:keys [version cargo] :as result} (cargo-repository/find cargo-id)
        booked-cargo (apply cargo/book-onto-voyage (list* cargo args))]
    (cargo-repository/update! version booked-cargo)
    nil))
    


