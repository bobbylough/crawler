# Basic Web Crawler

###Description:
A basic web crawler that will take a url and crawl until 50 unique links are followed

##Usage

Arguments:

1. **(Required)** root url (example: http://www.bobbylough.com)
1. *(Optional)* proxy host (example: usproxy.company.com)
1. *(Optional)* proxy port (example: 8080)


## Build Project:
```
>mvn clean package
```

## Execute Jar:
```
>java -jar target/crawler-0.0.1-SNAPSHOT-jar-with-dependencies.jar http://bobbylough.com usproxy.company.com 8080
>java -jar target/crawler-0.0.1-SNAPSHOT-jar-with-dependencies.jar http://bobbylough.com
```

## Sample Output Report:

```
200 - http://www.bobbylough.com
200 - http://www.bobbylough.com/2015/06/find-middle-of-linked-list-in-one-pass.html
200 - http://www.bobbylough.com/2014/09/palindrome-product-challenge.html
404 - http://www.bobbylough.com/2014/09/even-multiple-challenge.htm
200 - http://www.bobbylough.com/2014/09/largest-prime-factor-challenge.html
200 - http://jsfiddle.net/nctr2sqh/
200 - http://www.bobbylough.com/2015/05/another-easy-programming-stretch.html
200 - http://www.bobbylough.com/2015/05/programming-stretch-with-loops.html
200 - http://www.bobbylough.com/2014/09/display-natural-number-as-text.html
404 - http://4.bp.blogspot.com/-y_csf8snnfw/vxy3wnknjqi/aaaaaaaaawi/ll4ryoa0vka/s1600/image_medium.jpg
404 - http://3.bp.blogspot.com/-qpyvcry4vaq/vxih_6_q2qi/aaaaaaaaavm/vkriephycua/s1600/joltcola1.png
404 - http://2.bp.blogspot.com/-1ho4pcsve4y/vxh_yjryixi/aaaaaaaaauw/mo-fxwmzuqc/s1600/superthumb.png
404 - http://4.bp.blogspot.com/-zcdsr7wcg_o/vxcmbjxnu7i/aaaaaaaaaxq/vsbb4jk2yaq/s1600/unicornvmutant.png
200 - http://www.bobbylough.com/2015/06/give-your-tests-mutant-powers-with-pit.html
200 - https://www.linkedin.com/pulse/can-your-tests-survive-coming-mutant-apocalypse-bobby-lough
200 - https://projecteuler.net/problem=2
200 - http://www.togotutor.com/code-to-html/java-to-html.php
200 - https://projecteuler.net/profile/hibobbo.png
200 - https://projecteuler.net/problem=3
200 - http://doc.jsfiddle.net/use/embedding.html
INVALID - http://twitter.com/home?status=check out my fiddle:  https://jsfiddle.net/nctr2sqh/
```
