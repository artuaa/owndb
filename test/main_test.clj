(ns main-test
  (:require [clojure.test :as t]
            [matcho.core :as matcho]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]))

(defn run-script [ss]
  (str/split-lines (:out (sh "./a.out" :in (str (str/join "\n" ss) "\n.exit ")))))

(defmacro match [script expected]
  `(let [result# (run-script ~script)]
     (matcho/match result# ~expected)
     result#))

(t/deftest main
  (t/testing "inserts and retrieves a row'"
    (match
     ["insert 1 user1 person1@example.com"
      "select"
      ".exit"]

      ["db > Executed."
       "db > (1, user1, person1@example.com)"
       "Executed."
       "db > "]))

  (t/testing "prints error message when table is full"
    (def res (->> (range 1 1402)
                  (map (fn [i] (format "insert %1$s user%1$s person%1$s@example.com" i)))
                  (run-script)))

    (t/is (= "db > Error: Table full." (nth res (- (count res) 2)))))

  (t/testing
   "allows inserting strings that are the maximum length"
    (def long_username (apply str (repeat 32 "a")))
    (def long_email (apply str (repeat 255 "a")))
    (match
     [(format "insert 1 %s %s" long_username long_email)
      "select"
      ".exit"]

      ["db > Executed."
       (format "db > (1, %s, %s)" long_username long_email),
       "Executed."
       "db > "]))

  (t/testing
   "prints error message if strings are too long"
    (def long_username (apply str (repeat 33 "a")))
    (def long_email (apply str (repeat 256 "a")))
    (match
     [(format "insert 1 %s %s" long_username long_email)
      "select"
      ".exit"]

      ["db > String is too long."
       "db > Executed."
       "db > "]))

  (t/testing
   "prints an error message if id is negative"
    (match
     ["insert -1 hey hey"
      "select"
      ".exit"]
      ["db > ID must be positive."
       "db > Executed."
       "db > "])))
