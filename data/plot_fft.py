import matplotlib.pyplot as plt
import math
fileName = 'double_inside_roll_pitch'
pitch = []
roll = []

with open(fileName) as fp:
    for line in fp:
        if 'V' in line:
            p = line.index('V')
            name = line[p+2:p+7]
            if name == 'pitch':
                pitch.append(float(line[p+9:]))
            if name == '_roll':
                roll.append(line[p+9:])
# plot
fig = plt.figure('pitch and roll')
ax1 = plt.subplot(121)
ax1.set_title('pitch')
ax1.plot(pitch,'ro',pitch,'k')
ax2 = plt.subplot(122)
ax2.set_title('roll')
ax2.plot(roll,'go',roll,'k')

# for i in range(0,len(fft_v)):
# 	if(i%64==0):
# 		ax1.plot((i, i), (min(fft_v), max(fft_v)), 'r-')
# for i in range(0,len(acc_v)):
# 	if(i%64==0):
# 		ax2.plot((i, i), (min(acc_v), max(acc_v)), 'r-')
plt.show()
