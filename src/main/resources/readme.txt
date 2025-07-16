Note:
1.avg price is calculated by last X day. Not including today real price!

Reporter content: 5 day indicator: just to notice curve raising.

// need manually update data
1. baiduInfoService.queryBaiduIncomeDataFromNetForAllStocks();
2. baiduInfoService.updatefinancialSum();


====================
issues:
1.The decimal place is wrong --- done 20250101
2.sz002916 and sh002916 both exist! how to filter? --- manually delete. done 20250101

=================================

need to do:
7.行业估值显示数据修复，有几个没显示，另外，方法是获取所有数据并update,需要增加判断只update近期的（一周一次就好）。
6. adding stock sortColum, to decouple code sorting with html view.  In future, need extract html code and stock value code building html---- 20250323
5. top 10 holders if has 社保？！
4. add 市值 ?
3.IMPORTANT!----calculate the upAndDown program! to choose which ETF is the best to do upAndDown!---done
2.if not a opening day, skip to do realtime query! In order to void sending the same email as yesterday's.---done
1.down 10days price---done;20230729

Function:
=====
20230729
just after the realTime loops to send an email. Reduce to send many emails! This to prevent qq mail to ban me sending email.

========
20230701
1.eightHourQuery method would run every day at 8:00, to get 10day avg price.
2.realTimeQuery method run every 10min(can config) to monitor
3.if the date yyyy-MM-dd is equal, it means the market is started!


In the end at 18:00, send a summary report: higher/lower than XX day price


==================
to judge if exceeds 10 day price
1.yesterday's end price lower than or equals 10 day price:
    this to avoid yesterday's price already higher than 10 day price
2.real price exceeds 10 day price