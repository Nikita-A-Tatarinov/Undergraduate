#include <iostream>
#include <fstream>
#include <cmath>
#include <vector>
#include <algorithm>

///Сравнивает пару чисел по первому элементу пары.
bool cmp(std::pair<double, double> &i, std::pair<double, double> &j) {
    return i.first < j.first;
}

///Для выборки X создаём массив рангов её элементов.
void get_ranks(const std::vector<double> &X, std::vector<double> &ranks) {
    ranks.erase(ranks.begin(), ranks.end());

    //Массив пар элементов - наблюдение и его ранг.
    std::vector<std::pair<double, double>> helper;
    for (double x : X) {
        //Образуем пары, где ранги пока равны 0.
        helper.emplace_back(x, 0);
        ranks.push_back(0);
    }
    //Сортируем по возрастанию наблюдений.
    sort(helper.begin(), helper.end(), cmp);
    //По отсортированному массиву заполняем ранги.
    for (int i = 0, j, in_a_row = 1; i < helper.size(); i++) {
        if (i == helper.size() - 1 ||
            helper[i].first != helper[i + 1].first) {
            double rank = i + 1 - (in_a_row - 1) / 2.0;
            for (j = i - in_a_row + 1; j < i + 1; j++) {
                helper[j].second = rank;
            }
            in_a_row = 1;
        } else {
            in_a_row++;
        }
    }
    //В массив ranks записываем ранге в том порядке, в котором наблюдения
    //расположены в векторе X.
    for (int i = 0, j; i < X.size(); i++) {
        for (j = 0; j < helper.size(); j++) {
            if (X[i] == helper[j].first) {
                ranks[i] = helper[j].second;
                break;
            }
        }
    }
}

///Разделяет строку str по символу sep и возвращает массив
///подстрок str между символами sep (пустые строки не удаляются).
std::string *split(const std::string &str, const char sep) {
    size_t amount_of_elements = 1;
    for (char c : str) {
        if (c == sep) {
            amount_of_elements++;
        }
    }
    auto *elements = new std::string[amount_of_elements];
    for (int i = 0; i < amount_of_elements; i++) {
        elements[i] = "";
    }
    for (int i = 0, j = 0; i < str.length(); i++) {
        if (str[i] == sep) {
            j++;
        } else {
            elements[j] += str[i];
        }
    }
    return elements;
}

///Из файла input_path считывает данные для анализа. Данные с рангами сохраняются в файл
///output_path. Программно данные сохраняются в векторы, переданные в качестве параметров.
void get_data(const std::string &input_path, const std::string &output_path,
              std::vector<double> &framerate, std::vector<double> &frames,
              std::vector<double> &bitrate, std::vector<double> &duration,
              std::vector<double> &size, std::vector<double> &framerate_ranks,
              std::vector<double> &frames_ranks, std::vector<double> &bitrate_ranks,
              std::vector<double> &duration_ranks, std::vector<double> &size_ranks) {
    //Очищаем вукторы, если там хранились какие-либо данные.
    framerate.erase(framerate.begin(), framerate.end());
    frames.erase(frames.begin(), frames.end());
    bitrate.erase(bitrate.begin(), bitrate.end());
    duration.erase(duration.begin(), duration.end());
    size.erase(size.begin(), size.end());
    framerate_ranks.erase(framerate_ranks.begin(), framerate_ranks.end());
    frames_ranks.erase(frames_ranks.begin(), frames_ranks.end());
    bitrate_ranks.erase(bitrate_ranks.begin(), bitrate_ranks.end());
    duration_ranks.erase(duration_ranks.begin(), duration_ranks.end());
    size_ranks.erase(size_ranks.begin(), size_ranks.end());

    //Вектор для сохранения id (только для записи в файл - не для запоминания).
    std::vector<std::string> id;

    //Открываем файлы для чтения и записи.
    std::ifstream fin;
    fin.open(input_path);
    std::ofstream fout;
    fout.open(output_path);

    //Переменная для построчного чтения из файла.
    std::string line;
    std::string *line_elements;
    //Первая строка - строка с названиями столбцов.
    fin >> line;
    line_elements = split(line, ',');
    //Столбцы "N" и "id".
    fout << line_elements[0] << ";" << line_elements[1];
    for (int i = 2; i < 7; i++) {
        fout << ";" << line_elements[i] << ";" << line_elements[i] << "_rank";
    }
    fout << std::endl;
    delete[] line_elements;
    //Считываем наблюдения.
    while ((fin >> line)) {
        line_elements = split(line, ',');
        //Игнорируем номер, так как он будет выводиться в цикле с помощью счётчика.
        id.push_back(line_elements[1]);
        framerate.push_back(strtod(line_elements[2].c_str(), nullptr));
        frames.push_back(strtod(line_elements[3].c_str(), nullptr));
        bitrate.push_back(strtod(line_elements[4].c_str(), nullptr));
        duration.push_back(strtod(line_elements[5].c_str(), nullptr));
        size.push_back(strtod(line_elements[6].c_str(), nullptr));
        delete[] line_elements;
    }

    //Создаём векторы рангов.
    get_ranks(framerate, framerate_ranks);
    get_ranks(frames, frames_ranks);
    get_ranks(bitrate, bitrate_ranks);
    get_ranks(duration, duration_ranks);
    get_ranks(size, size_ranks);

    //Записываем все данные в файл.
    for (int i = 0; i < id.size(); i++) {
        fout << i << ";" << id[i] << ";" << framerate[i] << ";" << framerate_ranks[i] << ";" <<
             static_cast<int>(frames[i]) << ";" << frames_ranks[i] << ";" <<
             static_cast<int>(bitrate[i]) << ";" << bitrate_ranks[i] << ";" <<
             duration[i] << ";" << duration_ranks[i] << ";" <<
             static_cast<int>(size[i]) << ";" << size_ranks[i] << std::endl;
    }

    fin.close();
    fout.close();
}

///Вычисляет и возвращает выборочное среднее выборки X.
double calculate_sample_mean(const std::vector<double> &X) {
    double result = 0;
    for (double element : X) {
        result += element;
    }
    return result / static_cast<double>(X.size());
}

///Вычисляет и возвращает выборочный коэффициент корреляции Пирсона для выборок X и Y.
double calculate_sample_Pearson_correlation_coefficient(const std::vector<double> &X,
                                                        const std::vector<double> &Y) {
    double X_mean = calculate_sample_mean(X), Y_mean = calculate_sample_mean(Y);
    double sum1 = 0, sum2 = 0, sum3 = 0;
    for (int i = 0; i < X.size(); i++) {
        sum1 += (X[i] - X_mean) * (Y[i] - Y_mean);
        sum2 += (X[i] - X_mean) * (X[i] - X_mean);
        sum3 += (Y[i] - Y_mean) * (Y[i] - Y_mean);
    }
    return sum1 / sqrt(sum2) / sqrt(sum3);
}

///Для выборок X и Y (названия X_name и Y_name соответственно) проверяет независимость
///путём вычисления выборочного коэффициента корреляции Пирсона и значения t-статистики.
///Добавляет размер выборок и вычисленные данные в конец файла path.
void test_zero_correlation_hypothesis_with_Pearson(const std::string &path,
                                                   const std::vector<double> &X,
                                                   const std::string &X_name,
                                                   const std::vector<double> &Y,
                                                   const std::string &Y_name) {
    std::ofstream fout;
    fout.open(path, std::ios::app);

    double r_X_Y = calculate_sample_Pearson_correlation_coefficient(X, Y);
    fout << X_name << " - " << Y_name << std::endl << "\tn = " << X.size() << std::endl <<
         "\tr_{X;Y} = " << r_X_Y << std::endl << "\tt = " <<
         r_X_Y * sqrt(static_cast<double>(X.size()) - 2.0) / sqrt(1 - r_X_Y * r_X_Y) <<
         std::endl << std::endl;

    fout.close();
}

///Для выборок X и Y (названия X_name и Y_name соответственно) проверяет независимость
///путём вычисления коэффициента корреляции Спирмена и значения t-статистики.
///Добавляет размер выборок и вычисленные данные в конец файла path.
void test_zero_correlation_hypothesis_with_Spearman(const std::string &path,
                                                    const std::vector<double> &X_ranks,
                                                    const std::string &X_name,
                                                    const std::vector<double> &Y_ranks,
                                                    const std::string &Y_name) {
    std::ofstream fout;
    fout.open(path, std::ios::app);

    double rS_X_Y = calculate_sample_Pearson_correlation_coefficient(X_ranks, Y_ranks);
    fout << X_name << " - " << Y_name << std::endl << "\tn = " << X_ranks.size() << std::endl <<
         "\tr^S_{X;Y} = " << rS_X_Y << std::endl << "\tt = " <<
         rS_X_Y * sqrt(static_cast<double>(X_ranks.size()) - 2.0) / sqrt(1 - rS_X_Y * rS_X_Y)
         << std::endl << std::endl;

    fout.close();
}

int main() {
    std::vector<double> framerate, frames, bitrate, duration, size;
    std::vector<double> framerate_ranks, frames_ranks, bitrate_ranks, duration_ranks, size_ranks;
    get_data("youtube_1.csv", "data_with_ranks.csv",
             framerate, frames, bitrate, duration, size,
             framerate_ranks, frames_ranks, bitrate_ranks, duration_ranks, size_ranks);

    remove("Pearson_correlation.txt");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  framerate, "framerate",
                                                  frames, "frames");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  framerate, "framerate",
                                                  bitrate, "bitrate");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  framerate, "framerate",
                                                  duration, "duration");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  framerate, "framerate",
                                                  size, "size");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  frames, "frames",
                                                  bitrate, "bitrate");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  frames, "frames",
                                                  duration, "duration");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  frames, "frames",
                                                  size, "size");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  bitrate, "bitrate",
                                                  duration, "duration");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  bitrate, "bitrate",
                                                  size, "size");
    test_zero_correlation_hypothesis_with_Pearson("Pearson_correlation.txt",
                                                  duration, "duration",
                                                  size, "size");

    remove("Spearman_correlation.txt");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   framerate_ranks, "framerate",
                                                   frames_ranks, "frames");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   framerate_ranks, "framerate",
                                                   bitrate_ranks, "bitrate");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   framerate_ranks, "framerate",
                                                   duration_ranks, "duration");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   framerate_ranks, "framerate",
                                                   size_ranks, "size");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   frames_ranks, "frames",
                                                   bitrate_ranks, "bitrate");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   frames_ranks, "frames",
                                                   duration_ranks, "duration");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   frames_ranks, "frames",
                                                   size_ranks, "size");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   bitrate_ranks, "bitrate",
                                                   duration_ranks, "duration");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   bitrate_ranks, "bitrate",
                                                   size_ranks, "size");
    test_zero_correlation_hypothesis_with_Spearman("Spearman_correlation.txt",
                                                   duration_ranks, "duration",
                                                   size_ranks, "size");

    return 0;
}
