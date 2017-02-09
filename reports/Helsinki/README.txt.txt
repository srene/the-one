/////////////////////////////////////////////////////////////////
14/09/2016 Denis Shtefan
/////////////////////////////////////////////////////////////////
The following folders contain the final results obtained, following multiple simulations in the Helsinki city environment. The different folders represent different scenarios in which each parameter was varied for the different schemes. In general, 7 different schemes were defined: App-Aware with no relay (R-0), 5 min relay (R-300), 15 min relay (R-900) and single message relay (R-single); App-Unaware (Flooding) with 5 min relay, 15 min relay and single relay respectively. In each folder (except Relay) you will find 7 folders containing all raw reports for each of the 7 schemes. The numerous text files are the results for each metric as an average across all 10 applications per scenario. They also contain the upper and lower bound results corresponding to a particular application and were used to produce the plots. These text files were generated using two Python scripts for each scenario, which are also found here.

There are 6 result metrics in total. 4 are average results across one whole simulation and 2 are CDF results within one update interval (as averages across all intervals). These are described below (abbreviations in the brackets correspond to plot names in the Plots folders):

- Average percentage of destination nodes with up-to-date content at the end of each update interval, as an average across all intervals (Avg_Update)
- Average success rate of users checking their apps, i.e satisfaction rate, by the end of a simulation (Avg_Success)
- Percentage of messages sent by Relay nodes by the end of a simulation (Avg_Relay)
- Average Percentage  of messages rejected by receiving nodes by the end of a simulation (Avg_Reject)

- CDF percentage of users with up-to-date content within an update interval (CDF_Update)
- CDF percentage of messages sent by Relay nodes with an update interval (CDF_Relay)

/////////////////////////////////////////////////////////////////
Default scenario settings: 50 Source nodes, 1000 Destination Nodes, 10 applications, Wifi Direct radio. Details can be found in settings files in the Helsinki_settings folder.

Each scenario is briefly described below:

-Source
The number of source nodes was varied between 5 and 500, keeping number of Destination nodes the same. The additional source nodes were distributed amongst bus drivers and 'office-day' users. (See settings for more detail).

-Message
The size of update messages were varied between 0.5 and 200 MB.

-Interval
The interval between each application update was varied between 30 and 360 min.

-Relay
As an exception, there are no separate results for the 7 schemes mentioned above. Instead, duration of relaying was varied between 0 and 3600 seconds (No relay to 1 hour relay in 5 min increments) for the App-Aware and App-Unaware schemes. 

/////////////////////////////////////////////////////////////////
The result plots are found in the Plots folders for each scenario. Error bars correspond to upper and lower bound results for a particular application (1 to 10 in decreasing Zipf popularity), as opposed to standard deviation or confidence level margins, since there were no runs with varied seeds for the same parameter settings. 

Note: The upper and lower bounds don't always correspond to the same applications for each data point (i.e most popular vs least popular) but do indicate trends. For some results the most or even the least popular application has the best results and for some popularity seems irrelevant so these bounds are randomized. I did not find a suitable way to label the exact applications corresponding to the upper/lower bands in the plots but these are indicated in the average result text files for each scenario. 






