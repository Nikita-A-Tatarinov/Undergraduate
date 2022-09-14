#include <iostream>
#include <fstream>
#include <random>

const unsigned int bootstrap_length = 1000;

template<typename T>
int sgn(T val) {
    return (T(0) < val) - (val < T(0));
}

int cmp(const void *a, const void *b) {
    return sgn(*(double *) a - *(double *) b);
}

///Вычисляет выборочное среднее выборки sample
///размера size.
double sample_mean(const double *const sample,
                   const unsigned int size) {
    double result = 0;
    for (unsigned int i = 0; i < size; i++) {
        result += sample[i];
    }
    return result / static_cast<double>(size);
}

///Вычисляет дисперсию выборки sample размера size.
double sample_variance(const double *const sample,
                       const unsigned int size) {
    double sample_mean_ = sample_mean(sample, size);
    double result = 0;
    for (unsigned int i = 0; i < size; i++) {
        result += (sample[i] - sample_mean_) *
                  (sample[i] - sample_mean_);
    }
    return result / static_cast<double>(size);
}

///Вычисляет скорректированную дисперсию выборки sample
///размера size.
double biased_sample_variance(const double *const sample,
                              const unsigned int size) {
    double sample_mean_ = sample_mean(sample, size);
    double result = 0;
    for (unsigned int i = 0; i < size; i++) {
        result += (sample[i] - sample_mean_) *
                  (sample[i] - sample_mean_);
    }
    return result / static_cast<double>(size - 1);
}

///Получает выборку из файла path. Формат файла:
///первый элемент - размер выборки (сохраняется
///в size); далее идут элементы выборки в соответствующем
///количестве. Корректность файла не проверяется.
///Возвращает ссылку на выборку.
double *get_sample(const char *const path,
                   unsigned int &size) {
    //Файл, откуда получаем элементы выборки.
    std::ifstream fin;
    fin.open(path);
    //Размер выборки.
    fin >> size;
    auto *sample = new double[size];
    for (unsigned int i = 0; i < size; i++) {
        fin >> sample[i];
    }
    fin.close();
    return sample;
}

///Сортирует выборку sample размера size и выводит в файл
///path основную информацию об этой выборке.
void print_sample_data(const char *const path,
                       double *const sample,
                       const unsigned int size) {
    //Файл, в который выводим информацию о выборке.
    std::ofstream fout;
    fout.open(path);
    //Сортируем выборку.
    qsort(sample, size, sizeof(double), cmp);

    fout << "Отсортированная выборка:\n";
    for (unsigned int i = 0; i < size; i++) {
        fout << sample[i] << " ";
    }
    fout << "\nВыборочное среднее: " << sample_mean(sample, size) << "\n";
    fout << "Выборочная дисперсия: " << sample_variance(sample, size) << "\n";
    fout << "Скорректированная выборочная дисперсия: " <<
         biased_sample_variance(sample, size) << "\n";
    fout.close();
}

///В файл sample_path сохраняет bootstrap_length перевыборок длины size
///от выборки sample размера size. В файл mean_path сохраняются
///средние значения перевыборок. В файл mean_sorted_path сохраняются
///отсортированные средние значение перевыборок. В файл interval_path
///сохраняются границы 90% доверительного интервала.
///Возвращается массив средних значений перевыборок.
double *bootstrap(const char *const sample_path,
                  const char *const mean_path,
                  const char *const mean_sorted_path,
                  const char *const interval_path,
                  const double *const sample,
                  const unsigned int size) {
    //Генератор случайных чисел для составления перевыборок.
    std::random_device rd;
    std::mt19937 engine(rd());
    std::uniform_int_distribution<unsigned int>
            element_index(0, size - 1);
    //Файл с перевыборками.
    std::ofstream sample_fout;
    sample_fout.open(sample_path);
    //Файл со средними значениями перевыборок.
    std::ofstream mean_fout;
    mean_fout.open(mean_path);
    //Файл с отсортированными средними значениями
    //перевыборок.
    std::ofstream mean_sorted_fout;
    mean_sorted_fout.open(mean_sorted_path);
    //Файл с границами 90% доверительного интервала.
    std::ofstream interval_fout;
    interval_fout.open(interval_path);
    //Массив средних значений перевыборок.
    auto *means = new double[bootstrap_length];
    //Создаём bootstrap_length новых перевыборок.
    for (unsigned int i = 0; i < bootstrap_length; i++) {
        means[i] = 0;
        //Записыпаем size элементов перевыборки в файл.
        for (unsigned int j = 0; j < size; j++) {
            sample_fout << sample[element_index(engine)] << " ";
            means[i] += sample[element_index(engine)];
        }
        sample_fout << "\n";
        means[i] /= static_cast<double>(size);
        mean_fout << means[i] << "\n";
    }
    //Сортируем массив средних для вычисления 90% доверительного
    //интервала.
    qsort(means, bootstrap_length, sizeof(double), cmp);
    for (unsigned int i = 0; i < bootstrap_length; i++) {
        mean_sorted_fout << means[i] << "\n";
    }
    interval_fout << means[static_cast<unsigned long long>(
                                      0.05 * bootstrap_length) - 1] << " "
                     << means[static_cast<unsigned long long>(
                             0.95 * bootstrap_length)];

    sample_fout.close();
    mean_fout.close();
    mean_sorted_fout.close();
    interval_fout.close();
    return means;
}

int main() {
    //Из файла "sample.txt" получаем выборку sample размера
    //size, сортируем её и выводим основную информацию о ней.
    unsigned int size;
    double *sample = get_sample("sample.txt", size);
    print_sample_data("sample_data.txt", sample, size);

    //Создаём bootstrap_length перевыборок (сохраняем в файл
    //"bootstrap.txt"), вычисляем для каждой среднее значение
    //(сохраняем в файл "bootstrap_mean.txt" и после сортировки
    //в файл "bootstrap_mean_sorted.txt") и получаем
    //эти средние зачения в виде массива. Границы интервала
    //сохраняются в файл "interval_boundaries.txt".
    double *means = bootstrap("bootstrap.txt",
                              "bootstrap_mean.txt",
                              "bootstrap_mean_sorted.txt",
                              "interval_boundaries.txt",
                              sample, size);

    //Очищаем память.
    delete[] sample;
    delete[] means;
    return 0;
}
