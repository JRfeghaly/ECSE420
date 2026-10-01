import pandas as pd
import matplotlib.pyplot as plt

data = pd.read_csv("matrix_size_results.csv")

data["SequentialTimeSeconds"] = data["SequentialTime"] / 1000
data["ParallelTimeSeconds"] = data["ParallelTime"] / 1000

plt.plot(
    data["MatrixSize"],
    data["SequentialTimeSeconds"],
    marker="o",
    label="Sequential"
)

plt.plot(
    data["MatrixSize"],
    data["ParallelTimeSeconds"],
    marker="o",
    label="Parallel (15 threads)"
)

plt.xlabel("Matrix Size")
plt.ylabel("Execution Time (s)")
plt.title("Execution Time vs. Matrix Size")
plt.legend()
plt.grid(True)

plt.savefig(
    "execution_time_vs_matrix_size.png",
    dpi=300,
    bbox_inches="tight"
)

plt.show()