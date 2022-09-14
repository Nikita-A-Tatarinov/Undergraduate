#include <iostream>
#include <fstream>

void generate_random_sample(const int first_num, const int size, const char *path) {
    if (first_num < 0 || first_num > 9999) {
        throw std::logic_error(
                "Все элементы выборки должны лежать в диапазоне [0; 9999].");
    }
    std::ofstream fout;
    fout.open(path);
    for (int i = 0, cur_num = first_num; i < size; i++) {
        fout << "0." << cur_num / 1000 << (cur_num % 1000) / 100 <<
             (cur_num % 100) / 10 << cur_num % 10 << "\n";
        cur_num *= cur_num;
        cur_num /= 100;
        cur_num %= 10000;
    }
    fout.close();
}

void interval_distribution(const char *num_path, const char *distribution_path) {
    int *intervals = new int[10];
    for (int i = 0; i < 10; i++) {
        intervals[i] = 0;
    }

    std::ifstream fin;
    fin.open(num_path);
    double num;
    while (fin >> num) {
        intervals[static_cast<int>(num * 10)]++;
    }
    fin.close();

    std::ofstream fout;
    fout.open(distribution_path);
    for (int i = 0; i < 10; i++) {
        fout << intervals[i] << "\n";
    }
    fout.close();

    delete[] intervals;
}

void permutation_distribution(const char *num_path,
                              const char *permutation_distribution_path) {
    int *permutations = new int[6];
    for (int i = 0; i < 6; i++) {
        permutations[i] = 0;
    }

    std::ifstream fin;
    fin.open(num_path);
    double num1, num2, num3;
    while ((fin >> num1) && (fin >> num2) && (fin >> num3)) {
        if (num1 <= num2 && num2 <= num3) {
            permutations[0]++;
        } else if (num1 <= num3 && num3 <= num2) {
            permutations[1]++;
        } else if (num2 <= num1 && num1 <= num3) {
            permutations[2]++;
        } else if (num2 <= num3 && num3 <= num1) {
            permutations[3]++;
        } else if (num3 <= num1 && num1 <= num2) {
            permutations[4]++;
        } else {
            permutations[5]++;
        }
    }
    fin.close();

    std::ofstream fout;
    fout.open(permutation_distribution_path);
    for (int i = 0; i < 6; i++) {
        fout << permutations[i] << "\n";
    }
    fout.close();
    delete[] permutations;
}

int main() {
    generate_random_sample(1661, 100, "sample100.txt");
    interval_distribution("sample100.txt", "interval_distribution100.txt");
    generate_random_sample(1661, 10000, "sample10000.txt");
    interval_distribution("sample10000.txt", "interval_distribution10000.txt");
    permutation_distribution("sample10000.txt",
                             "permutation_distribution.txt");
    return 0;
}
