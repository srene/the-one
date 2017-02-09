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
checkIntervals = ('300', '1200')
checks = (12, 3)
relays = ('0', '300')
rng_max = 5

reports = ('MultiAppReport-BBC', 'MultiAppChecks-BBC', 'EnergyReport')
stats = ('Average_Percentage', 'Average_Success_Rate', 'Average_Source_Energy')


for y in xrange(len(relays)):
	for x in xrange(len(checkIntervals)):
		file = open('Cumulative_Success_Rate-'+checkIntervals[x]+'_R-'+relays[y]+'.txt', 'w')
		file.write('check_interval avg ci\n')
		for i in xrange(0, checks[x]):
			del_ratio = []
			for j in xrange(1, rng_max+1):
				fname = 'CDF_%s_R-%s_%d_CDFCheckReport-BBC.txt' % (checkIntervals[x], relays[y], j)
				with open(fname, 'r') as report:
					reader = csv.reader(report, delimiter = ' ')
					for line in reader:
						if line:
							if line[0].find(str(i)) == 0:
								del_ratio.append(float(line[1]))
								#print '%.2f' % del_ratio[j-1]
								break

			avg = gs.get_average(del_ratio)
			sd = gs.get_std_dev(del_ratio)
			ci = gs.confidence_interval_mean(rng_max, sd)

			print '%s %.2f %.4f' % (i, avg, ci,)
			file.write(str(i)+' '+str(avg)+' '+str(ci)+'\n')
		file.close()