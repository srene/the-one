import csv

'''
A simple module with a method to get the value of a statistic 
from the MessageStatsReport of the ONE simulator. Also provides 
a method to compute the 95% confidence interval from a set of sample values.
'''

__author__ = "Barun Kumar Saha"
__copyright__ = "Copyright 2013, Barun Kumar Saha"
__license__ = "MIT"
__version__ = "1.0"


# Average of a list of numbers
def get_average(numbers = []):
	avg = 0.0
	n = len(numbers)
	for i in xrange(0, n):
		avg += numbers[i]
	avg /= n
	return avg


# Std. Dev. of a list of numbers
def get_std_dev(num = []):
	n = len(num)
	avg = get_average(num)
	
	variance = 0.0
	for i in xrange(0, n):
		variance += (num[i] - avg) ** 2
	variance /= n
	std = variance ** 0.5
	return std


# Get a named statistic from the MessageStats report file
#def get_stat(file_name, stat_name = 'delivery_prob'):
def get_stat(file_name, stat_name):
	result = 0.0
	with open(file_name, 'r') as report:
		reader = csv.reader(report, delimiter = ' ')
		for line in reader:
			#if not line:
			#	break
			if line:
				if line[0].find(stat_name) == 0:
					result = float(line[1])
					break
	
	return result

def get_min(num = []):
	n = len(num)
	m = [100000, 0]
	for i in xrange(0,n):
		if (num[i] < m[0]):
			m[0] = num[i]
			m[1] = (i+1)
	return m

def get_max(num = []):
	n = len(num)
	m = [-1, 0]
	for i in xrange(0,n):
		if (num[i] > m[0]):
			m[0] = num[i]
			m[1] = (i+1)
	return m



#
# t-distribution table
#
#Tail Probabilities
#One Tail   0.10    0.05    0.025   0.01    0.005   0.001  0.0005
#Two Tails  0.20    0.10    0.05    0.02    0.01    0.002  0.001
#-------+---------------------------------------------------------+-----
# D   1 |   3.078   6.314  12.71   31.82   63.66  318.3   637     |   1
# E   2 |   1.886   2.920   4.303   6.965   9.925  22.330  31.6   |   2
# G   3 |   1.638   2.353   3.182   4.541   5.841  10.210  12.92  |   3
# R   4 |   1.533   2.132   2.776   3.747   4.604   7.173   8.610 |   4
# E   5 |   1.476   2.015   2.571   3.365   4.032   5.893   6.869 |   5
# E   6 |   1.440   1.943   2.447   3.143   3.707   5.208   5.959 |   6
# S   7 |   1.415   1.895   2.365   2.998   3.499   4.785   5.408 |   7
#     8 |   1.397   1.860   2.306   2.896   3.355   4.501   5.041 |   8
# O   9 |   1.383   1.833   2.262   2.821   3.250   4.297   4.781 |   9
# F  10 |   1.372   1.812   2.228   2.764   3.169   4.144   4.587 |  10
#    11 |   1.363   1.796   2.201   2.718   3.106   4.025   4.437 |  11
# F  12 |   1.356   1.782   2.179   2.681   3.055   3.930   4.318 |  12
# R  13 |   1.350   1.771   2.160   2.650   3.012   3.852   4.221 |  13
# E  14 |   1.345   1.761   2.145   2.624   2.977   3.787   4.140 |  14
# E  15 |   1.341   1.753   2.131   2.602   2.947   3.733   4.073 |  15
# D  16 |   1.337   1.746   2.120   2.583   2.921   3.686   4.015 |  16
# O  17 |   1.333   1.740   2.110   2.567   2.898   3.646   3.965 |  17
# M  18 |   1.330   1.734   2.101   2.552   2.878   3.610   3.922 |  18
#

# Get CI of a mean
# Currently hard coded for sample size = 10, 95% CI

## 95% only
__t_values = {
	1: 12.71, 
	2: 4.303, 
	3: 3.182, 
	4: 2.776, 
	5: 2.571, 
	6: 2.447, 
	7: 2.365, 
	8: 2.306, 
	9: 2.262, 
	10: 2.228, 
	11: 2.201, 
	12: 2.179, 
	13: 2.160, 
	14: 2.145, 
	15: 2.131, 
	16: 2.120, 
	17: 2.110, 
	18: 2.101,
}

def confidence_interval_mean(sample_size, sample_sd):
	'''Only 95% CI'''
	
	# If sample_size < 30 and population SD is unknown, use t distribution
	# Else use std. normal distribution
	delta = 0
	root_n = sample_size ** 0.5
	
	if sample_size < 30:
		df = sample_size - 1
		# t for 95% CI and df = 10 - 1 = 9
		t = __t_values[df]
		delta = t * sample_sd / root_n
	else:
		delta = 1.96 * sample_sd / root_n
	
	return delta
