(ns app.tutorial)

(def tutorial*
  "Collection of map steps."
  [{:title "Introduction to Chronos"
    :content "Chronos is a zero-dependency Clojure(Script) API to <a href=\"https://docs.oracle.com/javase/tutorial/datetime/iso/overview.html\">java.time</a> 
    on the JVM and <a href=\"https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Temporal\">Temporal</a> on JS runtimes (such as this browser)
    
To find out the rationale for this library and how to install it, visit the <a href=\"https://github.com/henryw374/chronos\">README</a>    

This is an interactive tutorial for Chronos. Jump to a section using the left-side nav or see the <a href=\"#/all\">Full listing</a> to search in page for anything in particular.
 <span id=\"location-of-editor\">Here on the right</span>
you have a **REPL**.
Functions from the main chronos ns are included under the alias 'c'

For example, click on this expression: `(c/date-parse \"2020-02-02\")` to auto insert, or type into the REPL directly. 

You can type `(help)` for more commands.
   
"}
   {:title "Entities & Naming"
    :content "
<img src=\"https://tc39.es/proposal-temporal/docs/object-model.svg\"/>

The above graph shows the entities in 'Temporal'. If you know 'java.time' and you squint a bit, it will look familiar to
you. The Chronos API aims to find common ground between Temporal and java.time - sufficient to satisfy the majority of
use cases.

Regarding names, the java.time 'Local' prefix and the Temporal 'Plain' prefix have been removed, 
so e.g. PlainDate/LocalDate are just 'date'

as in `(c/date? (c/date-parse \"2020-02-02\"))`.

Multi-part entities, such 'datetime' or 'yearmonth' have no separator in the name and all names are lower case.

ZonedDateTime is called 'zdt' to keep it short. 
    
js/Date and java.util.Date are called 'legacydate'

Otherwise, the naming of entities in 'Chronos' should be self-explanatory.
    "
    ;:test (constantly true)
    }
   {:title "Construction and Access"
    :content "
The naming of construction and access functions is based on mnemonics: The first word in the function is the entity name of the subject of the operation and
the second word (after the hyphen) is the operation, so IOW <i>c/entity-operation</i>

`(c/date-parse \"2020-02-02\")` ;iso strings only

`(c/zdt-parse \"2024-02-22T00:00:00Z[Europe/London]\")` ;iso strings only

For the remainder of this section, it might be useful to refer to the entity graph in the previous page.

As well as parsing, one can build from parts
`(c/date-from {:year 2020 :month 2 :day-of-month 2})`

 the '-from' functions accept a map of components which must be sufficient to build the entity

`(c/datetime-from {:date (c/date-parse \"2020-02-02\") :time (c/time-deref clock)})`

or equivalently

`(c/datetime-from {:year 2020 :month 2 :day-of-month 2 :time (c/time-deref clock)})`

with '-from', you can use smaller or larger components (size here is referring to number of fields). 

Larger entities take precedence. Below, the ':year' is ignored, because the ':date' took precedence (being larger) 

`(c/datetime-from {:year 2021 :date (c/date-parse \"2020-02-02\") :time (c/time-deref clock)})`

One can 'add' a field to an object to create a different type. 

`(c/yearmonth+day-of-month (c/yearmonth-parse \"2020-02\") 1)` ; => a date

`(c/yearmonth+day-at-end-of-month (c/yearmonth-parse \"2020-02\"))` ; => a date

`(c/datetime+timezone (c/datetime-parse \"2020-02-02T02:02\") \"Pacific/Honolulu\")` 

To get a part of an entity, the function name will start with the type of the entity, followed by '->' then 
the target type. For example:

`(c/date->yearmonth (c/date-parse \"2020-02-02\"))`

`(c/date->month (c/date-parse \"2020-02-02\"))`

`(c/zdt->nanosecond (c/zdt-parse \"2024-02-22T00:00:00.1Z[Europe/London]\"))`

`(c/instant->epochmilli (c/instant-deref clock))`

`(c/epochmilli->instant 123)`

`(c/legacydate->instant (js/Date.))`
    "}
   ;; Clocks
   {:title "Clocks and 'Now'"
    :content
    "> Best practice for applications is to pass a Clock into any method that requires the current instant. 
- from the Javadoc of java.time.InstantSource
        
In both java.time and Temporal it is possible to use the ambient Clock by calling a zero-arity 'now' function, 
for example `(js/Temporal.Now.instant)`, but this impedes testing so has no equivalent in Chronos.

Naming-wise, Chronos makes an analogy with clojure's atoms, so functions to get the current value 
from a clock are named <i>subject</i>-deref,
for example `(c/date-deref clock)` or `(c/timezone-deref clock)`


Create a Clock that is will return the current browser's time in the current timezone with 
  `(def clock (c/clock-system-default-zone))` or ...
  
A ticking clock in specified place
`(def clock (c/clock-with-timezone \"Pacific/Honolulu\"))`

 A clock fixed in time and place
`(def clock (c/clock-fixed (c/instant-parse \"2020-02-02T00:00:00Z\") \"Europe/Paris\"))`

Offset existing clock by specified millis
`(def clock (c/clock-offset-millis clock -5))`

Create a mutable, non-ticking clock - simply change the value in the atom as required
`(def zdt-atom (atom (c/zdt-parse \"2024-02-22T00:00:00Z[Europe/London]\")))`
`(def clock (c/clock-zdt-atom zdt-atom))`

If you have other requirements for a clock, it is easy to create your own implementation like so

`(c/clock  (fn return-an-instant [])  (fn return-a-timezone []))`

  "
    ;:test (constantly true)
    }
   {:title "Properties"
    :content "
Property vars such as `c/hours-property` combine the concepts (from the underlying platform APIs) of 'units' and 'fields',
so for example

`(c/until x y c/days-property)` ; how much time in unit days?
`(c/with (c/date-deref clock) 11 c/days-property)` ; set the day of month field to 11

Combining the concept of unit and field is a simplification. 

In some cases it may be an over-simplification, for example `c/days-property` corresponds to the 'day of month' field, 
so if 'day of year' was required a new property would have to be created in user space. 
    "}
   {:title "Manipulation" 
    :content "
Manipulation: aka construction a new temporal from one of the same type

move date forward 3 days

`(c/>> (c/date-deref clock) 3 c/days-property)`

 move date to next-or-same tuesday
`(c/date-next-or-same-weekday (c/date-deref clock) c/weekday-tuesday)`

move date to prev-or-same sunday
`(c/date-prev-or-same-weekday (c/date-deref clock) c/weekday-sunday)` 

;; set a particular field
`(c/with (c/yearmonth-deref clock) 3030 c/years-property)`

; set fields smaller than days (ie hours, mins etc) to zero
`(c/truncate (c/instant-deref clock) c/hours-property)`

   "}
   
   {:title "Weekdays and Months" 
    :content "As you may have noticed, weekdays and months are not reified entities in Chronos (same as Temporal)
    
Weekdays are represented by numbers 1-7, with Monday being 1.

Months are represented by numbers 1-12, with January being 1.

However, to avoid magical numbers, Chronos provides vars that provide suitable names for these

`(->> c/weekday-tuesday (get c/weekday->weekday-name))`

and if you need to know the number of a named day or month, a reverse lookup is available 

`(->> c/weekday-tuesday-name (get c/weekday-name->weekday))`
   
   "}
   {:title "Time zones" :content "

Timezones in 'chronos' are strings.

`(c/timezone-deref clock)`

`(c/zdt->timezone zdt)`

`(c/zdt-from {:datetime datetime :timezone timezone})`

   "}
   
   {:title "Guardrails" :content "
Consider the following:


` (-> (c/date-parse \"2020-01-31\") (c/>> 1 c/months-property) (c/<< 1 c/months-property))`

If you shift a date forward by an amount, then back by the same amount then one might think that the output would be equal to the
input. In some cases that would happen, but not in the case shown above.

Here's a similar example:

`(-> (c/date-parse \"2020-02-29\") (c/with 2021 c/years-property) (c/with 2020 c/years-property))`

We increment the year, then decrement it, but the output is not the same as the input.

Both java.time and Temporal work this way and in my experience it is a source of bugs. For this reason, shifting '>>/<<'
and 'with' do not work in Chronos if the property is years or months and the subject is not a year-month.

As a safer alternative, I suggest getting the year-month from a temporal first, doing whatever with/shift operations you
like then setting the remaining fields.

If you do not wish to have this guardrail, set `c/*block-non-commutative-operations*` to false
   
   "}
   {:title "Comparison" :content "

The following expression is false in JS runtimes by default:

`(= (c/date-parse \"2020-02-02\") (c/date-parse \"2020-02-02\"))`

The reason is that Chronos prioritises 'dead-code-elimination' - so in other words only the functions
used from this library should be incorporated in your ':advanced' compiled build.

If you want equality and hashing to work for all Temporal entities, run the following

`(c/enable-comparison-for-all-temporal-entities)`

Having done so, the previous expression will evaluate to true.

If your application only compares a subset of the Temporal entities, the protocol extension can just
be applied to those individually. See the source of 'c/enable-comparison-for-all-temporal-entities' for details. 

In addition to clojure's '=',  entities of the same type can be compared as follows.

`(c/>= a b)`

`(c/max a b c)`

`(c/until a b c/minutes-property)`



```
   
   "}
   {:title "Predicates" :content "
`(c/date? x)`   
   "}
   {:title "Temporal-amounts" :content "
   A temporal-amount is an entity representing a quantity of time, e.g. 3 hours and 5 seconds, or -6 months and 1 day.

'java.time' has 2 types to represent these: Period and Duration, whereas 'Temporal' has a single Duration entity. 

Since these differences exist, there is no common Chronos API for these and it is recommended to use platform APIs directly, if the Chronos properties & manipulation functions do not suffice.

   "}
   {:title "Formatting" :content "
formatting (and parsing) non-iso strings is not a feature in Chronos, because there is no suitable underlying
API in Temporal.

On the jvm, the in-built java.time.format.DateTimeFormatter is the most obvious choice.

In js runtimes, there is no in-built alternative for parsing. For printing, look at <a href=\"https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/DateTimeFormat\">Intl.DateTimeFormat</a>. 

Non-iso date parsing libraries might be an option but beware they
can involve hefty payloads if they contain localization data.

Alternatively, consider a regex to get the constituent parts of the string, then using those in Chronos's '-from' functions.

"}
  
  ])


(def tutorial 
  (->> tutorial* 
       (into [] (map-indexed (fn [idx item]
                               (assoc item :index idx))))))