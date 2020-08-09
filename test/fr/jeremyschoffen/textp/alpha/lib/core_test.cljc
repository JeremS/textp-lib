(ns fr.jeremyschoffen.textp.alpha.lib.core-test
  (:require [clojure.test :refer [deftest testing is are]]
            [fr.jeremyschoffen.textp.alpha.lib.core :as lib :include-macros true]))


(lib/def-xml-tag div :div)

(deftest tag-constructor
  (are [x y] (= x y)
    (div)
    {:tag :div  :attrs {} :content []}

    (div {:tag :tag-args-clj, :content [:class "toto"]}
         {:tag :tag-args-txt, :content ["content"]})
    {:tag :div, :attrs {:class "toto"}, :content ["content"]}

    (div {:tag :tag-args-clj, :content [:class "toto"]})
    {:tag :div, :attrs {:class "toto"} :content []}

    (div {:tag :tag-args-txt, :content ["content"]})
    {:tag :div  :attrs {} :content ["content"]})

  (are [x] (thrown? #?(:clj Exception
                       :cljs js/Error) x)
    (div {:tag :tag-args-txt, :content ["content"]}
         {:tag :tag-args-txt, :content ["content"]}
         {:tag :tag-args-clj, :content [:class "toto"]})

    (div {:tag :tag-args-txt, :content ["content"]}
         {:tag :tag-args-clj, :content [:class "toto"]}
         {:tag :tag-args-clj, :content [:class "toto"]})


    (div {:tag :tag-args-txt, :content ["content"]}
         {:tag :tag-args-clj, :content [:class "toto"]})))


(lib/def-tag-fn add [x y]
  (+ x y))


(lib/def-tag-fn example
  ([x] x)
  ([x y] [x y]))


(deftest tag-fn-cstr
  (testing "Adder"
    (is (= (add {:tag :tag-args-clj :content [1 2]}) 3))
    (are [x] (is (thrown? #?(:clj Exception
                             :cljs js/Error)
                          x))
      (add 1)
      (add 1 2)

      #?(:clj (add {:tag :tag-args-clj :content [1]}))
      #?(:clj (add {:tag :tag-args-clj :content [1 2 3]}))))

  (testing "example"
    (are [x y] (= x y)
      (example {:tag :tag-args-clj :content [1]}) 1
      (example {:tag :tag-args-clj :content [1 2]}) [1 2])))
