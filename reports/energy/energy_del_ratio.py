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
interfaces = ('btInterface', 'wifiDirect')
relays = ('0', '300')
rng_max = 5

reports = ('MultiAppReport-BBC', 'MultiAppChecks-BBC', 'EnergyReport')
stats = ('Average_Percentage', 'Average_Success_Rate', 'Average_Source_Energy')


for i in xrange(len(reports)):
	for relay in relays:
		file = open(stats[i]+'_R-'+relay+'.txt', 'w')
		file.write('interface avg ci\n')
		for interface in interfaces:
			del_ratio = []
			for j in xrange(1, rng_max+1):
				fname = 'Energy_%s_R-%s_%d_%s.txt' % (interface, relay, j, reports[i])
				del_ratio.append(gs.get_stat(fname,stats[i]))

	    	# Average delivery ratio
			avg = gs.get_average(del_ratio)
			sd = gs.get_std_dev(del_ratio)
			ci = gs.confidence_interval_mean(rng_max, sd)

			print '%s %.2f %.4f' % (interface, avg, ci,)
			file.write(str(interface)+' '+str(avg)+' '+str(ci)+'\n')
		file.close()
    
for relay in relays:
	file = open('Average_Destination_Energy_R-'+relay+'.txt', 'w')
	file.write('interface avg ci\n')
	for interface in interfaces:
		del_ratio = []
		for j in xrange(1, rng_max+1):
			fname = 'Energy_%s_R-%s_%d_EnergyReport.txt' % (interface, relay, j)
			del_ratio.append(gs.get_stat(fname,'Average_Destination_Energy'))

		avg = gs.get_average(del_ratio)
		sd = gs.get_std_dev(del_ratio)
		ci = gs.confidence_interval_mean(rng_max, sd)

		print '%s %.2f %.4f' % (interface, avg, ci,)
		file.write(str(interface)+' '+str(avg)+' '+str(ci)+'\n')
	file.close()