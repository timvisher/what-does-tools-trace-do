(ns what-does-tools-trace-do.core
  (:use clojure.tools.trace))

;;; Tutorial GitHub: https://github.com/timvisher/what-does-tools-trace-do
;;; GitHub: https://github.com/clojure/tools.trace
;;; API: http://clojure.github.io/tools.trace/
;;; ClojureDocs: http://clojuredocs.org/clojure_contrib/clojure.contrib.trace
;;; `=>`: return value of the function
;;; `1>`: standard out

(comment
  ;; What does tools.trace provide?

  ;; trace can "trace a value". What the heck does that mean?

  (trace 1)
  ;; => 1
  ;; 1> TRACE: 1

  ;; Gee. Real useful!

  (defn my-+ [a b]
    (+ a b))

  (trace (my-+ 1 2))
  ;; => 3
  ;; 1> TRACE: 3

  ;; Bang up again.

  ;; trace can also take a 'tag', presumably so you can easily search
  ;; for the trace?

  (trace "charnock" 1)
  ;; => 1
  ;; 1> TRACE charnock: 1

  ;; But wait! It finally clicks! trace is useful when you have a form
  ;; that is generating a value that you would like to see transform
  ;; over time. Of course, in clojure, values themselves never change,
  ;; but in a reduction, per se, the intermediate value will be built
  ;; up slowly over time.

  ;; This also explains why it's useful to have a label here but not
  ;; necessarily in the other trace forms. Only here would you
  ;; necessarily be tracing something that doesn't otherwise have a
  ;; name.

  (reduce (fn [a b] (trace :reduction (+ a b))) (range 1 10))
  ;; => 45
  ;; 1> TRACE :reduction: 3
  ;;    TRACE :reduction: 6
  ;;    TRACE :reduction: 10
  ;;    TRACE :reduction: 15
  ;;    TRACE :reduction: 21
  ;;    TRACE :reduction: 28
  ;;    TRACE :reduction: 36
  ;;    TRACE :reduction: 45

  ;; Nice!

  ;; trace-forms "Trace all the forms in the given body. Returns any
  ;; underlying uncaught exceptions that may make the forms fail."
  (trace-forms
   (let [a (+ 1 1)
         b (* 2 2)
         c (* a b (/ 4 0))]
     c))
  ;; => ArithmeticException Divide by zero
  ;;      Form failed: (/ 4 0)
  ;;      Form failed: (* a b (/ 4 0))
  ;;      Form failed: (let* [a (+ 1 1) b (* 2 2) c (* a b (/ 4 0))] c)
  ;;      Form failed: (let [a (+ 1 1) b (* 2 2) c (* a b (/ 4 0))] c)
  ;;      clojure.lang.Numbers.divide (Numbers.java:156)

  ;; The main usefulness here over just reading your stack trace is
  ;; the visibility of forms in the stack

  ;; Despite the obvious usefulness here, the docstring's slightly
  ;; misleading
  (trace-forms
   (let [a (+ 1 1)
         b (* 2 (/ 2 0))
         c (* a b (/ 4 0))]
     c))
  ;; => ArithmeticException Divide by zero
  ;;      Form failed: (/ 2 0)
  ;;      Form failed: (* 2 (/ 2 0))
  ;;      Form failed: (let* [a (+ 1 1) b (* 2 (/ 2 0)) c (* a b (/ 4 0))] c)
  ;;      Form failed: (let [a (+ 1 1) b (* 2 (/ 2 0)) c (* a b (/ 4 0))] c)
  ;;      clojure.lang.Numbers.divide (Numbers.java:156)

  ;; Note that despite there being multiple divide by zero
  ;; possibilities in the code, only (/ 2 0) is returned. This makes
  ;; sense, but hey, Clojure can be magical sometimes so I initially
  ;; took the docstring as read.

  ;; when there is no error, it returns nothing, which may have
  ;; confused you as it did me.
  (trace-forms
   (let [a (+ 1 1)
         b (* 2 2)
         c (* a b (/ 4 3))]
     c))
  ;; => 32/3

  ;; deftrace can be used in place of defn to define a function that will always be traced
  (deftrace charnock [x v] (+ x v))

  (charnock 1 2)
  ;; => 3
  ;; 1> TRACE t2331: (charnock 1 2)
  ;;    TRACE t2331: => 3

  (reduce charnock (range 1 10))
  ;; => 45
  ;; 1> TRACE t2340: (charnock 1 2)
  ;;    TRACE t2340: => 3
  ;;    TRACE t2341: (charnock 3 3)
  ;;    TRACE t2341: => 6
  ;;    TRACE t2342: (charnock 6 4)
  ;;    TRACE t2342: => 10
  ;;    TRACE t2343: (charnock 10 5)
  ;;    TRACE t2343: => 15
  ;;    TRACE t2344: (charnock 15 6)
  ;;    TRACE t2344: => 21
  ;;    TRACE t2345: (charnock 21 7)
  ;;    TRACE t2345: => 28
  ;;    TRACE t2346: (charnock 28 8)
  ;;    TRACE t2346: => 36
  ;;    TRACE t2347: (charnock 36 9)
  ;;    TRACE t2347: => 45

  ;; deftrace can take a docstring as well
  (deftrace docstring-charnock
    "docstring"
    []
    "Heyo!")

  ;; nested calls to deftrace functions will be printed in a tree-like
  ;; structure. I'm honestly not sure exactly what this means. I had
  ;; trouble reproducing the behavior on my own but I did dredge this
  ;; example up from clouredocs.org:
  ;; http://clojuredocs.org/clojure_contrib/clojure.contrib.trace/deftrace
  (deftrace fib [n]
    (if (or (= n 0) (= n 1))
      1
      (+ (fib (- n 1)) (fib (- n 2)))))

  (fib 4)
  ;; => 5
  ;; 1> TRACE t2742: (fib 4)
  ;;    TRACE t2743: | (fib 3)
  ;;    TRACE t2744: | | (fib 2)
  ;;    TRACE t2745: | | | (fib 1)
  ;;    TRACE t2745: | | | => 1
  ;;    TRACE t2746: | | | (fib 0)
  ;;    TRACE t2746: | | | => 1
  ;;    TRACE t2744: | | => 2
  ;;    TRACE t2747: | | (fib 1)
  ;;    TRACE t2747: | | => 1
  ;;    TRACE t2743: | => 3
  ;;    TRACE t2748: | (fib 2)
  ;;    TRACE t2749: | | (fib 1)
  ;;    TRACE t2749: | | => 1
  ;;    TRACE t2750: | | (fib 0)
  ;;    TRACE t2750: | | => 1
  ;;    TRACE t2748: | => 2
  ;;    TRACE t2742: => 5

  ;; I assume that is because this implementation of Fibonacci
  ;; consumes stack by not using loop/recur or it's ilk

  (docstring-charnock)
  ;; => "Heyo!"
  ;; 1> TRACE t4351: (docstring-charnock)
  ;;    TRACE t4351: => "Heyo!"

  ;; according to the authors, this was done to make it easy to use find/replace.

  ;; trace-vars can be used to trace a function you've already defined
  (defn a-pre-existing-function [a b]
    (/ (+ a b) 2))

  (a-pre-existing-function 1 2)
  ;; => 3/2

  (trace-vars a-pre-existing-function)

  (a-pre-existing-function 1 2)
  ;; => 3/2
  ;; 1> TRACE t2594: (what-does-tools-trace-do.core/a-pre-existing-function 1 2)
  ;;    TRACE t2594: => 3/2

  (doall (map a-pre-existing-function (range 1 10) (range 11 20)))
  ;; => (6 7 8 9 10 11 12 13 14)
  ;; 1> TRACE t5406: (what-does-tools-trace-do.core/a-pre-existing-function 1 11)
  ;;    TRACE t5406: => 6
  ;;    TRACE t5407: (what-does-tools-trace-do.core/a-pre-existing-function 2 12)
  ;;    TRACE t5407: => 7
  ;;    TRACE t5408: (what-does-tools-trace-do.core/a-pre-existing-function 3 13)
  ;;    TRACE t5408: => 8
  ;;    TRACE t5409: (what-does-tools-trace-do.core/a-pre-existing-function 4 14)
  ;;    TRACE t5409: => 9
  ;;    TRACE t5410: (what-does-tools-trace-do.core/a-pre-existing-function 5 15)
  ;;    TRACE t5410: => 10
  ;;    TRACE t5411: (what-does-tools-trace-do.core/a-pre-existing-function 6 16)
  ;;    TRACE t5411: => 11
  ;;    TRACE t5412: (what-does-tools-trace-do.core/a-pre-existing-function 7 17)
  ;;    TRACE t5412: => 12
  ;;    TRACE t5413: (what-does-tools-trace-do.core/a-pre-existing-function 8 18)
  ;;    TRACE t5413: => 13
  ;;    TRACE t5414: (what-does-tools-trace-do.core/a-pre-existing-function 9 19)
  ;;    TRACE t5414: => 14

  ;; Note the `doall`. Without that the REPL output is interleaved
  ;; with the return value of the function because of laziness. Thank
  ;; you, https://github.com/amalloy

  ;; and trace-ns can be used to trace everything in a namespace

  (defn another-pre-existing-function [a b]
    (+ a b))

  (reduce another-pre-existing-function (map a-pre-existing-function (range 1 10) (range 11 20)))
  ;; => 90
  ;; 1> TRACE t5548: (what-does-tools-trace-do.core/a-pre-existing-function 1 11)
  ;;    TRACE t5548: => 6
  ;;    TRACE t5549: (what-does-tools-trace-do.core/a-pre-existing-function 2 12)
  ;;    TRACE t5549: => 7
  ;;    TRACE t5550: (what-does-tools-trace-do.core/a-pre-existing-function 3 13)
  ;;    TRACE t5550: => 8
  ;;    TRACE t5551: (what-does-tools-trace-do.core/a-pre-existing-function 4 14)
  ;;    TRACE t5551: => 9
  ;;    TRACE t5552: (what-does-tools-trace-do.core/a-pre-existing-function 5 15)
  ;;    TRACE t5552: => 10
  ;;    TRACE t5553: (what-does-tools-trace-do.core/a-pre-existing-function 6 16)
  ;;    TRACE t5553: => 11
  ;;    TRACE t5554: (what-does-tools-trace-do.core/a-pre-existing-function 7 17)
  ;;    TRACE t5554: => 12
  ;;    TRACE t5555: (what-does-tools-trace-do.core/a-pre-existing-function 8 18)
  ;;    TRACE t5555: => 13
  ;;    TRACE t5556: (what-does-tools-trace-do.core/a-pre-existing-function 9 19)
  ;;    TRACE t5556: => 14

  (untrace-vars a-pre-existing-function)

  (trace-ns *ns*)

  (reduce another-pre-existing-function (map a-pre-existing-function (range 1 10) (range 11 20)))
  ;; => 90
  ;; 1> TRACE t2053: (what-does-tools-trace-do.core/a-pre-existing-function 1 11)
  ;;    TRACE t2053: => 6
  ;;    TRACE t2054: (what-does-tools-trace-do.core/a-pre-existing-function 2 12)
  ;;    TRACE t2054: => 7
  ;;    TRACE t2055: (what-does-tools-trace-do.core/another-pre-existing-function 6 7)
  ;;    TRACE t2055: => 13
  ;;    TRACE t2056: (what-does-tools-trace-do.core/a-pre-existing-function 3 13)
  ;;    TRACE t2056: => 8
  ;;    TRACE t2057: (what-does-tools-trace-do.core/another-pre-existing-function 13 8)
  ;;    TRACE t2057: => 21
  ;;    TRACE t2058: (what-does-tools-trace-do.core/a-pre-existing-function 4 14)
  ;;    TRACE t2058: => 9
  ;;    TRACE t2059: (what-does-tools-trace-do.core/another-pre-existing-function 21 9)
  ;;    TRACE t2059: => 30
  ;;    TRACE t2060: (what-does-tools-trace-do.core/a-pre-existing-function 5 15)
  ;;    TRACE t2060: => 10
  ;;    TRACE t2061: (what-does-tools-trace-do.core/another-pre-existing-function 30 10)
  ;;    TRACE t2061: => 40
  ;;    TRACE t2062: (what-does-tools-trace-do.core/a-pre-existing-function 6 16)
  ;;    TRACE t2062: => 11
  ;;    TRACE t2063: (what-does-tools-trace-do.core/another-pre-existing-function 40 11)
  ;;    TRACE t2063: => 51
  ;;    TRACE t2064: (what-does-tools-trace-do.core/a-pre-existing-function 7 17)
  ;;    TRACE t2064: => 12
  ;;    TRACE t2065: (what-does-tools-trace-do.core/another-pre-existing-function 51 12)
  ;;    TRACE t2065: => 63
  ;;    TRACE t2066: (what-does-tools-trace-do.core/a-pre-existing-function 8 18)
  ;;    TRACE t2066: => 13
  ;;    TRACE t2067: (what-does-tools-trace-do.core/another-pre-existing-function 63 13)
  ;;    TRACE t2067: => 76
  ;;    TRACE t2068: (what-does-tools-trace-do.core/a-pre-existing-function 9 19)
  ;;    TRACE t2068: => 14
  ;;    TRACE t2069: (what-does-tools-trace-do.core/another-pre-existing-function 76 14)
  ;;    TRACE t2069: => 90

  ;; trace-ns and trace-vars both come with un* alternatives
  (untrace-vars a-pre-existing-function)

  (a-pre-existing-function 1 2)
  ;; => 3/2

  (reduce another-pre-existing-function (map a-pre-existing-function (range 1 10) (range 11 20)))
  ;; => 90
  ;; 1> TRACE t2849: (what-does-tools-trace-do.core/another-pre-existing-function 6 7)
  ;;    TRACE t2849: => 13
  ;;    TRACE t2850: (what-does-tools-trace-do.core/another-pre-existing-function 13 8)
  ;;    TRACE t2850: => 21
  ;;    TRACE t2851: (what-does-tools-trace-do.core/another-pre-existing-function 21 9)
  ;;    TRACE t2851: => 30
  ;;    TRACE t2852: (what-does-tools-trace-do.core/another-pre-existing-function 30 10)
  ;;    TRACE t2852: => 40
  ;;    TRACE t2853: (what-does-tools-trace-do.core/another-pre-existing-function 40 11)
  ;;    TRACE t2853: => 51
  ;;    TRACE t2854: (what-does-tools-trace-do.core/another-pre-existing-function 51 12)
  ;;    TRACE t2854: => 63
  ;;    TRACE t2855: (what-does-tools-trace-do.core/another-pre-existing-function 63 13)
  ;;    TRACE t2855: => 76
  ;;    TRACE t2856: (what-does-tools-trace-do.core/another-pre-existing-function 76 14)
  ;;    TRACE t2856: => 90

  (untrace-ns *ns*)

  (reduce another-pre-existing-function (map a-pre-existing-function (range 1 10) (range 11 20)))
  ;; => 90

  ;; There we go! Hopefully this little tutorial makes it easier for
  ;; you to pick up tools.trace. I'm happy that I did. It should make
  ;; REPL debugging much less painful in the future.
  )
