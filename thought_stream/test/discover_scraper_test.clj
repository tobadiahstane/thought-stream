(ns thought-stream.discover-scraper-test
  (:require
    [clojure.test :refer :all])
  (:import
    [org.openqa.selenium
      By Keys WebDriver WebElement JavascriptExecutor]
    [org.openqa.selenium.chrome
      ChromeDriver ChromeOptions]
     [org.openqa.selenium.support.ui
      ExpectedConditions Select WebDriverWait]))

(defn test-driver []
  (let [options (ChromeOptions. )
        _ (.setHeadless options false)
        _ (.addArguments options ["'--disable-extensions'"])
        _ (.addArguments options ["'--profile-directory=Default'"])
        _ (.addArguments options ["--incognito"])
        _ (.addArguments options ["--disable-plugins-discovery"])
        _ (.addArguments options ["--start-maximized"])
        driver-prop "webdriver.chrome.driver"
        _ (System/setProperty driver-prop "resources/chromedriver.exe")
        driver (new ChromeDriver options)]
    driver))

(defn testing-driver-functionality [test-driver]
  (let [testable test-driver]
    (.get testable "https://portal.discover.com/customersvcs/universalLogin/ac_main")
    (let [login-form (.findElement testable (By/name "loginForm"))
          ]
      (Thread/sleep 8000)
      (.executeScript testable "document.getElementById('userid-content').click();" (to-array [login-form]))
      (Thread/sleep 1000)
      (.executeScript testable "document.getElementById('userid-content').value = 'oluwatobi1';" (to-array [login-form]))
      (Thread/sleep 1000)
      (.executeScript testable "document.getElementById('password-content').click();" (to-array [login-form]))
      (Thread/sleep 1000)
      (.executeScript testable "document.getElementById('password-content').value = 'T6BxgHC2CtlqvC1f';" (to-array [login-form]))
      (Thread/sleep 1000))))
      ;(.executeScript testable "document.getElementById('log-in-button').click();" (to-array [login-form]))
      ;(.until (WebDriverWait. testable (/ 5000 1000) 0) (ExpectedConditions/urlContains "homepage" )))))



(testing-driver-functionality (test-driver))

(comment
    """Host: portal.discover.com
    User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0
    Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
    Accept-Language: en-US,en;q=0.5
    Accept-Encoding: gzip, deflate, br
    Referer: https://www.discover.com/
    Content-Type: application/x-www-form-urlencoded
    Content-Length: 4584
    DNT: 1
    Connection: keep-alive
    Cookie: check=true;
           s_pers=%20s_ev31%3D%255B%255B%2527hdr_logn_cc_logn%2527%252C%25271546556687715%2527%255D%255D%7C1704323087714%3B%20s_dfa%3Ddiscoverglobalprod%252C%2520discovercardservicingprod%7C1546897644277%3B%20s_vnum%3D1549000800316%2526vn%253D6%7C1549000800316%3B%20s_fid%3D2E9ED6B7E599735B-155D9F15CFBF4C3F%7C1704662260146%3B%20s_invisit%3Dtrue%7C1546897660147%3B%20gpv_p5%3D%252Fgateway%252Fkwn%7C1546897660148%3B%20v15%3D1031602468%7C1578431860149%3B;
           s_sess=%20s_tp%3D3968%3B%20s_cc%3Dtrue%3B%20s_sq%3Ddiscoverglobalprod%25252C%252520discovercardservicingprod%253D%252526c.%252526a.%252526activitymap.%252526page%25253D%2525252Fgateway%2525252Fkwn%252526link%25253DLog%25252520In%252526region%25253Dlogin-form-content%252526pageIDType%25253D1%252526.activitymap%252526.a%252526.c%252526pid%25253D%2525252Fgateway%2525252Fkwn%252526pidt%25253D1%252526oid%25253DLog%25252520In%252526oidt%25253D3%252526ot%25253DSUBMIT%3B%20s_ppv%3D%252Fgateway%252Fkwn%252C23%252C23%252C916%3B;
           marqueeIndex=1010;
           marqueeStatus=known;
           JSESSIONID=0000kJaNqu3nu7gyKTDAbTX1bJI:1cn5v5fm8;
           tluserID=oluwatobi1; tlsession=WG1sbytmNUhxcGZiRE9Nbkc2eTZhQ09Rc3VFPQ==;
           customerId=f942edff793c494da50066cc9fc6a08c;
           dfsedskey=1031602468;
           PMData=PMV6K7ODzGrvaZd0ziUm8sOKRLwXpOpk6LIqQm%2B9rSdTMQNK5SdipzrjPz24Ty2ff0OqhOEQO42h0139z2FYQre5DkBQ%3D%3D;
           dcsession=SIGNOFF;
           sectoken=K857olzLIOSsNaW1rfU3PLEMKM8=;
           PMValidated=2;
           ACLOGIN=incntvtyp1=CBB;
           ACHASH=901946029;
           wkp=FALSE;
           secTrack=conly;
           STRONGAUTHSVCS=SASID=null&SATID=c3d:45662701861:a35c2dd4_TRX&REQID=64d26161-2cda-430b-be28-f13e50ae2f61&;
           dcuserid=***CLEARED***;
           TS01ba2681=0140983af9cdbce6d4c9553df388c6238cbf71060c472b410f4a16a015c86fb38552e55ce365fefabee8b6d13e6cb72a5b470503d6;
           TS01388013=01e7db6daa6261db6eefa2ee870aee66042f87684621465371df41f6f793a4f6dd48a26aa6f6a36b454d3183358f5dcf8627635b4f6f11f94a181194eb220fd3e49916429f38c34e37888ad9dfd57dd543843fed7199623af0cd2cd39220786e918affb87d05f56a194e1b2414d893a0b8451389f587e3f6d1284f3066234a2d4fb76707d4f31a326be048044a0869d98558cafc7f9874638fd5778c81f4df563adca30cef69051f64edba8b33ac8ae4fd6b14624f96051a6f249a04f1d363d64ed8f12951a6cc0a808dd9b5f32a7ef0a6e7aec62140dfad310f16cf9dcca79201590c136145afb28d08b621031eed04b5b2b7b334c4894671938330388d7bf08e99d8cb482f5b57a7a50e65a1d02da26da11ad5de22333830b87c398d38a44025dcd744405d34baffe4abd6a52064e0e0b55a82c7;
           LPVID=NmYTBlZTc4ZGUzNDBmM2Yy;
           LPSID-3824612=k7KYLOZ8RAKnJTKSQfWKAg;
           SSO_LOGOFF=Y;
           dfswaf=!GqgkCVKP7pBxr7/4tb6jjgPCaWkO6wNR/HjtV+PBNzafTQZrlLp+RRzIy4KsraLSpXFYwlsg/63x5hqw7lMXdLpa0wFZSWXgtWyl2SiW6w==;
           ak_bmsc=9891B33D52DE5DAF4687187642F9CF6848F62BDCED190000E3C1335C4A01A835~plVjVSBKDt9XERCLcGkEkO1vTaerfqfyTddqHCRSxFK5vJFwE2I+aeY13hXvkGdJpbCYklDc9ClNEtoQhxuGoPuiPwBPyaN4DZTeSl2Y/NubRklUTGJ83kpn9+qcJS8yrDMMOEYgL5kXAdWqB1VoYJTOA9eEwOPOQ8cJZ0r+uI2+40PzzcZEXcAFwQsUIIxk3MLFcagIwlmsc3C8a6D9WaiUc+E2kbipQvT1WkdU1U5do=;
           mbox=session#f99badb5a7d641108cbef8bd440cadb9#1546897705;
           bm_sv=BC1333E28F3C69EB854E5A4E524A163C~rK+2XuNjfftl/SqXtR0PdJHmQgCOTe8k2RkfAbXujrcLpNgvXVNSgEN7jkD0pJdhjnnJNBC8yIrzoIqkBfZ+42CpsEufNDsGxCG2rMPLa6OH7JTSb9BUWH7yRRIMi5dw8/lnvUylaHqyJQIYsggWyam/GIDp96CgyHdrS8QYBes=
    Upgrade-Insecure-Requests: 1""")

(comment
  "From driver: source"
  """Host: portal.discover.com
  User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36
  Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8
  Accept-Encoding: gzip, deflate, br
  Accept-Language: en-US,en;q=0.9
  Cache-Control: max-age=0
  Connection: keep-alive
  Content-Length: 5060
  Content-Type: application/x-www-form-urlencoded
  Cookie: JSESSIONID=0000T-YMZkSHivE-OExNYuTM3lH:1a0e645kt;
  tlsession=WlM1Qkp3ZFBlb2RxMnI2dGZ4Ym1JbnZSSFBVPQ==;
  DCID=www12;
  ak_bmsc=F5C33896E6EA61DFF29EB8B041D01BB0B83365DD56060000C9C5335CD9F7A833~pl2CNXx1zg/j0+jCobNQ/idNVtYrktBHzJvZKRxxQfcQfSF/heC0Yq9lnuxRRe156uaXjrDr1oX0qy7KMxCtnbA7yT+hAJR3s7k57UxysXkze50NaZiM/scjcAdxV6CpgFK9n42a6Nxoa5Oqej/ve6bOrR3ghvNWEHIX2cdT2cu5yvmc/1jUGPEMh/vzjA6YTBchPHdIHA2Fm2a4adP2q8icd/K4b591uznlMdHuX39iU=;
  AMCVS_0D6C4673527839230A490D45%40AdobeOrg=1;
  AMCV_0D6C4673527839230A490D45%40AdobeOrg=1406116232%7CMCIDTS%7C17904%7CMCMID%7C84189240587356800153837489574522167565%7CMCAAMLH-1547501642%7C7%7CMCAAMB-1547501642%7CRKhpRz8krg2tLO6pguXWp5olkAcUniQYPHaMWWgdJ3xzPWQmdj0y%7CMCOPTOUT-1546904042s%7CNONE%7CMCAID%7CNONE%7CMCSYNCSOP%7C411-17911%7CvVersion%7C2.5.0;
  xp1id=SY-00IHtAADCmXCc=630;
  TS01388013=01e7db6daa0e9f609b3c1aea091e00324023abf3366b0495bb094386547e083ed93bbf09e62372f8634a2e7d6267d5ea3aa14947ec98ba1aa1ffe39194547e81a18f2999f7;
  bm_sv=25D3252864B296C268068DD614FB41CC~pCn4XZLL1C7BpdEi2vHxScZioOAKuKufCnix0BOPoerlrIgoPnEIN08HwqZhv2JlLOeW4wcZFlhicGyPFgavvRzV/O6yWDGUvUNljFkjCmX9CD8KPIV4DleTCRSayHRCY6omks4WzTTrHwqFyrKE85u3X5TrKO8rOeXOqJh9fV8=;
  TS01ba2681=0122e201cf04f4f86997ddfe6f6c6dbcb6f72eb5683c50882927d2d240dbd330d046b9589acf3e2b7e0575d904a863e1c37cfc07fb;
  LPVID=JlN2YxNWU0NDE5MTkzMzUz;
  LPSID-3824612=_jttoN9MR16iaqieJhi6hA;
  marqueeIndex=1010;
  marqueeStatus=known;
  s_pers=%20s_dfa%3Ddiscoverglobalprod%252C%2520discovercardservicingprod%7C1546898642009%3B%20s_ev31%3D%255B%255B%2527hdr_logn_cc_logn%2527%252C%25271546896842590%2527%255D%255D%7C1704663242590%3B%20s_vnum%3D1549000800595%2526vn%253D1%7C1549000800595%3B%20s_invisit%3Dtrue%7C1546898823754%3B%20gpv_p5%3Dcustomersvcs%252FuniversalLogin%252Fac_main%7C1546898823761%3B;
  s_sess=%20s_cc%3Dtrue%3B%20s_tp%3D1343%3B%20s_sq%3Ddiscoverglobalprod%25252C%252520discovercardservicingprod%253D%252526c.%252526a.%252526activitymap.%252526page%25253Dcustomersvcs%2525252FuniversalLogin%2525252Fac_main%252526link%25253DLog%25252520In%252526region%25253Dlogin-form-content%252526pageIDType%25253D1%252526.activitymap%252526.a%252526.c%252526pid%25253Dcustomersvcs%2525252FuniversalLogin%2525252Fac_main%252526pidt%25253D1%252526oid%25253DLog%25252520In%252526oidt%25253D3%252526ot%25253DSUBMIT%3B%20s_ppv%3Dcustomersvcs%252FuniversalLogin%252Fac_main%252C69%252C69%252C925%3B
  Origin: https://portal.discover.com
  Referer: https://portal.discover.com/customersvcs/universalLogin/ac_main?ICMPGN=HDR_LOGN_CC_LOGN
  Upgrade-Insecure-Requests: 1""")



(comment
  """POST /customersvcs/universalLogin/signin HTTP/1.1
  Host: portal.discover.com
  Connection: keep-alive
  Content-Length: 5971
  Cache-Control: max-age=0
  Origin: https://portal.discover.com
  Upgrade-Insecure-Requests: 1
  Content-Type: application/x-www-form-urlencoded
  User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36
  Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8
  Referer: https://portal.discover.com/customersvcs/universalLogin/signin
  Accept-Encoding: gzip, deflate, br
  Accept-Language: en-US,en;q=0.9
  Cookie: tlsession=WlM1Qkp3ZFBlb2RxMnI2dGZ4Ym1JbnZSSFBVPQ==;
  DCID=www12;
  ak_bmsc=F5C33896E6EA61DFF29EB8B041D01BB0B83365DD56060000C9C5335CD9F7A833~pl2CNXx1zg/j0+jCobNQ/idNVtYrktBHzJvZKRxxQfcQfSF/heC0Yq9lnuxRRe156uaXjrDr1oX0qy7KMxCtnbA7yT+hAJR3s7k57UxysXkze50NaZiM/scjcAdxV6CpgFK9n42a6Nxoa5Oqej/ve6bOrR3ghvNWEHIX2cdT2cu5yvmc/1jUGPEMh/vzjA6YTBchPHdIHA2Fm2a4adP2q8icd/K4b591uznlMdHuX39iU=;
  AMCVS_0D6C4673527839230A490D45%40AdobeOrg=1; AMCV_0D6C4673527839230A490D45%40AdobeOrg=1406116232%7CMCIDTS%7C17904%7CMCMID%7C84189240587356800153837489574522167565%7CMCAAMLH-1547501642%7C7%7CMCAAMB-1547501642%7CRKhpRz8krg2tLO6pguXWp5olkAcUniQYPHaMWWgdJ3xzPWQmdj0y%7CMCOPTOUT-1546904042s%7CNONE%7CMCAID%7CNONE%7CMCSYNCSOP%7C411-17911%7CvVersion%7C2.5.0;
  xp1id=SY-00IHtAADCmXCc=630;
  LPVID=JlN2YxNWU0NDE5MTkzMzUz;
  LPSID-3824612=_jttoN9MR16iaqieJhi6hA;
  marqueeIndex=1010;
  marqueeStatus=known;
  tluserID=oluwatobi1;
  TS01388013=018fa0005013e523e48e5997c925055857032a2574fe57c04621fe5d71542ca7f5ef10de472807da5726729f50d87346fb3c49968c616f0b4f7751a363c27faea934398ef9;
  JSESSIONID=00014uaINJwQX114Qh5ZgYyReOT:1cmc86977;
  TS01ba2681=018fa00050eb267568397d638f65396054350b300ffe57c04621fe5d71542ca7f5ef10de47a0f05e130f4f5c3964590ed98717b241d494ca88b6c8d7b21504c5cb4f716002;
  bm_sv=25D3252864B296C268068DD614FB41CC~pCn4XZLL1C7BpdEi2vHxScZioOAKuKufCnix0BOPoerlrIgoPnEIN08HwqZhv2JlLOeW4wcZFlhicGyPFgavvRzV/O6yWDGUvUNljFkjCmWtJ4CLDhYDh8D1Gozsb4h0apQo0YbykubblvdBrUNvaQB6O8/mXS8mYBZRAgwcz8M=; s_pers=%20s_vnum%3D1549000800595%2526vn%253D1%7C1549000800595%3B%20s_fid%3D4CBF2C90F7D800D8-2F45D61A1B3D1DD3%7C1610055682000%3B%20s_ev31%3D%255B%255B%2527hdr_logn_cc_logn%2527%252C%25271546897285207%2527%255D%255D%7C1704663685207%3B%20s_dfa%3Ddiscoverglobalprod%252C%2520discovercardservicingprod%252Cdiscovercardservicingprod%7C1546899100040%3B%20s_invisit%3Dtrue%7C1546899123217%3B%20gpv_p5%3DInvalidLogin%7C1546899123225%3B; s_sess=%20s_cc%3Dtrue%3B%20s_tp%3D1532%3B%20s_sq%3Ddiscoverglobalprod%25252C%252520discovercardservicingprod%253D%252526c.%252526a.%252526activitymap.%252526page%25253DInvalidLogin%252526link%25253DLog%25252520In%252526region%25253Dlogin-form-content%252526pageIDType%25253D1%252526.activitymap%252526.a%252526.c%252526pid%25253DInvalidLogin%252526pidt%25253D1%252526oid%25253DLog%25252520In%252526oidt%25253D3%252526ot%25253DSUBMIT%3B%20s_ppv%3DInvalidLogin%252C63%252C32%252C968%3B;
  at=1
  """)


(comment
  "Driver login form posted"
  """userID: oluwatobi1
password: T6BxgHC2CtlqvC1f
accountType:
userTypeCode: C
pm_fp: version=1&pm_fpua=mozilla/5.0 (windows nt 10.0; win64; x64) applewebkit/537.36 (khtml, like gecko) chrome/70.0.3538.110 safari/537.36|5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36|Win32&pm_fpsc=24|1920|1080|1040&pm_fpsw=&pm_fptz=-6&pm_fpln=lang=en-US|syslang=|userlang=&pm_fpjv=0&pm_fpco=1
currentFormId: login
link:
intcpt:
ssid: bff406f5-4dc8-4653-8c99-6378f670217-1546897974623
X-lHtlUSrt-uniqueStateKey: A57iTSpoAQAA_KnVDxBUzQ3_DQNRRYCl_7GGGjInJKqpBfxNbnYEzSV_hBi_AawUKfGucvDHwH8AABszAAAAAA==
X-lHtlUSrt-b: 5b1nmx
X-lHtlUSrt-c: AxXYTSpoAQAAyVSnSYgqzhq9CjO458pVPDWBKsapZ7m222YDNfCokSmxFEXjAawUKfEJ-PDHwH9gfTJdYD2ykA==
X-lHtlUSrt-d: 0
X-lHtlUSrt-a: lkqp8AaEYIs61FlTn9XWwd-6=QoGP9fQrdX5_-Xjfh6TV1Xqn9PS1eaRmBZwPtBv8GFi=L8zDzpw1NW5HcYJ89QyyYTb81oFY9lbvb-4-3hTYRqKlIslsiZx=zShVI6G0TFXWEqOwdLeteAR-1-7Ow8JtAZxr=4BM=vRWI8vHdoJBmUR8wwSs6ozWjsGM99omeb51Y8cEKocfi_f0dBXV4iz-ZspVhvx=mmHPg-SPdsTDjnZ89QRHxpR0QmJSo8z=Qvq0I_sBuKlVwqzndBiW3ZGmx0xPRSTsMQx=t1SyQnEWQ8wq7F6BdGAPnqc1FFx0zaOY9hfWjW0PcYzV98PmcyR06vS0etTM-bK1O0SEKab1zYdI3izysWTzLYGVIQxHdXqAdbr0yWN0EqEsSqtBQBk0X-7y-pLP8taWSqFnN=TMiHS1IqRyQtl0dBsZqbGm9Vf0xoX_X7EBbYJY98z89taBdwl-9tSsnipDdaJIw86sQ8XwJ6Swzf7mw-eVtQHSSOG1ewG0emEfe4WMwZjDJSemM8BrR2Mt_8f068cPni7jZY7Hyol4aSIBh_HWI4afIB48=Wy1dDTDL8r=I_4l=v4WIuTmcFh1bKq0bDrkdK60LvRMjaFYJqXW3QWBQqGwJ4GslOR0GYJwJfzn3oJSixos4Q4N3iethQRN1Bbl6mMSEqJmXYz0MYF0DQTPMYc0d8lmni7P9BXlsKT04acHzvxWQYJ-wocv5WaoQmM0yYX-ttTWsiXOcYGVMVYWQ3-VKSp0kqJ=1-dsd8JwDEBsiw4OX_OnV=xwf8qw_mLXyoJ1dtAkIwEs3qnS38hnhY31GPHYdozWESOkmSh0wFO1RSSYZAIBiAW1iWTvRqiP9uSfjYzyIv9wzfQHzbq1FSlMQ6HsMi4HfsSteB79Ff6PtipnsozfQB3-QYGj7YzwAFwsLWcDdyO0eQlJeoZfIXxHX_csE_J-ImcEXA4JRERP9b4mm8xyQqqmxnTVDrqVlqs1poXmX1S=qbeDgWc-vfi8spRwdVr0ESalNoJAdJRwb8F1dfHVkW5z7Y38j8X0LixBk3TEeZS168EV1=Xz4uGQeXX1RqXqSqX-QtlSjk4snibKzlT-E4T8LqXBnBsWIoi1xXL=s4SSSiw=5qe=w85VMsOmupd0TSYP1opH5i313EnMwoX0QWa1xVXNmTTOZX=fIk6wzaRHEzNBjmTqi_XsxBEBwsrB9B7-I4Ta5iqPel7ltoX5GjfwdBzV9rrPmhdsI_it6Y4lRSh1dGR1eBXm6nN-IB7V7RKYzvGM9BFAIvI=kfck1OOyj8i1doxmKqHWQ87ne_7y9LRmMZo-NoJVw8F=QWJyQoQJ9iJWQqBVIaxP-8xiMoJVMo5JgWTWjo7WEuv-6oKExQHVtB5EAYSHz74-jvEqh7q15odV9gQsvgK0n-SHLiqb4FsPLvxVLmis9BpWhwE06A4t4cH0xqzs68esdXsjzjR04VHlKbJ0Eq0YnBF0is5lcTzrti5sGhh8Aqb=q-cNPqkW3iF0taEPdPhPxZG05k6dIXc0GiFHmmaBEq7-k_wm9QRPBT0BdYq=9trHTQFHdBJO9a70NozP9FXVMNXQnSbHL8qH5oZadiUjefcOzgISmPbEefn13qc8ATPyqXl0JqJ-Ibi1GVXsDqX-YrjmTqXBdA4Wi_X-IIFP9zz16nGrQvIsm8s-tGl09AA8AXXYJfW1I1OP7XzzeDLrTqr-I4hBM87wdhbH5YOS9QjubXKleXKE4aOs7TlP6oGVw8XH5WsPz8Jm7F5-QnY-3=RBIBc1qVJVo4IsiyXHRqfWiAU1QqOVk3UHdwxPJfnPxRZ1D-z0t-KvysaYItTqX8ZwGYXV9bEqyYEnIZqsQFE16os1lnR-9u_rRSa0NVz1AGHn3tzPGqr=mqeH6tzsqFZyI_Kt6tTQZjewJqq05sJVUDJYD=TthiEsdu2-9m5mdBosk_Q0dbz1dB3V9tO-9nGsgiGVjY7Vd_K-hAxNEfQ1Rqp-7FXS9afmgVOH6oG-rOOfQ8qPXD3PAFxg4iewzld=1o7-woXHeLxr3jRHdzTNQoXwL82s6oXVL8cWjvHge__wAFx-3Q4V9aJV9alV9VR0dmTPmAEe98XvJqeWhatOX1aWIGFE9DhqjN4is09lIYSs9BUBN0Ev38P04uxPATh1sSw1RZHi0FzknBi06FfH6nGtzBGs9wHsnoEq3=RnMsb0QqzHniiP9_F-9yRwd8cmIoX-IwR1DBKYNhb3eBcP4o4-IfX0LoLVSwG-3iPPw8cNmUYVLtT0IzhnzlE0dY51ds1WSiF1R3HUlvxYzpKyZxxsmqcreYX13QR1eARskSTWsieM6xH05vWNIyRwLqGW3IRsmYOJMZ4OF8iHewT=Q8F0ad4M96FB3il-sv8BeBmWIuH1eBzRL8EqjYU1XoJVGqz1Q8tneFbDdw4PmSTBbnGVwOs1AYX8wml04_wr5oGVIl3mLvxSdbtlRq5s9wks7qGfIwFV1SaoD-FlQraV4X7SsfF9i8ca6BzHN1TytoJ-lrr0L1TWQ3fyQBcPPEn0kXS14BkmevXL3-JM3E4sNYQ0ePaYTnM=J6WVnYfrpPTWhYQ1jYBPJ3x=m8n-IBb-iwolX-im6VK=RTWSx8HDI-Xwl3RhSqxVwoOoL86sEXwPk3RqS8OLBTTP4izwGibtylGN8R6-I8XDJ3JBdotd1os09uH0AsWVnO70EOs0d8XWEzTodiJ-3InH4KGyqAl0w9G4zp7VUicaIiQYlqEakp7Pym10Nv6PMjRHLvxsmqGPnkRm215se4i=qfFh3FQWQqQ-3YbsAPcnIHTDbFEVcYJlI8XMsqzYT3G04uRNiZx0AqXHRqbAbqxV7FX0zBoM3BzHAZWsMivPwqpVkqzm=W5-9Hgyj_p0ThmM9j-DdXQYdQE168Fm7xG-8bFPLZs=taiN9wlaehyYd6E-QYEV9WTaB1z14Q8qLmaOGSSAAYO-w8E=IRRrGiFsNoF2nZiy3Wl2dtTETqESNqF=d9Sse1TyIft-wxh1yqjEhizmkSHqZxTVMnp-3hSVti78tvx0cqwmL8zP4oO0sWh0AThrV_nsjYbWQTS0yYKALmTVI9Rwd_iSEuc-eaPHlq4leihvi_zNIi7yNVO=zt5H7sa-9ibHdBOazpc-IBz8AFrvX8c12UH1c6HMhYj9ESSEtDhOdV4-tQR0LmqOb_FYNoq-eBXWQYG0M8ZVeFiSjoFWIBf-4oG=w8zsSVJmI4uY97RHws5m9kT0dVRmyYFazlT8B3H-sT516YBY4aXWQo7VtB7VmSa0wX7WEqd0eo5BI4GSIYtVwsT1i8w0lqZyIobsEqd1Rqx8njRMIBJyIYcVBB30Xv20Qoq83xE-NzlsI4SlZT5fIiGHL8c8d_lPL8HlttcVdf7VsOl0d_48LnTDGPaIeAP0efpsJqFfIBX14WS0dBlWiyXV9hzN9xQqRTTHLZqWQWTJ3BO-BDG-wodJEqXMEqHtDq4Dd_X0e96-IfZtQNXZiozwLBJJ9tGN-Y50y84kL6x0hqJmeYK-i1LyEqp-9_q=kbb8bmTsSii1diQ0xYp-nqp06qcqQmGVm8316qfYIfG-nhrqIXX=Efirh4kqIwjP9fQs3xewL8zrEqJswoRsYFH=9iOVeakwL8XlzisV9ieV9B4edl31L8FwLSEsGSaM3fz8w8OVBZENIbRDIiish-izIHutlYqwGYGV9t5teXjPw=RGMmo-EFe068GMLoX1d_ESjOEvprd0L8ssBD30LoJin85HB-7m9RePA6RY9xKPAFrP9oFyIF4PkunBIixs9Y5mKBJwdAl8wqb0haGVNF7s96eBL15-3xRzIm3sjYOwdB7=96fqjY7wdY703oQ0dHJYgXxNQqF0IfzP9fzDbDTBI_cHX_bhha7-ZTJ=IBXP-Xwwzap1dXFwL8qHIuS1xxqHeHL
  """)



(defn get-page-at-address [address]
  (let [conn (Jsoup/connect address)
        soup (.get conn)]
    soup))



;(let [discover-soup (get-page-at-address "https://portal.discover.com/customersvcs/universalLogin/ac_main?ICMPGN=HDR_LOGN_CC_LOGN")]
 ; (println discover-soup))


(comment
  (jdbc/))
