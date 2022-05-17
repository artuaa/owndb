(ns main-test
  (:require [clojure.test :as t]
            [matcho.core :as matcho]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]))

(defn run-script [ss]
  (str/split-lines (:out (sh "./a.out" :in (str (str/join "\n" ss) "\n.exit ")))))

(defmacro match [script expected]
  `(let [result# (run-script ~script)]
     (matcho/match ~expected result#)
     result#))

(t/deftest main
  (t/testing "inserts and retrieves a row'"
    (match
     ["db > Executed."
      "db > (1 user1, person1@example.com)"
      "Executed."
      "db > "]

     ["insert 1 user1 person1@example.com"
      "select"
      ".exit"]))

  (t/testing "prints error message when table is full"
    (def res (->> (range 1 1402)
                  (map (fn [i] (format "insert %1$s user%1$s person%1$s@example.com" i)))
                  (run-script)))

    (t/is (= "db >Error: Table full." (nth res (- (count res) 2))))))
