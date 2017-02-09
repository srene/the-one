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
prioritize = ('true', 'false')
apps = ('BBC', 'SportsApp', 'Weather')
rng_max = 5

reports = ('MultiAppReport', 'MultiAppChecks')
stats = ('Average_Percentage', 'Average_Success_Rate')


for i in xrange(len(reports)):
	for k in xrange(len(apps)):
		file = open(stats[i]+'-'+apps[k]+'.txt', 'w')
		file.write('priority avg ci\n')
		for priority in prioritize:
			del_ratio = []
			for j in xrange(1, rng_max+1):
				fname = 'Priority_%s_%d_%s-%s.txt' % (priority, j, reports[i], apps[k])
				del_ratio.append(gs.get_stat(fname,stats[i]))

	    	# Average delivery ratio
			avg = gs.get_average(del_ratio)
			sd = gs.get_std_dev(del_ratio)
			ci = gs.confidence_interval_mean(rng_max, sd)

			print '%s %.2f %.4f' % (priority, avg, ci,)
			file.write(str(priority)+' '+str(avg)+' '+str(ci)+'\n')
		file.close()
    
file = open('delivery_prob.txt', 'w')
file.write('priority avg ci\n')
for priority in prioritize:
	del_ratio = []
	for j in xrange(1, rng_max+1):
		fname = 'Priority_%s_%d_MessageStatsReport.txt' % (priority, j, )
		del_ratio.append(gs.get_stat(fname,'delivery_prob'))

	avg = gs.get_average(del_ratio)
	sd = gs.get_std_dev(del_ratio)
	ci = gs.confidence_interval_mean(rng_max, sd)

	print '%s %.2f %.4f' % (priority, avg, ci,)
	file.write(str(priority)+' '+str(avg)+' '+str(ci)+'\n')
file.close()