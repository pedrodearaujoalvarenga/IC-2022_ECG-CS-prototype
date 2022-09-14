import numpy as np
import matplotlib.pyplot as plt

data = [
    #ordem: 20-240 + contínuo
[0.043126, 0.043126, 0.042797], #20mhz rajada
[0.049044, 0.048715, 0.048386], #40mhz rajada
[0.064825, 0.064825, 0.065154], #80mhz rajada
[0.08258, 0.082251, 0.082908], #160mhz rajada
[0.111841, 0.111512, 0.111841], #240mhz rajada
[0.066141, 0.074689, 0.084224]#contínuo

]
X = np.arange(3)
fig = plt.figure()
ax = fig.add_axes([0.1,0.1,0.80,0.80])

ax.bar(X + 0.00, data[0], color = '#1e81b0', width = 0.10)
ax.bar(X + 0.10, data[1], color = '#e28743', width = 0.10)
ax.bar(X + 0.20, data[2], color = '#eab676', width = 0.10)
ax.bar(X + 0.30, data[3], color = '#76b5c5', width = 0.10)
ax.bar(X + 0.40, data[4], color = '#abdbe3', width = 0.10)
ax.bar(X + 0.50, data[5], color = '#154c79', width = 0.10)

ax.set_xticks(X+0.25, ('Transmitindo a 80mhz', 'Transmitindo a 160mhz', 'Transmitindo a 240mhz'))
ax.set_yticks([0, 0.03, 0.06, 0.09, 0.12, 0.15])

ax.set_title('Consumo de Corrente em Rajada x Contínuo')

ax.legend(labels=['Gravando em rajada a 20mhz', 'Gravando em rajada a 40mhz', 'Gravando em rajada a 80mhz', 'Gravando em rajada a 160mhz', 'Gravando em rajada a 240mhz', 'Contínuo'], loc='upper right', prop={'size': 7})

plt.axhline(y = 0.15, linewidth=0.3, color=(0, 0, 0, 0.5), linestyle = '-')
plt.axhline(y = 0.12, linewidth=0.3, color=(0, 0, 0, 0.5), linestyle = '-')
plt.axhline(y = 0.09, linewidth=0.3, color=(0, 0, 0, 0.5), linestyle = '-')
plt.axhline(y = 0.06, linewidth=0.3, color=(0, 0, 0, 0.5), linestyle = '-')
plt.axhline(y = 0.03, linewidth=0.3, color=(0, 0, 0, 0.5), linestyle = '-')

plt.savefig("figure.png", dpi=800)