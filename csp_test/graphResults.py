import numpy as np
import matplotlib.pyplot as plt
import matplotlib.ticker as ticker
import os
import re

def represent_scientific(number):
    if number == 0: return "0"
    elif number < 10000:
        if number % 1 == 0: return f"{int(number)}"
        else: return f"{number:.2f}"
    power = 0
    while number >= 10:
        power += 1
        number /= 10
    if number % 1 == 0: number = int(number)
    if number == 1: return f"$10^{power}$"
    return f"${number} \\cdot 10^{power}$"

def plot_results(data, dirname):
    numProducers, numConsumers, numBuffers, bufferSize, itemSize = np.array(re.findall('\\d+', dirname), dtype=np.int64)

    mean_data = np.mean(data[1:], axis=0)
    
    producer_indices = np.arange(start=1, stop=numProducers + 1, step=1)
    consumer_indices = np.arange(start=numProducers + 1, stop=numProducers + numConsumers + 1, step=1)
    buffer_indices = np.arange(start=numProducers + numConsumers + 1, stop=numProducers + numConsumers + numBuffers + 1, step=1)
    
    producer_names = list(range(numProducers))
    consumer_names = list(range(numConsumers))
    buffer_names = list(range(numBuffers))

    producer_data = mean_data[producer_indices]
    consumer_data = mean_data[consumer_indices]
    buffer_data = mean_data[buffer_indices]

    numPasses = data.shape[0] - 1

    plot_barchart(producer_names, producer_data, mean_data[0], "producentów", "Producenci", f"{dirname}/plot_producers", numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize)
    plot_barchart(consumer_names, consumer_data, mean_data[0], "konsumentów", "Konsumenci", f"{dirname}/plot_consumers", numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize)
    plot_barchart(buffer_names, buffer_data, mean_data[0], "buforów", "Bufory", f"{dirname}/plot_buffers", numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize)

def plot_barchart(names, values, middleman_operations, title, xlabel, filepath, numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize):
    plt.bar(names, values, color='steelblue', label=xlabel)
    
    plt.suptitle(f"Średnia liczba operacji {title} po {numPasses} testach", fontsize=22)
    plt.title(f"Liczba: producentów {numProducers}, konsumentów {numConsumers}, buforów {numBuffers},\npojemność buforu {bufferSize}, rozmiar elementu {itemSize} [B]", fontsize=18)
    plt.xlabel(xlabel)
    plt.ylabel("Liczba wykonanych operacji")

    locs, labels = plt.xticks()
    locs = list(filter(lambda x: int(x) == x, locs))
    plt.xticks(ticks=locs[1:-1], labels=list(map(lambda x: f"{xlabel[0]} {int(x)}", locs))[1:-1])

    locs, labels = plt.yticks()
    plt.yticks(ticks=locs[1:], labels=list(map(lambda x: represent_scientific(x._y), labels))[1:], rotation=45)

    plt.tight_layout()
    plt.savefig(f"{filepath}.png")
    plt.close()

if __name__ == "__main__":
    plt.rcParams["figure.figsize"] = (9, 6)
    plt.rcParams["savefig.dpi"] = 300
    plt.rcParams.update({'font.size': 16})

    dirs = list(filter(lambda x: "." not in x, os.listdir()))
    
    for dirname in dirs:
        data = np.genfromtxt(f"{dirname}/results.csv", delimiter=',', dtype=np.int64)
        plot_results(data, dirname)
