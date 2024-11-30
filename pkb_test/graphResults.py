import numpy as np
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import os
import re

def represent_scientific(number):
    if number == 0: return "0"
    power = 0
    while number >= 10:
        power += 1
        number /= 10
    if number % 1 == 0: number = int(number)
    if number == 1: return f"$10^{power}$"
    return f"${number} \\cdot 10^{power}$"

def plot_results(data, filename):
    minQuantity, maxQuantity, maxBuffer, threads = re.findall('\\d+', filename)
    legend = ('2 Condition', '4 Condition', '3 Lock')
    for data_col in range(1, data.shape[1]):
        plt.plot(data[:, 0], data[:, data_col], ".-", label=legend[data_col - 1])
    plt.suptitle(f"Wyniki testów przy wykorzystaniu {threads} wątków", fontsize=14)
    plt.title(f"Parametry: produkcja/konsumpcja od {minQuantity} do {maxQuantity} przy buforze {maxBuffer}", fontsize=10)
    plt.xlabel("Czas działania [s]")
    plt.ylabel("Liczba wykonanych operacji")
    locs, labels = plt.yticks()
    plt.yticks(ticks=locs[1:], labels=list(map(lambda x: represent_scientific(x._y), labels))[1:], rotation=45)
    plt.legend(loc="upper left")
    plt.savefig(f"{filename}.png")
    plt.close()

if __name__ == "__main__":
    plt.rcParams["figure.figsize"] = (9, 6)
    plt.rcParams["savefig.dpi"] = 300
    files = list(filter(lambda x: len(x) > 5 and x[-4:] == ".csv", os.listdir()))
    for filename in map(lambda x: x[:-4], files):
        data = np.genfromtxt(f"{filename}.csv", delimiter=',', dtype=np.int64)
        plot_results(data, filename)
