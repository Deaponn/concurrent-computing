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
    itemSize, bufferSize, numBuffers, numProducers, numConsumers = np.array(re.findall('\\d+', filename), dtype=np.int64)

    data = data[1:]
    print(filename)

    mean_data = np.mean(data, axis=0)
    print(mean_data[:3])
    producer_indices = np.arange(start=1, stop=numProducers + 1, step=1)
    consumer_indices = np.arange(start=numProducers + 1, stop=numProducers + numConsumers + 1, step=1)
    buffer_indices = np.arange(start=numProducers + numConsumers + 1, stop=numProducers + numConsumers + numBuffers + 1, step=1)
    
    producer_names = ["P {}".format(i) for i in range(1, numProducers + 1)]
    consumer_names = ["C {}".format(i) for i in range(1, numConsumers + 1)]
    buffer_names = ["B {}".format(i) for i in range(1, numBuffers + 1)]

    producer_data = mean_data[producer_indices]
    consumer_data = mean_data[consumer_indices]
    buffer_data = mean_data[buffer_indices]

    print(data.shape)
    numPasses = data.shape[0]

    plot_barchart(producer_names, producer_data, mean_data[0], "producentów", "Producenci", "p_" + filename, numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize)
    plot_barchart(consumer_names, consumer_data, mean_data[0], "konsumentów", "Konsumenci", "k_" + filename, numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize)
    plot_barchart(buffer_names, buffer_data, mean_data[0], "buforów", "Bufory", "b_" + filename, numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize)

def plot_barchart(names, values, middleman_operations, title, xlabel, filename, numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize):
    plt.bar(names, values, color='skyblue', label=xlabel)
    # plt.axhline(y=middleman_operations, color='r', linestyle='--', label=f"Middleman ({middleman_operations})")
    
    plt.suptitle(f"Średnia liczba operacji {title} po {numPasses} testach", fontsize=14)
    plt.title(f"Parametry: P {numProducers}, K {numConsumers}, B {numBuffers}, pojemność B {bufferSize}, rozmiar elementu {itemSize}", fontsize=10)
    plt.xlabel(xlabel)
    plt.ylabel("Liczba wykonanych operacji")

    locs, labels = plt.yticks()
    plt.xticks(rotation=90)
    plt.yticks(ticks=locs[1:], labels=list(map(lambda x: represent_scientific(x._y), labels))[1:], rotation=45)
    
    # plt.yscale("log")

    plt.savefig(f"{filename}.png")
    plt.close()

if __name__ == "__main__":
    plt.rcParams["figure.figsize"] = (9, 6)
    plt.rcParams["savefig.dpi"] = 300
    files = list(filter(lambda x: len(x) > 5 and x[-4:] == ".csv", os.listdir()))
    for filename in map(lambda x: x[:-4], files):
        data = np.genfromtxt(f"{filename}.csv", delimiter=',', dtype=np.int64)
        plot_results(data, filename)
