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

    # plot_barchart(producer_names, producer_data, mean_data[0], "producentów", "Producenci", f"{dirname}/plot_producers", numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize)
    # plot_barchart(consumer_names, consumer_data, mean_data[0], "konsumentów", "Konsumenci", f"{dirname}/plot_consumers", numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize)
    # plot_barchart(buffer_names, buffer_data, mean_data[0], "buforów", "Bufory", f"{dirname}/plot_buffers", numPasses, numProducers, numConsumers, numBuffers, bufferSize, itemSize)

    return np.array([
        mean_data[0],
        np.mean(mean_data[producer_indices]),
        np.mean(mean_data[consumer_indices]),
        np.mean(mean_data[buffer_indices])
    ])

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

def plot_performance(data, index, title, filename):
    plt.plot([1, 2, 3, 4, 5], performance_data[0, :, index], "-o")
    plt.plot([1, 2, 3, 4, 5], performance_data[1, :, index], "-o")
    plt.plot([1, 2, 3, 4, 5], performance_data[2, :, index], "-o")
    plt.plot([1, 2, 3, 4, 5], performance_data[3, :, index], "-o")
    plt.plot([1, 2, 3, 4, 5], performance_data[4, :, index], "-o")
    
    plt.suptitle(f"Ogólna wydajność {title}\nw zależności od parametrów testu", fontsize=22)
    plt.xlabel("Indeks wskazujący na wartość parametru")
    plt.ylabel("Liczba wykonanych operacji")

    locs, labels = plt.xticks()
    locs = list(filter(lambda x: int(x) == x, locs))
    plt.xticks(ticks=locs, labels=list(map(lambda x: f"{int(x)}", locs)))

    plt.legend(
        [
            "liczby Producentów",
            "liczby Konsumenentów",
            "liczby Buforów",
            "pojemności Buforu",
            "rozmiaru elementu"
        ],
        title="Zmiana..."
    )

    plt.gcf().set_size_inches(11, 8)
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()

if __name__ == "__main__":
    plt.rcParams["figure.figsize"] = (9, 6)
    plt.rcParams["savefig.dpi"] = 300
    plt.rcParams.update({'font.size': 16})

    dirs = list(filter(lambda x: "." not in x, os.listdir()))
    performance_data = np.ndarray((5, 5, 4))
    
    for dirname in dirs:
        data = np.genfromtxt(f"{dirname}/results.csv", delimiter=',', dtype=np.int64)
        mean_results = plot_results(data, dirname)

        numProducers, numConsumers, numBuffers, bufferSize, itemSize = np.array(re.findall('\\d+', dirname), dtype=np.int64)

        if numProducers != 150:
            idx = int(numProducers / 50 - 1)
            mean_results[1] = mean_results[1] / (150 / numProducers)
            performance_data[0, idx, :] = mean_results
        elif numConsumers != 150:
            idx = int(numConsumers / 50 - 1)
            mean_results[2] = mean_results[2] / (150 / numConsumers)
            performance_data[1, idx, :] = mean_results
        elif numBuffers != 150:
            idx = int(numBuffers / 50 - 1)
            mean_results[3] = mean_results[3] / (150 / numBuffers)
            performance_data[2, idx, :] = mean_results
        elif bufferSize != 30:
            idx = int(bufferSize / 10 - 1)
            performance_data[3, idx, :] = mean_results
        elif itemSize != 1024:
            idx = 0 if itemSize == 64 else 1 if itemSize == 512 else 3 if itemSize == 65536 else 4
            performance_data[4, idx, :] = mean_results
        else:
            performance_data[:, 2, :] = mean_results

    plot_performance(performance_data, 0, "Pośrednika", "middleman.png")
    plot_performance(performance_data, 1, "Producenta", "producer.png")
    plot_performance(performance_data, 2, "Konsumenta", "consumer.png")
    plot_performance(performance_data, 3, "Buforu", "buffer.png")
