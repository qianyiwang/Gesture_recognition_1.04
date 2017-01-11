import matplotlib.pyplot as plt
import math
fileName = 'gyroscope data/gyr_touchGlasses'
gyr_m = []

with open(fileName) as fp:
    for line in fp:
        if 'V' in line:
            p = line.index('V')
            name = line[p+2:p+7]
            if name == 'magni':
                gyr_m.append(float(line[p+9:]))
# plot
fig = plt.figure('pitch and roll')
ax1 = plt.subplot(111)
ax1.set_title('gyr_magnitude')
ax1.plot(gyr_m,'ro',gyr_m,'k')

# for i in range(0,len(fft_v)):
# 	if(i%64==0):
# 		ax1.plot((i, i), (min(fft_v), max(fft_v)), 'r-')
# for i in range(0,len(acc_v)):
# 	if(i%64==0):
# 		ax2.plot((i, i), (min(acc_v), max(acc_v)), 'r-')
plt.show()