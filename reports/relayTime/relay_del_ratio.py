#!/usr/bin/python

'''
A simple to compute the delivery ratio of the messages (with 95% CI) under 
various scenarios.
'''
 
__author__ = "Barun Kumar Saha"
__copyright__ = "Copyright 2013, Barun Kumar Saha"
__license__ = "MIT"
__version__ = "1.0"


import csv
import _gen_stats as gs
import math


# Routers used
#routers = ('EpidemicRouter', 'SprayAndWaitRouter',)
#areas = ('500,500', '1000,1000', '1500,1500',)
relays = (0, 60, 120, 300, 600, 1200, 2400, 3600)
rng_max = 5

reports = ('MultiAppReport-BBC', 'MultiAppChecks-BBC', 'MessageStatsReport')
stats = ('Average_Percentage', 'Average_Success_Rate', 'delivery_prob')


for i in xrange(len(reports)):
	file = open(stats[i]+'.txt', 'w')
	file.write('relay avg ci\n')
	for relay in relays:
		del_ratio = []
		for j in xrange(1, rng_max+1):
			fname = 'Relay_%d_%d_%s.txt' % (relay, j, reports[i])
			del_ratio.append(gs.get_stat(fname,stats[i]))

	    # Average delivery ratio
		avg = gs.get_average(del_ratio)
		sd = gs.get_std_dev(del_ratio)
		ci = gs.confidence_interval_mean(rng_max, sd)

		print '%s %.2f %.4f' % (relay, avg, ci,)
		file.write(str(relay)+' '+str(avg)+' '+str(ci)+'\n')
	file.close()


file = open('Relay_Percentage.txt', 'w')
file.write('relay aavg ci\n')
for relay in relays:
	del_ratio = []
	for j in xrange(1, rng_max+1):
		fname = 'Relay_%d_%d_%s.txt' % (relay, j, 'MultiAppReport-BBC')
		del_ratio.append(gs.get_stat(fname,'Relay_Percentage'))

    # Average delivery ratio
	avg = gs.get_average(del_ratio)
	sd = gs.get_std_dev(del_ratio)
	ci = gs.confidence_interval_mean(rng_max, sd)

	print '%s %.2f %.4f' % (relay, avg, ci,)
	file.write(str(relay)+' '+str(avg)+' '+str(ci)+'\n')
file.close()

