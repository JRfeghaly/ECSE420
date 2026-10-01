import pandas as pd
import matplotlib.pyplot as plt

data = pd.read_csv("thread_results.csv")

plt.plot(
    data["Threads"],
    data["AverageTime"],
    marker="o"
)

plt.xlabel("Number of Threads")
plt.ylabel("Average Execution Time (ms)")
plt.title("Execution Time vs. Number of Threads")
plt.grid(True)

plt.savefig("execution_time_vs_threads.png", dpi=300, bbox_inches="tight")

plt.show()