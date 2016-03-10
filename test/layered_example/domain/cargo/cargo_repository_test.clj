(ns layered-example.domain.cargo.cargo-repository-test
  (:require [layered-example.domain.cargo.cargo-repository :as repo]
            [layered-example.domain.cargo.cargo :as cargo]
            [clojure.test :refer :all]))

(defn find-new-id [cargos-map]
  (if (empty? cargos-map)
    1
    (+ 1 (apply max (keys cargos-map)))))

(defn add [cargos-map a-cargo]
  (let [new-id (find-new-id cargos-map)
        created-cargo (assoc a-cargo :cargo-id new-id)]
    [created-cargo (assoc cargos-map new-id created-cargo)]))

(defrecord CargoRepositoryMock [cargos-map]
  repo/CargoRepository
  (repo/-find [_ cargo-id] 
    (when-let [cargo (get @cargos-map cargo-id)]
      {:version 1 :cargo cargo}))
  (repo/-add! [_ a-cargo] 
    (let [[created-cargo new-cargo-map] (add @cargos-map a-cargo)]
      (reset! cargos-map new-cargo-map)
      created-cargo))
  (repo/-update! [_ _ a-cargo] 
    (swap! cargos-map 
           (fn [val] (assoc val (:cargo-id a-cargo) a-cargo)))
    nil))

(def cargo-1 (cargo/map->Cargo {:cargo-id 1 :size 10}))
(def cargo-2 (cargo/map->Cargo {:cargo-id 3 :size 15}))

(use-fixtures :each (fn [f]
  (let [cargos [cargo-1 cargo-2]
        cargos-map (into {} (for [cargo cargos] [(:cargo-id cargo) cargo]))
        cargo-impl (->CargoRepositoryMock (atom cargos-map))]
    (repo/set-implementation! cargo-impl)
    (f))))

(deftest convenience-functions
  (is (= {:version 1 :cargo cargo-1} (repo/find 1)) "couldn't find cargo-1 in repository")
  (let [new-cargo (cargo/create-new-cargo :size 10)
        created-cargo (repo/add! new-cargo)]
    (is (:cargo-id created-cargo) "cargo-id not set")
    (is (repo/find (:cargo-id created-cargo)) "couldn't find newly created cargo"))
  (let [{:keys [version cargo]} (repo/find 1)]
    (repo/update! version 
                  (cargo/book-onto-voyage cargo :voyage-id 12))
    (let [updated-cargo (:cargo (repo/find 1))]
      (is (= 12 (:voyage-id updated-cargo)) "cargo voyage not set"))))
