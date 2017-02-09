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
hosts = (25, 45, 65, 85, 105, 125)
rng_max = 5

reports = ('MultiAppReport-BBC', 'MultiAppChecks-BBC', 'MessageStatsReport.txt')
stats = ('Average_Percentage', 'Average_Success_Rate', 'delivery_prob')


for i in xrange(len(reports)):
	file = open(stats[i]+'.txt', 'w')
	file.write('host avg ci\n')
	for host in hosts:
		del_ratio = []
		for j in xrange(1, rng_max+1):
			fname = 'Density_%d-hosts_%d_%s' % (host, j, reports[i],)
			del_ratio.append(gs.get_stat(fname,stats[i]))

	    # Average delivery ratio
		avg = gs.get_average(del_ratio)
		sd = gs.get_std_dev(del_ratio)
		ci = gs.confidence_interval_mean(rng_max, sd)

		print '%s %.2f %.4f' % (host, avg, ci,)
		file.write(str(host)+' '+str(avg)+' '+str(ci)+'\n')
	file.close()
    