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
flood = ('false', 'true')
folder = ('App-Aware', 'App-Unaware')
cdf = ('Update', 'Relay')
relays = (0, 300, 600, 900, 1200, 1500, 1800, 2100, 2400, 2700, 3000, 3300, 3600)
intervals = (0, 360, 720, 1080, 1440, 1800, 2160, 2520, 2880, 3240, 3600)
rng_max = 5
apps = 10


reports = ('MultiAppReport-App', 'MultiAppChecks-App', 'CDFRelayReport-App', 'CDFUpdateReport-App')
stats = ('Average_Percentage', 'Reject_Percentage', 'Relay_Percentage')

for x in xrange(len(flood)):
	# MultiAppReport
	for i in xrange(0, len(stats)):
		file = open(stats[i]+'_F-'+flood[x]+'.txt', 'w')
		file.write('relay_time avg max app min app\n')
		for relay in relays:
			del_ratio = []
			for j in xrange(1, apps+1):
				fname = '%s/R-%d/Relay_R-%d_F-%s_MultiAppReport-App%d.txt' % (folder[x], relay, relay, flood[x], j)
				del_ratio.append(gs.get_stat(fname,stats[i]))

	    	# Average delivery ratio
			avg = gs.get_average(del_ratio)			
			minimum = gs.get_min(del_ratio)
			maximum = gs.get_max(del_ratio)

			print '%s %.2f %.2f App%f %f App%f' % (relay, avg, maximum[0], maximum[1], minimum[0], minimum[1])
			file.write(str(relay)+' '+str(avg)+' '+str(maximum[0])+' '+str(maximum[1])+' '+str(minimum[0])+' '+str(minimum[1])+'\n')
		file.close()


	file = open('Success_Rate_F-'+flood[x]+'.txt', 'w')
	file.write('relay_time avg max app min app\n')
	for relay in relays:
		del_ratio = []
		for j in xrange(1, apps+1):
			fname = '%s/R-%d/Relay_R-%d_F-%s_MultiAppChecks-App%d.txt' % (folder[x], relay, relay, flood[x], j)
			del_ratio.append(gs.get_stat(fname,'Average_Success_Rate'))

    	# Average delivery ratio
		avg = gs.get_average(del_ratio)			
		minimum = gs.get_min(del_ratio)
		maximum = gs.get_max(del_ratio)

		print '%s %.2f %.2f App%f %f App%f' % (relay, avg, maximum[0], maximum[1], minimum[0], minimum[1])
		file.write(str(relay)+' '+str(avg)+' '+str(maximum[0])+' '+str(maximum[1])+' '+str(minimum[0])+' '+str(minimum[1])+'\n')
	file.close()


	for stat in cdf:
		for relay in relays:
			file = open('Cumulative_'+stat+'_Percentage_R-'+str(relay)+'_F-'+flood[x]+'.txt', 'w')
			file.write('time avg max app min app\n')
			for i in xrange(0, len(intervals)):
				del_ratio = []
				for j in xrange(1, apps+1):
					fname = '%s/R-%d/Relay_R-%d_F-%s_CDF%sReport-App%d.txt' % (folder[x], relay, relay, flood[x], stat, j)
					with open(fname, 'r') as report:
						reader = csv.reader(report, delimiter = ' ')
						for line in reader:
							if line:
								if line[0].find(str(i)) == 0:
									del_ratio.append(float(line[1]))
									#print '%.2f' % del_ratio[j-1]
									break

				avg = gs.get_average(del_ratio)
				minimum = gs.get_min(del_ratio)
				maximum = gs.get_max(del_ratio)

				print '%s %.2f %.2f App%f %f App%f' % (intervals[i], avg, maximum[0], maximum[1], minimum[0], minimum[1])
				file.write(str(intervals[i])+' '+str(avg)+' '+str(maximum[0])+' '+str(maximum[1])+' '+str(minimum[0])+' '+str(minimum[1])+'\n')
			file.close()