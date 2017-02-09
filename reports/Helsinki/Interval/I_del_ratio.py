#!/usr/bpin/python

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
flood = ('false', 'false', 'false', 'false', 'true', 'true', 'true')
cdf = ('Update', 'Relay')
scheme = ('App-Aware', 'App-Aware', 'App-Aware', 'App-Aware', 'App-Unaware', 'App-Unaware', 'App-Unaware')
relay = ('0', '300', '900', 'single', '300', '900', 'single')
updates = (30, 60, 120, 180, 240, 300, 360)
intervals = (0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
rng_max = 5
apps = 10


reports = ('MultiAppReport-App', 'MultiAppChecks-App', 'CDFRelayReport-App', 'CDFUpdateReport-App')
stats = ('Average_Percentage', 'Reject_Percentage', 'Relay_Percentage')

for x in xrange(len(flood)):
	# MultiAppReport
	for i in xrange(0, len(stats)):	
		file = open(stats[i]+'_'+scheme[x]+'_R-'+relay[x]+'.txt', 'w')
		file.write('update_interval avg max app min app\n')
		for update in updates:
			del_ratio = []
			for j in xrange(1, apps+1):
				fname = '%s_R-%s/I-%d/Interval_I-%d-%s_F-%s_MultiAppReport-App%d.txt' % (scheme[x], relay[x], update, update, relay[x], flood[x], j)
				del_ratio.append(gs.get_stat(fname,stats[i]))

	    	# Average delivery ratio
			avg = gs.get_average(del_ratio)			
			minimum = gs.get_min(del_ratio)
			maximum = gs.get_max(del_ratio)

			print '%d %.2f %.2f App%f %f App%f' % (update, avg, maximum[0], maximum[1], minimum[0], minimum[1])
			file.write(str(update)+' '+str(avg)+' '+str(maximum[0])+' '+str(maximum[1])+' '+str(minimum[0])+' '+str(minimum[1])+'\n')
		file.close()


	file = open('Success_Rate_'+scheme[x]+'_R-'+relay[x]+'.txt', 'w')
	file.write('update_interval avg max app min app\n')
	for update in updates:
		del_ratio = []
		for j in xrange(1, apps+1):
			fname = '%s_R-%s/I-%d/Interval_I-%d-%s_F-%s_MultiAppChecks-App%d.txt' % (scheme[x], relay[x], update, update, relay[x], flood[x], j)
			del_ratio.append(gs.get_stat(fname,'Average_Success_Rate'))

    	# Average delivery ratio
		avg = gs.get_average(del_ratio)			
		minimum = gs.get_min(del_ratio)
		maximum = gs.get_max(del_ratio)

		print '%s %.2f %.2f App%f %f App%f' % (update, avg, maximum[0], maximum[1], minimum[0], minimum[1])
		file.write(str(update)+' '+str(avg)+' '+str(maximum[0])+' '+str(maximum[1])+' '+str(minimum[0])+' '+str(minimum[1])+'\n')
	file.close()


	for stat in cdf:
		for update in updates:
			file = open('Cumulative_'+stat+'_Percentage_S-'+str(update)+'_'+scheme[x]+'_R-'+relay[x]+'.txt', 'w')
			file.write('time avg max app min app\n')
			for i in xrange(0, len(intervals)):
				del_ratio = []
				for j in xrange(1, apps+1):
					fname = '%s_R-%s/I-%d/Interval_I-%d-%s_F-%s_CDF%sReport-App%d.txt' % (scheme[x], relay[x], update, update, relay[x], flood[x], stat, j)
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