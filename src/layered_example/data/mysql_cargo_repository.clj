(ns layered-example.data.mysql-cargo-repository
  (:refer-clojure :exclude [find])
  (:require [layered-example.domain.cargo.cargo-repository :refer [CargoRepository -find -add! -update!]]
            [layered-example.domain.cargo.cargo :refer [map->Cargo]]
            [clojure.set :refer [rename-keys]]
            [clojure.java.jdbc :as db]))

(defn row->cargo [row]
  (-> row
      (select-keys [:id :size :voyage_id])
      (rename-keys {:id :cargo-id :voyage_id :voyage-id})
      map->Cargo))
  

(defn find [mysql-config cargo-id]
  (let [row (db/query mysql-config ["select * from cargoes where id=?" cargo-id])]
    (-> row
        first
        row->cargo)))

(defn new-cargo-repository [mysql-config]
  (reify CargoRepository
    (-find [this cargo-id] (find mysql-config cargo-id))
    (-add! [this cargo] nil)
    (-update! [this concurrency-version cargo-id] nil)))
