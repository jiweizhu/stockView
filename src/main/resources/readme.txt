Note:
1.avg price is calculated by last X day. Not including today real price!

Reporter content: 5 day indicator: just to notice curve raising.



====================
issues:
1.The decimal place is wrong
2.sz002916 and sh002916 both exist! how to filter?

=================================

need to do:
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