import matplotlib.pyplot as plt
import math
fileName = 'acc data/acc_right_turn'
acc_m = []
pitch = []
roll = []

with open(fileName) as fp:
    for line in fp:
        if ';' in line:
            p = line.index(':')
            dataArr = line[p+2:]
            dataArr = dataArr.split(';')
            acc_m.append(dataArr[0])
            roll.append(dataArr[1])
            pitch.append(dataArr[2])
# plot
fig = plt.figure('pitch and roll')
ax1 = plt.subplot(131)
ax1.set_title('acc_magnitude')
ax1.plot(acc_m,'ro',acc_m,'k')
ax2 = plt.subplot(132)
ax2.set_title('roll')
ax2.plot(roll,'bo',roll,'k')
ax3 = plt.subplot(133)
ax3.set_title('pitch')
ax3.plot(pitch,'go',pitch,'k')

# for i in range(0,len(fft_v)):
# 	if(i%64==0):
# 		ax1.plot((i, i), (min(fft_v), max(fft_v)), 'r-')
# for i in range(0,len(acc_v)):
# 	if(i%64==0):
# 		ax2.plot((i, i), (min(acc_v), max(acc_v)), 'r-')
plt.show()